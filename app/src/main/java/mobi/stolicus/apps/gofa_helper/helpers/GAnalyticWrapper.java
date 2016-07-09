package mobi.stolicus.apps.gofa_helper.helpers;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import mobi.stolicus.app.gofa_helper.BuildConfig;
import mobi.stolicus.app.gofa_helper.R;

/**
 * for handling google analytics
 * Created by shtolik on 18/05/2016
 */
public class GAnalyticWrapper {
    public static final String Action = "Action";
    public static final String LauncherClickStartHelper = "LauncherClickStartHelper";
    public static final String LauncherClickStartGame = "LauncherClickStartGame";
    public static final String HistoryClipClicked = "HistoryClipClicked";
    public static final String NotificationClicked = "NotificationClicked";
    public static final String WidgetClicked = "WidgetClicked";
    public static final String WidgetClickedNoGofa = "WidgetClickedNoGofa";
    public static final String ClipAddedToBuffer = "ClipAddedToBuffer";
    public static final String MultiClipAddedToBuffer = "MultipleClipsAddedToBuffer";
    public static final String ManualLinkInput = "ManualLinkInput";

    public static final String SCREEN_CONFIG = "config";
    public static final String SCREEN_HISTORY = "history";
    public static final String SCREEN_START = "gofa~";
    private static final Logger logger = LoggerFactory.getLogger(GAnalyticWrapper.class);
    private final static String TAG = GAnalyticWrapper.class.getSimpleName();
    // Dispatch period in seconds.
    private static final int GA_DISPATCH_PERIOD_DEBUG = 30; //every minute
    private static final int GA_DISPATCH_PERIOD_RELEASE = 1800;//every 30min
    private static GAnalyticWrapper INSTANCE;
    private Context mAppContext = null;
    private Tracker mTracker;

    //	  // Prevent hits from being sent to reports, i.e. during testing.
//	  private static final boolean GA_IS_DRY_RUN = false;

    private GAnalyticWrapper(Context context1) {
        mAppContext = context1.getApplicationContext();
    }

    public static GAnalyticWrapper getInstance(Context context1) {

        synchronized (GAnalyticWrapper.class) {
            if (INSTANCE == null) {
                INSTANCE = new GAnalyticWrapper(context1);
                INSTANCE.initializeGa();
            }
        }
        return INSTANCE;
    }

    private void initializeGa() {

        SharedPreferences sp = Prefs.getPrefs(mAppContext);
        boolean googleAnalyticsEnabledForApp = sp.getBoolean(Prefs.SET_GOOGLE_ANALYTICS_ENABLED, Prefs.DEF_GOOGLE_ANALYTICS_ENABLED);
        if (!googleAnalyticsEnabledForApp) {
            logger.info(TAG + "/initializeGa/analytics not enabled. opting out");
            GoogleAnalytics.getInstance(mAppContext).setAppOptOut(true);
            return;
        }
        GoogleAnalytics mGa = GoogleAnalytics.getInstance(mAppContext);

        // Set dispatch period.
        if (BuildConfig.DEBUG) {
            mGa.setLocalDispatchPeriod(GA_DISPATCH_PERIOD_DEBUG);
        } else {
            mGa.setLocalDispatchPeriod(GA_DISPATCH_PERIOD_RELEASE);
        }

        boolean googanUserEnabledInSetting = Prefs.getPrefs(mAppContext).getBoolean(
                Prefs.SET_GOOGLE_ANALYTICS_ENABLED, Prefs.DEF_GOOGLE_ANALYTICS_ENABLED);
        if (!googanUserEnabledInSetting) {
            GoogleAnalytics.getInstance(mAppContext).setAppOptOut(true);
            logger.trace(TAG + "/initializeGa/opted out from GA");
            mGa.setDryRun(true);

        } else {
            GoogleAnalytics.getInstance(mAppContext).setAppOptOut(false);
            logger.trace(TAG + "/initializeGa/opted in for GA");
            mGa.setDryRun(false);
        }

    }

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     *
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(mAppContext);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.app_tracker);
        }
        return mTracker;
    }

    synchronized public void setAnalyticsOptOut(boolean optOut) {
        GoogleAnalytics.getInstance(mAppContext).setAppOptOut(optOut);
        logger.debug(TAG + "/setAnalyticsOptOut/opt out from GA:" + optOut);
    }

    public void reportEvent(String category, String action, String label) {
        getDefaultTracker().send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel(label)
                .build());
    }

    public void reportScreenViewStart(String screenName) {
        // Get tracker.
        Tracker t = getDefaultTracker();
        // Set screen name.
        t.setScreenName(screenName);
        // Send a screen view.
        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public void reportScreenViewStop(String screenName) {
        Tracker t = getDefaultTracker();
        // Set screen name.
        t.setScreenName(screenName);

        Map<String, String> hit = new HitBuilders.ScreenViewBuilder().build();
        hit.put("&sc", "end");
        t.send(hit);
    }
}
