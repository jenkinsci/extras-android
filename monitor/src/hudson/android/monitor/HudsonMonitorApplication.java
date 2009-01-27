/**
 *
 */
package hudson.android.monitor;

import hudson.android.monitor.model.FeedData;

import java.util.Date;

import net.jcip.annotations.GuardedBy;
import android.app.Application;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Config;
import android.util.Log;

/**
 *
 * @author Xavier Le Vourch
 *
 */

public class HudsonMonitorApplication extends Application {

    @SuppressWarnings("PMD.AvoidUsingVolatile")
    private static volatile FeedData feedData;

    @GuardedBy("confined to GUI thread")
    private static long lastUpdate;

    @Override
    public void onCreate() {
        super.onCreate();

        if (Config.LOGD) {
            Log.d(Util.LOG_TAG, "Starting HudsonMonitorApplication at " + new Date().toLocaleString());
        }
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        startService(new Intent(this, UpdateService.class));
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public static long getLastUpdate() {
        return lastUpdate;
    }

    public static void setLastUpdate(long lastUpdate) {
        HudsonMonitorApplication.lastUpdate = lastUpdate;
    }

    public static void setFeedData(FeedData feedData) {
        HudsonMonitorApplication.feedData = feedData;
    }

    public static FeedData getFeedData() {
        return feedData;
    }
}
