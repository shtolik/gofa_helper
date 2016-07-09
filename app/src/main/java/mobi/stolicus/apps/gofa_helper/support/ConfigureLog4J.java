package mobi.stolicus.apps.gofa_helper.support;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * /**
 * Created by shtolik on 02.09.2015.
 */
public class ConfigureLog4J {
    public static final String DIR_LOGS = "/logs/";

    public static void configure() {
/*        final LogConfigurator logConfigurator = new LogConfigurator();

        logConfigurator.setFileName(getLogsDir() + "gofa_helper.log");
        logConfigurator.setRootLevel(Level.DEBUG);

        // Set log level of a specific logger
//        logConfigurator.
        logConfigurator.setLevel("org.apache", Level.ALL);
        logConfigurator.setUseLogCatAppender(true);
        logConfigurator.setUseFileAppender(true);
        logConfigurator.setMaxFileSize(5 * 1024 * 1024);
        logConfigurator.configure();
        */
    }

    public static String getLogsDir(@NonNull Context context) {
        String dir;
        if (context.getExternalCacheDir() != null) {
            dir = context.getExternalCacheDir().getAbsolutePath();
        } else if (context.getCacheDir() != null) {
            dir = context.getCacheDir().getAbsolutePath();
        } else {
            dir = context.getFilesDir().getAbsolutePath();
        }

        dir = dir + ConfigureLog4J.DIR_LOGS;
        return dir;

    }
}
