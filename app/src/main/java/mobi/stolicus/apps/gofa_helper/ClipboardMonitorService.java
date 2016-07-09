package mobi.stolicus.apps.gofa_helper;

import android.app.Activity;
import android.app.Service;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.widget.Toast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import mobi.stolicus.app.gofa_helper.R;
import mobi.stolicus.apps.gofa_helper.db.Clip;
import mobi.stolicus.apps.gofa_helper.db.ClipWrapper;
import mobi.stolicus.apps.gofa_helper.helpers.ClipboardHelper;
import mobi.stolicus.apps.gofa_helper.helpers.GAnalyticWrapper;
import mobi.stolicus.apps.gofa_helper.helpers.GofaIntentHelper;
import mobi.stolicus.apps.gofa_helper.helpers.NotificationHelper;
import mobi.stolicus.apps.gofa_helper.helpers.Prefs;
import mobi.stolicus.apps.gofa_helper.helpers.TopActivityHelper;

/**
 * Monitors the {@link ClipboardManager} for changes and logs the text to a file.
 */
public class ClipboardMonitorService extends Service {
    private static final Logger logger = LoggerFactory.getLogger(ClipboardMonitorService.class);
    private ClipboardManager mClipboardManager;

    private final ClipboardManager.OnPrimaryClipChangedListener mOnPrimaryClipChangedListener =
            new ClipboardManager.OnPrimaryClipChangedListener() {
                @Override
                public void onPrimaryClipChanged() {
                    logger.trace("/OnPrimaryClipChangedListener/");

                    String text = ClipboardHelper.getTextFromPrimaryClipboard(mClipboardManager);
                    List<String> gofaLinks = GofaIntentHelper.checkAndFindGofaLinks(text);

                    Clip clip = null;
                    Clip lastGofaClip = null;
                    if (gofaLinks != null && gofaLinks.size() > 0
                            && Prefs.getPrefs(getApplicationContext()).getBoolean(Prefs.SET_KEEP_LINKS_HISTORY, Prefs.DEF_KEEP_LINKS_HISTORY)) {
                        ClipWrapper cw = ClipWrapper.getInstance(getApplicationContext());
                        lastGofaClip = cw.getLastClip();
                        for (String link : gofaLinks) {
                            clip = new Clip(link, System.currentTimeMillis());
                            //adding or updating clip's timestamp if item exist with the same text
                            cw.addReplaceClip(clip, true);
                        }


                        if (gofaLinks.size() > 1) {
                            GAnalyticWrapper.getInstance(getApplicationContext()).reportEvent(
                                    GAnalyticWrapper.Action, GAnalyticWrapper.MultiClipAddedToBuffer, "");
                        } else if (gofaLinks.size() == 1) {
                            GAnalyticWrapper.getInstance(getApplicationContext()).reportEvent(
                                    GAnalyticWrapper.Action, GAnalyticWrapper.ClipAddedToBuffer, "");
                        }

                    } // else { //keeping clips history not allowed or no links found }


                    if (gofaLinks != null && clip != null) {
                        String opening = checkNeedsToOpenInGofa(clip, lastGofaClip);

                        if (opening != null) {
                            logger.info("/OnPrimaryClipChangedListener/Opening link in gofa");
                            Intent gofaIntent = GofaIntentHelper.prepareFiringGofaIntent(getApplicationContext(), clip.getText(), opening);
                            if (gofaIntent != null) {
                                Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.opening_link) + gofaIntent.getData().toString() + opening, Toast.LENGTH_LONG).show();
                                startActivity(gofaIntent);
                            } else {
                                logger.warn("/no link made of clip=" + clip.getText());
                            }
                        }

                        if (Prefs.getPrefs(getApplicationContext()).getBoolean(Prefs.SET_POST_NOTIFICATION, Prefs.DEF_POST_NOTIFICATION)) {
                            if (gofaLinks.size() == 1) {
                                NotificationHelper.sendNotification(ClipboardMonitorService.this, clip.getText());
                            } else {
                                //multiple clips
                                NotificationHelper.sendNotification(ClipboardMonitorService.this, gofaLinks);
                            }
                        }
                    }

                }
            };

    public static void clipboardListeningStop(Activity act) {
        if (!act.stopService(new Intent(act, ClipboardMonitorService.class))) {
            logger.warn("/clipboardListeningStop/failed to stop clipboard listening service");
        } else {
            logger.info("/clipboardListeningStop/stopped listening clipboard");
        }
    }

    public static void clipboardListeningStart(Activity act) {
        if (Prefs.getPrefs(act.getApplicationContext()).getBoolean(Prefs.SET_OBSERVE_CLIPBOARD, Prefs.DEF_OBSERVE_CLIPBOARD)) {
            act.startService(new Intent(act, ClipboardMonitorService.class));
            logger.info("/clipboardListeningStart/started listening clipboard");
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mClipboardManager =
                (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        mClipboardManager.addPrimaryClipChangedListener(
                mOnPrimaryClipChangedListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mClipboardManager != null) {
            mClipboardManager.removePrimaryClipChangedListener(
                    mOnPrimaryClipChangedListener);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private String checkNeedsToOpenInGofa(Clip clip, Clip lastGofaClip) {

        if (clip == null) {
            logger.debug("/checkNeedsToOpenInGofa/clip == null");
            return null;
        }
        String currentActivityPackage = TopActivityHelper.getCurrentTopActivity(ClipboardMonitorService.this);
        logger.debug("/checkNeedsToOpenInGofa/current activity:" + currentActivityPackage);

        if (currentActivityPackage != null && (currentActivityPackage.contains(Prefs.GOFA_PACKAGE_NAME)
                || currentActivityPackage.contains(getApplicationContext().getPackageName()))
                ) {
            logger.debug("/checkNeedsToOpenInGofa/game is in foreground. Not opening link in gofa.");
            return null;
        } else {
            if (GofaHelperActivity.mGofaHelperRunning){
                logger.debug("/checkNeedsToOpenInGofa/mGofaHelperRunning == true");
                return null;
            }
            logger.debug("/checkNeedsToOpenInGofa/game is not in foreground or no info available.");
//          seems that we can't get current top activity. if it's lollipop,
//          then we are missing permission from user. stopping
//          or not gofa package top.
        }
        SharedPreferences sp = Prefs.getPrefs(getApplicationContext());
        if (sp.getBoolean(Prefs.SET_OPEN_ON_DOUBLE_COPY, Prefs.DEF_OPEN_ON_DOUBLE_COPY)
                && sp.getBoolean(Prefs.SET_KEEP_LINKS_HISTORY, Prefs.DEF_KEEP_LINKS_HISTORY)
                ) {
            if (lastGofaClip != null && lastGofaClip.getText().equals(clip.getText())) {
                logger.debug("/checkNeedsToOpenInGofa/Last clip is same as now. Opening according to opening on double copy pref");
                return getString(R.string.opening_on_double_click);
            }
        }

        if (Prefs.getPrefs(getApplicationContext()).getBoolean(Prefs.SET_OPEN_ON_SINGLE_COPY, Prefs.DEF_OPEN_ON_SINGLE_COPY)) {
            logger.debug("/checkNeedsToOpenInGofa/Opening according to opening on single copy");
            return getString(R.string.opening_on_link_copy);
        }

        return null;
    }
}
