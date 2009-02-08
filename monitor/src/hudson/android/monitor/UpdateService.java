/*
 * The MIT License
 *
 * Copyright (c) 2009, Xavier Le Vourch
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.android.monitor;

import hudson.android.monitor.model.BuildData;
import hudson.android.monitor.model.Feed;
import hudson.android.monitor.model.FeedData;
import hudson.android.monitor.model.FeedParser;

import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Config;
import android.util.Log;
import android.widget.Toast;

/**
 *
 * @author Xavier Le Vourch
 *
 */

public class UpdateService extends Service implements OnSharedPreferenceChangeListener {

    private final IBinder binder = new UpdateServiceBinder();

    private final Handler handler = new Handler();

    private AlarmReceiver alarmReceiver;

    private PendingIntent pendingRefreshIntent;

    private PendingIntent pendingUpdateCheckIntent;

    private long lastRefreshDate;

    private long lastUpdateCheckDate;

    private final ExecutorService backgroundExecutor = Executors.newFixedThreadPool(1);

    private final RefreshTask refreshTask = new RefreshTask();

    private final Runnable guiNotificationsTask = new GuiNotificationTask();

    private final Runnable guiRefeshNotificationTask = new GuiRefeshNotificationTask();

    public static final String NEW_FEED_DATA = "New_Feed_Data";

    public static final String REFRESH_FEED_ACTION = "Refresh Feed Action";

    public static final String UPDATE_CHECK_ACTION = "Update Check Action";

    @Override
    public void onCreate() {
        super.onCreate();

        if (Config.LOGD) {
            Log.d(Util.LOG_TAG, "UpdateService.onCreate");
        }

        initAlarms();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(UpdateService.this);
        prefs.registerOnSharedPreferenceChangeListener(this);

        lastUpdateCheckDate = prefs.getLong(PreferencesActivity.LAST_CHECK_DATE, 0);

        setRefreshFeedAlarm();
        setUpdateCheckAlarm();
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        setRefreshFeedAlarm();
        setUpdateCheckAlarm();

        // FIXME: change server url change to force immediate refresh
    }

    @Override
    public void onDestroy() {
        if (Config.LOGD) {
            Log.d(Util.LOG_TAG, "UpdateService.onDestroy");
        }

        unregisterReceiver(alarmReceiver);

        backgroundExecutor.shutdownNow();

        super.onDestroy();
    }

    private void initAlarms() {
        IntentFilter filter;
        alarmReceiver = new AlarmReceiver();

        filter = new IntentFilter(REFRESH_FEED_ACTION);
        registerReceiver(alarmReceiver, filter);

        filter = new IntentFilter(UPDATE_CHECK_ACTION);
        registerReceiver(alarmReceiver, filter);
    }

