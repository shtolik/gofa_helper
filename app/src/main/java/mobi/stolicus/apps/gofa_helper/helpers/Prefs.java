package mobi.stolicus.apps.gofa_helper.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * Class for handling preferences of the app in SharedPreferences
 * Created by shtolik on 09.03.2015.
 */
public class Prefs {
    public static final String PACKAGE_EXTERNAL = "mobi.stolicus.apps.gofa_helper";
    public static final String SHARED_PREFS_CONTEXT_STRING = PACKAGE_EXTERNAL;
    public static final String SET_RUN_ON_STARTUP = "RUN_ON_STARTUP";
    public static final String SET_OBSERVE_CLIPBOARD = "OBSERVE_CLIPBOARD";
    public static final String SET_CREATE_WIDGET = "CREATE_WIDGET";
    public static final String SET_KEEP_LINKS_HISTORY = "KEEP_LINKS_HISTORY";
    public static final boolean DEF_RUN_ON_STARTUP = true;
    public static final boolean DEF_OBSERVE_CLIPBOARD = true;
    public static final boolean DEF_CREATE_WIDGET = false;
    public static final boolean DEF_KEEP_LINKS_HISTORY = false;
    public static final String GOFA_PACKAGE_NAME = "net.fishlabs.gofa";
    public static final String GOFA_PATTERN = "gofa://.+";
    public static final String GOFA_SCHEME = "gofa://";
    public static final String SET_OPEN_ON_DOUBLE_COPY = "SET_OPEN_ON_DOUBLE_COPY";
    public static final boolean DEF_OPEN_ON_DOUBLE_COPY = false;
    public static final String SET_OPEN_ON_SINGLE_COPY = "SET_OPEN_ON_SINGLE_COPY";
    public static final boolean DEF_OPEN_ON_SINGLE_COPY = false;
    public static final String SET_POST_NOTIFICATION = "set_post_notification";
    public static final boolean DEF_POST_NOTIFICATION = true;
    public static final String SET_MANUAL_INPUT_ENABLED = "set_manual_input_enabled";
    public static final boolean DEF_MANUAL_INPUT_ENABLED = false;

    public static final String SET_GOOGLE_ANALYTICS_ENABLED = "GoogleAnalyticsEnabled";
    public static final boolean DEF_GOOGLE_ANALYTICS_ENABLED = true;

    private static final String TAG = Prefs.class.getSimpleName();
    private static final String PREFERENCES_FILENAME = "link_pref";

    public static SharedPreferences getPrefs(Context context) {
        SharedPreferences myPrefs;
        try {
            Context sharedPrefsContext = context.createPackageContext(
                    SHARED_PREFS_CONTEXT_STRING, 0);
            int flags = Context.MODE_PRIVATE;
            myPrefs = sharedPrefsContext.getSharedPreferences(PREFERENCES_FILENAME,
                    flags);
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, getMethodName() + " : " + "NameNotFoundException in getSharedPrefs, trying just context:", e);
            myPrefs = context.getSharedPreferences(PREFERENCES_FILENAME,
                    Context.MODE_PRIVATE);
        }
        return myPrefs;
    }

    public static String getMethodName() {
        return getMethodName(3);
    }

    public static String getMethodName(final int depth) {
        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        return "/" + ste[1 + depth].getMethodName() + "/";
    }
}
