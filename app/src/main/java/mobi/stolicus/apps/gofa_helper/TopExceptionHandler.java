package mobi.stolicus.apps.gofa_helper;

/**
 * Class for handling exceptions and logs sending. Logs are not sent anywhere ATM.
 * Exceptions catch by google play/analytics
 * Created by shtolik on 02.09.2015.
 */

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.BatteryManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

import mobi.stolicus.apps.gofa_helper.support.ConfigureLog4J;
import mobi.stolicus.apps.gofa_helper.support.DateHelper;
import mobi.stolicus.apps.gofa_helper.support.FileUtils;

public class TopExceptionHandler implements Thread.UncaughtExceptionHandler {

    protected static final Logger logger = LoggerFactory.getLogger(TopExceptionHandler.class);

    private static final String TAG = TopExceptionHandler.class.getSimpleName();
    public static final String CRASH_FILE_NAME = "stack.trace";
    public static final String MANUALLY_TRIGGERED_REPORT_SENDING_FILE_NAME = "stack_manual.trace";

    private final Thread.UncaughtExceptionHandler defaultUEH;
    private Context context = null;

    public TopExceptionHandler(Context app) {
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        this.context = app.getApplicationContext();
    }

    public static void uncaughtException(boolean manuallyTriggeredReportSending, Context context, Throwable e) {
        logger.error(TAG + "/uncaughtException/" + e.getMessage(), e);
        StackTraceElement[] arr = e.getStackTrace();

        String updatedDate = DateHelper.FORMAT_HUMAN_READABLE.format(new Date(
                System.currentTimeMillis()));


        String report = "crashReportCreationDate:" + updatedDate + ":" + e.toString() + "\n\n";

        report += "--------- Stack trace ---------\n\n";
        for (StackTraceElement anArr : arr) {
            report += "    " + anArr.toString() + "\n";
        }
        report += "-------------------------------\n\n";

        // If the exception was thrown in a background thread inside
        // AsyncTask, then the actual exception can be found with getCause
        report += "--------- Cause ---------\n\n";
        Throwable cause = e.getCause();
        if (cause != null) {
            report += cause.toString() + "\n\n";
            arr = cause.getStackTrace();
            for (StackTraceElement anArr : arr) {
                report += "    " + anArr.toString() + "\n";
            }
        }
        report += "-------------------------------\n\n";
        report += "--------Versions ----------------\n\n";
        String current_version = getAppVersionFromSystem(context);
        report += current_version;

        report += "\n\n--------Battery level----------------\n\n";
        report += "/getBatteryLevel=" + getBatteryLevel(context);

        try {
            String logDir = ConfigureLog4J.getLogsDir(context);
            String crashName = CRASH_FILE_NAME;
            if (manuallyTriggeredReportSending)
                crashName = MANUALLY_TRIGGERED_REPORT_SENDING_FILE_NAME;
            FileUtils.saveLogTextsToFile(logDir, crashName, report, false);
        } catch (Exception ioe) {
            logger.error(TAG + "/uncaughtException/while handling uncaught exception:" + ioe.getMessage(), ioe);

        }
    }

    private static String getAppVersionFromSystem(Context context) {
        String appVersionName;

        try {
            appVersionName = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0).versionName;
            int appVersionCode = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0).versionCode;
            logger.info("/getAppVersionFromSystem/appVersion:" + appVersionName + ". code:"
                    + appVersionCode);
            return appVersionName;
        } catch (PackageManager.NameNotFoundException e) {
            logger.warn("/getAppVersionFromSystem/" + e.getMessage());
            return null;
        }
    }

    public static float getBatteryLevel(Context context) {
        Intent batteryIntent = context.getApplicationContext().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (batteryIntent == null)
            return -1f;
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        // Error checking that probably isn't needed but I added just in case.
        if (level == -1 || scale == -1) {
            return -1f;
        }

        return ((float) level / (float) scale) * 100.0f;
    }

    public void uncaughtException(Thread t, Throwable e) {
        TopExceptionHandler.uncaughtException(false, this.context, e);
        defaultUEH.uncaughtException(t, e);
    }


}
