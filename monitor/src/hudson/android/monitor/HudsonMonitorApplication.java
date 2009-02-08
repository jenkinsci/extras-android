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
/**
 *
 */
package hudson.android.monitor;

import hudson.android.monitor.model.FeedData;
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
        if (Config.LOGD) {
            Log.d(Util.LOG_TAG, "HudsonMonitorApplication.onCreate");
        }

        super.onCreate();

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        startService(new Intent(this, UpdateService.class));
    }

    @Override
    public void onTerminate() {
        if (Config.LOGD) {
            Log.d(Util.LOG_TAG, "HudsonMonitorApplication.onTerminate");
        }
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
