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

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceScreen;
import android.util.Config;
import android.util.Log;

/**
 *
 * @author Xavier Le Vourch
 *
 */

public final class PreferencesActivity extends android.preference.PreferenceActivity implements OnSharedPreferenceChangeListener {

    public static final String USES_GREEN_FOR_SUCCESS = "preferences_green_for_success";

    public static final String DEBUG_MODE = "preferences_debug_mode";

    public static final String NOTIFICATION_ENABLED = "preferences_enable_notifications";
    public static final String RINGTONE = "preferences_ringtone";
    public static final String DEFAULT_RINGTONE = "content://settings/system/notification_sound";
    public static final String WAKEUP_PHONE = "preferences_wakeup_phone";
    public static final String VIBRATE_ENABLED = "preferences_vibrate";

    public static final String REFRESH_RATE = "preferences_refresh_rate";
    public static final String REFRESH_RATE_NEVER = "Never";

    public static final String DEFAULT_REFRESH_RATE = "5";

    public static final String AUTOMATIC_CHECK_FOR_UPDATES = "preferences_check_for_updates";
    public static final String LAST_CHECK_DATE = "preferences_last_check_date";

    public static final String SERVER_URL = "preferences_server_url";
    public static final String SERVER_USERNAME = "preferences_server_username";
    public static final String SERVER_PASSWORD = "preferences_server_password";

    @Deprecated
    public static final String DEFAULT_FEED_NAME = "XLV Labs";

    @Deprecated
    public static final String DEFAULT_FEED_URL = "http://hudson.xlv-labs.com/rssAll";

    @Override
    protected void onCreate(final Bundle icicle) {
        if (Config.LOGD) {
            Log.d(Util.LOG_TAG, "PreferencesActivity.onCreate");
        }

        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.preferences);

        PreferenceScreen preferences = getPreferenceScreen();
        preferences.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        // Update service has its own listener
        // startService(new Intent(this, UpdateService.class));
    }

}