    private void setRefreshFeedAlarm() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(UpdateService.this);
        String rateString = prefs.getString(PreferencesActivity.REFRESH_RATE, PreferencesActivity.DEFAULT_REFRESH_RATE);
        AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (PreferencesActivity.REFRESH_RATE_NEVER.equals(rateString)) {
            // disable refresh
            if (pendingRefreshIntent != null) {
                if (Config.LOGD) {
                    Log.d(Util.LOG_TAG, "Disabling refresh alarm");
                }

                mgr.cancel(pendingRefreshIntent);
                pendingRefreshIntent = null;
            }
        } else {
            // schedule reset
            boolean wakeup = prefs.getBoolean(PreferencesActivity.WAKEUP_PHONE, true);
            long refreshRate = Long.parseLong(rateString) * Util.MINUTE_IN_MILLISECONDS;

            Intent intent = new Intent(REFRESH_FEED_ACTION);
            pendingRefreshIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
            int alarmType = wakeup ? AlarmManager.RTC_WAKEUP : AlarmManager.RTC;
            long date = lastRefreshDate + refreshRate;
            mgr.set(alarmType, date, pendingRefreshIntent);

            if (Config.LOGD) {
                Log.d(Util.LOG_TAG, "Refresh rate scheduled for " + new Date(date));
            }
        }
    }

    private void setUpdateCheckAlarm() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(UpdateService.this);
        AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (prefs.getBoolean(PreferencesActivity.AUTOMATIC_CHECK_FOR_UPDATES, true)) {
            // schedule update check
            Intent intent = new Intent(UPDATE_CHECK_ACTION);
            pendingUpdateCheckIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
            int alarmType = AlarmManager.RTC;
            long date = lastUpdateCheckDate + Util.DAY_IN_MILLISECONDS;
            mgr.set(alarmType, date, pendingUpdateCheckIntent);

            if (Config.LOGD) {
                Log.d(Util.LOG_TAG, "Update check scheduled for " + new Date(date));
            }
        } else {
            // disable refresh
            if (pendingUpdateCheckIntent != null) {
                if (Config.LOGD) {
                    Log.d(Util.LOG_TAG, "Disabling update check alarm");
                }

                mgr.cancel(pendingUpdateCheckIntent);
                pendingUpdateCheckIntent = null;
            }
        }
    }

    public class AlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            if (action.equals(REFRESH_FEED_ACTION)) {
                startRefreshFeed();
            } else if (action.equals(UPDATE_CHECK_ACTION)) {
                startUpdateCheck();
            } else {
                Log.e(Util.LOG_TAG, "Invalid intent " + action);
            }
        }
    }

    private void startUpdateCheck() {
        if (Config.LOGD) {
            Log.d(Util.LOG_TAG, "UpdateService.startUpdateCheck");
        }

        lastUpdateCheckDate = System.currentTimeMillis();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(UpdateService.this);
        prefs.edit().putLong(PreferencesActivity.LAST_CHECK_DATE, lastUpdateCheckDate).commit();

        backgroundExecutor.submit(new Runnable() {
            public void run() {
                try {
                    PackageInfo info = getPackageManager().getPackageInfo(Util.PACKAGE_NAME, 0);

                    int installedVersion = info.versionCode;
                    String s[] = Util.getLatestVersion(installedVersion);

                    int onlineVersion = Integer.parseInt(s[0]);
                    if (onlineVersion > installedVersion) {
                        final String newVersion = s[1];
                        final String currentVersion = info.versionName;

                        handler.post(new Runnable() {
                            public void run() {
                                Context context = getApplicationContext();

                                NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                int iconId = R.drawable.icon_32;
                                // FIXME localize
                                StringBuilder title = new StringBuilder("Update available: ").append(newVersion);
                                StringBuilder text = new StringBuilder("Installed version: ").append(currentVersion);

                                Intent intent = new Intent().setAction(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse(Util.MARKET_URL));
                                PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);

                                Notification notification = new Notification(iconId, title, System.currentTimeMillis());
                                notification.setLatestEventInfo(context, title, text, contentIntent);
                                int id = 2;
                                mNotificationManager.notify(id, notification);

                            }
                        });
                    }
                    // schedule next check
                    setUpdateCheckAlarm();
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e(Util.LOG_TAG, "IllegalState: call on current package", e);
                }
            }
        });
    }

    private void startRefreshFeed() {
        if (Config.LOGD) {
            Log.d(Util.LOG_TAG, "UpdateService.startRefreshFeed");
        }

        // cancel any pending alarm as refresh may be activated early
        AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mgr.cancel(pendingRefreshIntent);

        lastRefreshDate = System.currentTimeMillis();

        backgroundExecutor.submit(refreshTask);
    }

    private class RefreshTask implements Runnable {
        @Override
        public void run() {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(UpdateService.this);
            boolean debugMode = prefs.getBoolean(PreferencesActivity.DEBUG_MODE, false);

            if (debugMode) {
                handler.post(guiRefeshNotificationTask);
            }

            String serverName = PreferencesActivity.DEFAULT_FEED_NAME;
            String serverURL = prefs.getString(PreferencesActivity.SERVER_URL, PreferencesActivity.DEFAULT_FEED_URL);
            String userName = prefs.getString(PreferencesActivity.SERVER_USERNAME, "");
            String password = prefs.getString(PreferencesActivity.SERVER_PASSWORD, "");

            Feed f = new Feed(1, serverName, serverURL, userName, password);

            try {
                FeedData data = FeedParser.parseHistory(f);

                HudsonMonitorApplication.setFeedData(data);

                Intent intent = new Intent(NEW_FEED_DATA);
                sendBroadcast(intent);
            } catch (MonitorException e) {
                Log.e(Util.LOG_TAG, "Feed Data Parsing", e.getCause());
            }

            // schedule next check
            setRefreshFeedAlarm();

            boolean notificationsEnabled = prefs.getBoolean(PreferencesActivity.NOTIFICATION_ENABLED, true);
            if (notificationsEnabled) {
                handler.post(guiNotificationsTask);
            }
        }
    };

    private class GuiRefeshNotificationTask implements Runnable {
        public void run() {

            Context context = getApplicationContext();
            String msg = "Hudson Monitor refresh started";
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(context, msg, duration);
            toast.show();

            NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            int iconId = R.drawable.icon_32;
            String text = "refresh started";

            Intent intent = new Intent().setAction(Intent.ACTION_VIEW).setData(Uri.parse(""));
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);

            Notification notification = new Notification(iconId, text, System.currentTimeMillis());
            notification.setLatestEventInfo(context, text, new Date().toLocaleString(), contentIntent);
            int id = 1;
            mNotificationManager.notify(id, notification);
        }
    };

    private class GuiNotificationTask implements Runnable {
        public void run() {
            Context context = getApplicationContext();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean notificationsEnabled = prefs.getBoolean(PreferencesActivity.NOTIFICATION_ENABLED, true);

            assert notificationsEnabled : "Notification enabled";

            List<BuildData> list = HudsonMonitorApplication.getFeedData().getBuildData();

            long lastUpdate = HudsonMonitorApplication.getLastUpdate();

            if (lastUpdate != 0) {
                boolean vibrateEnabled = prefs.getBoolean(PreferencesActivity.VIBRATE_ENABLED, false);
                String ringtone = prefs.getString(PreferencesActivity.RINGTONE, PreferencesActivity.DEFAULT_RINGTONE);
                Uri ringtoneUri = android.text.TextUtils.isEmpty(ringtone) ? null : Uri.parse(ringtone);
                boolean audioProcessed = false; // beep and/or vibrate only once
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                // send notifications for each new failed builds
                // starting from oldest ones
                ListIterator<BuildData> i = list.listIterator(list.size());
                while (i.hasPrevious()) {
                    BuildData d = i.previous();

                    if (d.isFailure() && d.getDate().getTime() > lastUpdate) {
                        if (Config.LOGD) {
                            Log.d(Util.LOG_TAG, "build date: " + d.getDate().toLocaleString() + " last update: " + new Date(lastUpdate).toLocaleString());
                            Log.d(Util.LOG_TAG, "build date: " + d.getDate().getTime() + " last update: " + lastUpdate);
                        }
                        int iconId = R.drawable.failure;
                        String text = d.getText(true);

                        Notification notification = new Notification(iconId, text, System.currentTimeMillis());

                        Intent intent = new Intent().setAction(Intent.ACTION_VIEW).setData(Uri.parse(d.getLink()));
                        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);

                        String notificationContent = d.getDateString(true);

                        notification.setLatestEventInfo(context, text, notificationContent, contentIntent);
                        if (!audioProcessed) {
                            notification.sound = ringtoneUri;
                            if (vibrateEnabled) {
                                notification.defaults |= Notification.DEFAULT_VIBRATE;
                            }
                            audioProcessed = true;
                        }
                        // use unique ids so that multiple notifications
                        // can be sent
                        int notificationId = (int) d.getDate().getTime();
                        mNotificationManager.notify(notificationId, notification);
                    }
                }
            }

            if (!list.isEmpty()) {
                // date of latest build
                Date d = list.get(0).getDate();
                HudsonMonitorApplication.setLastUpdate(d.getTime());
                if (Config.LOGD) {
                    Log.d(Util.LOG_TAG, "feed last updated at " + d.toLocaleString());
                }
            }
        }
    };

    @Override
    public void onStart(final Intent intent, final int startId) {
        if (Config.LOGD) {
            Log.d(Util.LOG_TAG, "UpdateService.onStart");
        }

        super.onStart(intent, startId);

        startRefreshFeed();
    }

    public class UpdateServiceBinder extends Binder {
        public UpdateService getService() {
            return UpdateService.this;
        }
    };

    @Override
    public IBinder onBind(final Intent intent) {
        return binder;
    }

}
