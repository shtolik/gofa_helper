package mobi.stolicus.apps.gofa_helper.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mobi.stolicus.app.gofa_helper.R;

/**
 * Class for preparing and firing gofa intents
 * Created by shtolik on 20.08.2015.
 */
public class GofaIntentHelper {

    private static final Logger logger = LoggerFactory.getLogger(GofaIntentHelper.class);
    private static final String TAG = GofaIntentHelper.class.getSimpleName();
    private static final Pattern GOFA_PATTERN = Pattern.compile("gofa://[0-9]+/((players)/[a-zA-Z0-9%]+|(alliances)/[a-zA-Z0-9%]+|(planets)/[0-9]+/[0-9]+/[a-zA-Z0-9]+)");

    public static Intent prepareGofaLaunchIntent(Context act) {
        PackageManager manager = act.getPackageManager();
        return manager.
                getLaunchIntentForPackage(Prefs.GOFA_PACKAGE_NAME);
    }

    public static Intent prepareGofaIntentDataUri(Context act, Intent gofaIntent, String text2) {
        if (text2 == null || text2.length() <= 0) {
            Log.i(TAG, "/prepareGofaIntentDataUri/text (" + text2 + ")doesn't start with " + Prefs.GOFA_SCHEME);
            return null;
        }
        String gofaLink = text2.trim();
        if (gofaLink.startsWith(Prefs.GOFA_SCHEME)) {
            gofaIntent.setData(Uri.parse(gofaLink));
            return gofaIntent;
        } else {
            logger.debug(TAG, "/prepareGofaIntentDataUri/text (" + text2 + ")doesn't start with " + Prefs.GOFA_SCHEME);
        }
        return null;
    }

    public static boolean fireGofaIntent(Activity act, String text2) {

        Intent gofaIntent = prepareFiringGofaIntent(act, text2, null);
        if (gofaIntent != null) {
            Toast.makeText(act, act.getString(R.string.opening_link) + gofaIntent.getData().toString(), Toast.LENGTH_LONG).show();
            act.startActivity(gofaIntent);
            return true;
        } else {
            return false;
        }
    }

    public static Intent prepareFiringGofaIntent(Context context, String text2, String openingComment) {

        try {

            Intent gofaIntent = prepareGofaLaunchIntent(context);
            if (gofaIntent == null) {
                String warn = context.getString(R.string.gofa_not_installed) + Prefs.GOFA_PACKAGE_NAME
                        + context.getString(R.string.gofa_not_installed2);
                Toast.makeText(context, warn, Toast.LENGTH_LONG).show();
                logger.warn(warn);
                return null;
            }

            gofaIntent = prepareGofaIntentDataUri(context, gofaIntent, text2);
            if (gofaIntent == null) {
                String warn = context.getString(R.string.fire_no_gofa_scheme_in_text)
                        + text2;
                Toast.makeText(context, warn, Toast.LENGTH_LONG).show();
                logger.warn(warn);
                return null;
            }

            return gofaIntent;
        } catch (Throwable t) {
            logger.warn(TAG, "/fireGofaIntent/failed:" + t.getMessage(), t);
        }
        return null;
    }

    public static void openBrowsable(FragmentActivity activity, String text2) {
        try {
            Intent gofaIntent = new Intent();
            gofaIntent.setAction(Intent.ACTION_VIEW);
            gofaIntent.setData(Uri.parse(text2));
            activity.startActivity(gofaIntent);
        } catch (Throwable t) {
            logger.debug(TAG, "/fireGofaIntent/" + t.getMessage(), t);
        }
    }

    public static List<String> checkAndFindGofaLinks(String text) {
        List<String> results = new ArrayList<>();
        if (StringUtils.isEmpty(text)) {
            logger.debug("/empty text to check");
            return results;
        }
        if (!text.contains(Prefs.GOFA_SCHEME)) {
            logger.trace("/copied text doesn't contain gofa");
            return results;
        }
        int i = text.indexOf(Prefs.GOFA_SCHEME);
        while (i >= 0) {
            int next = text.indexOf(Prefs.GOFA_SCHEME, i + 1);
            String found;
            if (next > 0) {
                found = checkGofaLink(text.substring(i, next));
            } else {
                //till end
                found = checkGofaLink(text.substring(i));
            }
            if (!StringUtils.isEmpty(found)) {
                results.add(found);
                logger.debug("/checkAndFindGofaLinks/found:" + found + ", new size " + results.size());
            }
            i = next;
        }
        return results;
    }

    public static String checkGofaLink(String splitByGofa) {
        Matcher m = GOFA_PATTERN.matcher(splitByGofa);
        if (m.find()) {
            String found = splitByGofa.substring(m.start(), m.end()).trim();
            logger.trace("/checkGofaLink/found:" + found + " in " + splitByGofa);
            return found;
        } else {
            logger.trace("/checkGofaLink/not found gofa in " + splitByGofa);
        }
        return null;
    }
}
