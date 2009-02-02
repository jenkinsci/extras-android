package hudson.android.monitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.util.Log;

/**
 *
 * @author Xavier Le Vourch
 *
 */

public final class Util {

    public static final String LOG_TAG = "HudsonMonitor";

    public static final String UPDATE_URL = "http://xlv-labs.com/android/hudsonmonitor/latest?version=";

    private Util() {
    }

    public static final String MARKET_URL = "market://search?q=pname:hudson.android.monitor";

    public static final long SECOND_IN_MILLISECONDS = 1000;

    public static final long MINUTE_IN_MILLISECONDS = SECOND_IN_MILLISECONDS * 60;

    public static final long HOUR_IN_MILLISECONDS = MINUTE_IN_MILLISECONDS * 60;

    public static final long DAY_IN_MILLISECONDS = HOUR_IN_MILLISECONDS * 24;

    public static final String PACKAGE_NAME = "hudson.android.monitor";

    private static final String HUDSON_DATE_FORMAT = "yyy-MM-dd'T'HH:mm:ssZ";

    public static Date parseHudsonDate(final String date) {
        try {
            DateFormat dateFormat = new SimpleDateFormat(HUDSON_DATE_FORMAT, Locale.getDefault());
            Date d = dateFormat.parse(date.replaceAll("Z$", "-0000"));

            return d;
        } catch (ParseException e) {
            Log.e(LOG_TAG, "Parse exception for " + date, e);

            return new Date(0);
        }
    }

    public static String[] getLatestVersion(int installedVersion) {
        String[] a = new String[2];
        try {
            URL url;
            URLConnection urlConn = null;

            url = new URL(UPDATE_URL + installedVersion);
            urlConn = url.openConnection();

            BufferedReader r = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));

            a[0] = r.readLine();
            a[1] = r.readLine();

            r.close();
        } catch (IOException e) {
            a[0] = "0";
        }
        return a;
    }

}
