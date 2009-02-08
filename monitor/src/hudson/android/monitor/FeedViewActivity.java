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
import hudson.android.monitor.model.FeedData;

import java.util.HashMap;
import java.util.LinkedList;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Config;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

/**
 *
 * @author Xavier Le Vourch
 *
 */

public class FeedViewActivity extends ListActivity {

    private static final int REFRESH_ID = Menu.FIRST;
    private static final int SETTINGS_ID = Menu.FIRST + 1;
    private static final int ABOUT_ID = Menu.FIRST + 2;

    private FeedDataReceiver receiver;

    @Override
    protected void onCreate(final Bundle state) {
        super.onCreate(state);

        if (Config.LOGD) {
            Log.d(Util.LOG_TAG, "FeedViewActivity.onCreate");
        }

        setContentView(R.layout.build_history);
        getListView().setOnCreateContextMenuListener(this);
    }

    public class FeedDataReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            updateJobList();
        }
    }

    private void updateJobList() {
        if (Config.LOGD) {
            Log.d(Util.LOG_TAG, "FeedViewActivity.updateJobList");
        }

        FeedData feedData = HudsonMonitorApplication.getFeedData();

        LinkedList<HashMap<String, Object>> list = new LinkedList<HashMap<String, Object>>();

        if (feedData != null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            boolean usesGreenForSuccess = prefs.getBoolean(PreferencesActivity.USES_GREEN_FOR_SUCCESS, false);

            for (BuildData b : feedData.getBuildData()) {
                HashMap<String, Object> m = new HashMap<String, Object>();
                String text = b.getText(true);
                String link = b.getLink();
                String date = b.getDateString(true);

                m.put("text", text);
                m.put("date", date);
                m.put("link", link);

                String status;
                int iconId;
                if (b.isSuccess()) {
                    iconId = usesGreenForSuccess ? R.drawable.success_g : R.drawable.success;
                    status = "S";
                } else if (b.isFailure()) {
                    iconId = R.drawable.failure;
                    status = "F";
                } else if (b.isWarning()) {
                    iconId = R.drawable.warning;
                    status = "W";
                } else {
                    iconId = R.drawable.unknown;
                    status = "U";
                }
                m.put("build_status_icon", iconId);

                m.put("status", status);
                list.add(m);
            }
        }

        String[] keys = { "text", "date", "build_status_icon" };
        int[] resources = { R.id.line1, R.id.date, R.id.build_status_icon };
        SimpleAdapter a = new SimpleAdapter(this, list, R.layout.build_history_list_item, keys, resources);
        setListAdapter(a);
    }

    @Override
    protected void onResume() {
        if (Config.LOGD) {
            Log.d(Util.LOG_TAG, "FeedViewActivity.onResume");
        }

        super.onResume();

        IntentFilter filter;
        filter = new IntentFilter(UpdateService.NEW_FEED_DATA);
        receiver = new FeedDataReceiver();
        registerReceiver(receiver, filter);

        updateJobList();
    }

    @Override
    protected void onPause() {
        if (Config.LOGD) {
            Log.d(Util.LOG_TAG, "FeedViewActivity onPause");
        }

        unregisterReceiver(receiver);

        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, REFRESH_ID, 0, R.string.menu_refresh).setIcon(R.drawable.ic_menu_refresh);
        menu.add(0, SETTINGS_ID, 0, R.string.menu_settings).setIcon(android.R.drawable.ic_menu_preferences);
        menu.add(0, ABOUT_ID, 0, R.string.menu_about).setIcon(android.R.drawable.ic_menu_info_details);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
        case REFRESH_ID: {
            // request refresh
            startService(new Intent(this, UpdateService.class));
            break;
        }
        case SETTINGS_ID: {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setClassName(this, PreferencesActivity.class.getName());
            startActivity(intent);
            break;
        }
        case ABOUT_ID:
        default:
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            StringBuffer title = new StringBuffer(getString(R.string.about_title));
            StringBuffer msg = new StringBuffer();
            try {
                PackageInfo info = getPackageManager().getPackageInfo(Util.PACKAGE_NAME, 0);
                msg.append("Version ").append(info.versionName).append("\n\n");
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(Util.LOG_TAG, "IllegalState: call on current package", e);
            }
            msg.append(getString(R.string.about_description)).append("\n\n").append(getString(R.string.app_url));

            builder.setTitle(title);
            builder.setMessage(msg);
            builder.setIcon(R.drawable.icon_32);
            builder.setPositiveButton(R.string.about_button_visit_site, new DialogInterface.OnClickListener() {
                public void onClick(final DialogInterface dialogInterface, final int i) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.app_url)));
                    startActivity(intent);
                }
            });
            builder.setNegativeButton(R.string.about_button_close, null);
            builder.show();
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onListItemClick(final ListView l, final View v, final int position, final long id) {
        @SuppressWarnings("unchecked")
        HashMap<String, Object> map = (HashMap<String, Object>) l.getItemAtPosition(position);

        String link = (String) map.get("link");

        Intent intent = new Intent().setAction(Intent.ACTION_VIEW).setData(Uri.parse(link));
        startActivity(intent);
    }

}
