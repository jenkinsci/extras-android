package hudson.android.monitor;

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

    private Util() {
    }

    public static final long SECOND_IN_MILLISECONDS = 1000;

    public static final long MINUTE_IN_MILLISECONDS = SECOND_IN_MILLISECONDS * 60;

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

}
