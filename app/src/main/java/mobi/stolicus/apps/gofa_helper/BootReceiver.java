package mobi.stolicus.apps.gofa_helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mobi.stolicus.apps.gofa_helper.helpers.Prefs;

/**
 * receiver for auto starting app after reboot
 * Created by shtolik on 25.08.2015.
 */
public class BootReceiver extends BroadcastReceiver {

    private static final Logger logger = LoggerFactory.getLogger(BootReceiver.class);

    @Override
    public void onReceive(Context context, Intent intent) {

        SharedPreferences sp = Prefs.getPrefs(context);
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) && sp.getBoolean(Prefs.SET_RUN_ON_STARTUP, Prefs.DEF_RUN_ON_STARTUP)) {
            Intent startOnBootIntent = new Intent(context, GofaHelperActivity.class);
            startOnBootIntent.setAction(GofaHelperActivity.ACTION_REQUEST_BOOTUP);
            startOnBootIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(startOnBootIntent);
        } else {
            logger.debug("/onReceive/app got intent " + intent.toString() + " , but didn't autostart cause it disabled in prefs");
        }
    }
}
