package hudson.android.monitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Config;
import android.util.Log;

/**
 *
 * @author Xavier Le Vourch
 *
 */

public class AutoStartReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Config.LOGD) {
            Log.d(Util.LOG_TAG, "AutoStarter.onReceiveIntent");
        }
    }

}
