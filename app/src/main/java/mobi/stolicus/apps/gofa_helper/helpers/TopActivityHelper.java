package mobi.stolicus.apps.gofa_helper.helpers;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import mobi.stolicus.app.gofa_helper.R;

/**
 * Class for checking if application is running in foreground
 * Created by shtolik on 20.08.2015.
 */
public class TopActivityHelper {
    private static final Logger logger = LoggerFactory.getLogger(TopActivityHelper.class);

    public static String getCurrentTopActivity(Context act) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return getCurrentRunningTasks(act);
        } else {
            if (!needPermissionForBlocking(act)) {
                return getTopPackage(act);
            } else {
                SharedPreferences sp = Prefs.getPrefs(act);
                String complain = act.getString(R.string.ask_to_set_permission);
                sp.edit().putBoolean(Prefs.SET_OBSERVE_CLIPBOARD, false).apply();
                Toast.makeText(act.getApplicationContext(), complain, Toast.LENGTH_LONG).show();
                try {
                    Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                    act.startActivity(intent);
                } catch (Throwable t) {
                    logger.warn(t.getMessage(), t);
                }
                return null;
            }
        }
    }

    @SuppressWarnings("deprecation")
    public static String getCurrentRunningTasks(Context act) {
        try {
            ActivityManager am = (ActivityManager) act.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            logger.debug("/getCurrentTopActivity/CURRENT Activity ::" + taskInfo.get(0).topActivity.getClassName());
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            return componentInfo.getPackageName();
        } catch (Throwable t) {
            logger.warn("/getCurrentTopActivity/failed" + t.getMessage(), t);
        }
        return null;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    public static String getTopPackage(Context act) {

        long ts = System.currentTimeMillis();

        UsageStatsManager mUsageStatsManager = (UsageStatsManager) act.getSystemService(Context.USAGE_STATS_SERVICE);
        List<UsageStats> usageStats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, ts - 2000, ts);

        if (usageStats == null) {
            logger.trace("/getTopPackage/usageStats == null");
            return null;
        }
        if (usageStats.size() == 0) {
            logger.trace("/getTopPackage/usageStats.size() == 0");
            return null;
        }
        RecentUseComparator mRecentComp = new RecentUseComparator();
        Collections.sort(usageStats, mRecentComp);

        return usageStats.get(0).getPackageName();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean needPermissionForBlocking(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName);
            if (mode == AppOpsManager.MODE_ALLOWED){
                return false;
            } else return mode != AppOpsManager.MODE_DEFAULT;
        } catch (PackageManager.NameNotFoundException e) {
            logger.warn("" + e.getMessage(), e);
            return true;
        }
    }

    private static class RecentUseComparator implements Comparator<UsageStats> {

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public int compare(UsageStats lhs, UsageStats rhs) {
            return (lhs.getLastTimeUsed() > rhs.getLastTimeUsed()) ? -1 : (lhs.getLastTimeUsed() == rhs.getLastTimeUsed()) ? 0 : 1;
        }
    }
}
