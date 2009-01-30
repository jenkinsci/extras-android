package hudson.android.monitor;

import hudson.android.monitor.model.BuildData;
import hudson.android.monitor.model.Feed;
import hudson.android.monitor.model.FeedData;
import hudson.android.monitor.model.FeedParser;

import java.util.Date;
import java.util.List;
import java.util.ListIterator;

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

public class UpdateService extends Service {

    private final IBinder binder = new UpdateServiceBinder();

    private final Handler handler = new Handler();

    private AlarmReceiver alarmReceiver;

    private PendingIntent pendingAlarmIntent;

    private final RefreshTask refreshTask = new RefreshTask();

    private final Runnable guiNotificationsTask = new GuiNotificationTask();

    private final Runnable guiRefeshNotificationTask = new GuiRefeshNotificationTask();

    public static final String NEW_FEED_DATA = "New_Feed_Data";

    public static final String ALARM_ACTION = "Alarm Action";

    @Override
    public void onCreate() {
        super.onCreate();

        initAlarm();

        // force a refresh when service is created
        startRefresh();
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(alarmReceiver);

        super.onDestroy();
    }

    private void initAlarm() {
        IntentFilter filter;
        filter = new IntentFilter(ALARM_ACTION);
        alarmReceiver = new AlarmReceiver();
        registerReceiver(alarmReceiver, filter);
    }

    private void setAlarm(final boolean wakeup, final long time) {
        int alarmType = wakeup ? AlarmManager.RTC_WAKEUP : AlarmManager.RTC;
        Intent intent = new Intent(ALARM_ACTION);
        pendingAlarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mgr.set(alarmType, time, pendingAlarmIntent);
    }

    public class AlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            startRefresh();
        }
    }

    private void startRefresh() {
        // cancel any pending alarm as refresh may be activated early
        AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mgr.cancel(pendingAlarmIntent);

        Thread thread = new Thread(null, refreshTask, "Refresh Task");
        thread.start();
    }

    private class RefreshTask implements Runnable {
        @Override
        public void run() {
            long startTime = java.lang.System.currentTimeMillis();

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(UpdateService.this);
            boolean debugMode = prefs.getBoolean(PreferencesActivity.DEBUG_MODE, false);

            if (debugMode) {
                handler.post(guiRefeshNotificationTask);
            }

            String serverName = PreferencesActivity.DEFAULT_FEED_NAME;
            String serverURL = prefs.getString(PreferencesActivity.SERVER_URL, PreferencesActivity.DEFAULT_FEED_URL);

            Feed f = new Feed(1, serverName, serverURL);

            try {
                FeedData data = FeedParser.parseHistory(f.getUrl());

                HudsonMonitorApplication.setFeedData(data);

                Intent intent = new Intent(NEW_FEED_DATA);
                sendBroadcast(intent);
            } catch (MonitorException e) {
                Log.e(Util.LOG_TAG, "Feed Data Parsing", e.getCause());
            }

            String rateString = prefs.getString(PreferencesActivity.REFRESH_RATE, PreferencesActivity.DEFAULT_REFRESH_RATE);
            if (!PreferencesActivity.REFRESH_RATE_NEVER.equals(rateString)) {
                boolean wakeup = prefs.getBoolean(PreferencesActivity.WAKEUP_PHONE, true);
                long refreshRate = Long.parseLong(rateString) * Util.MINUTE_IN_MILLISECONDS;
                setAlarm(wakeup, startTime + refreshRate);
            }

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
                    Log.d(Util.LOG_TAG, "feed updated at " + d.toLocaleString());
                }
            }
        }
    };

    @Override
    public void onStart(final Intent intent, final int startId) {
        super.onStart(intent, startId);

        // FIXME: check whether refresh should be forced (refresh button,
        // server change) or if timer should be adjusted (refresh rate changed)

        startRefresh();
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
