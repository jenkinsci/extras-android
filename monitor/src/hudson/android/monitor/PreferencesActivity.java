package hudson.android.monitor;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceScreen;

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

    public static final String SERVER_URL = "preferences_server_url";

    @Deprecated
    public static final String DEFAULT_FEED_NAME = "XLV Labs";

    @Deprecated
    public static final String DEFAULT_FEED_URL = "http://hudson.xlv-labs.com/rssAll";

    @Override
    protected void onCreate(final Bundle icicle) {
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
