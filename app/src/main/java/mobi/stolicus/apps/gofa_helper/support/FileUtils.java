package mobi.stolicus.apps.gofa_helper.support;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

/**
 * Helper for handling file related ops
 * Created by shtolik on 02.09.2015.
 */
public class FileUtils {

    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    public static boolean saveLogTextsToFile(String logDir, String crashName, String report, boolean replace) {

        FileOutputStream trace = null;
        logger.debug("/saveLogTextsToFile/logDir=" + logDir + ", crashName=" + crashName +
                ", report=" + report + ", replace=" + replace);
        File crashFile = new File(logDir, crashName);
        try {
            if (crashFile.exists()) {
                if (replace) {
                    crashFile.delete();
                } else {
                    //rename old. should we just delete old?
                    long last = crashFile.lastModified();
                    String renameCrashName = "old_" + crashFile.getName()
                            + DateHelper.FORMAT_FILENAME.format(new Date(last));
                    crashFile.renameTo(new File(logDir, renameCrashName));
                    crashFile = new File(logDir, crashName);
                }
            }

            if (!crashFile.exists()) {

                crashFile.createNewFile();

            }
            trace = new FileOutputStream(crashFile);
            trace.write(report.getBytes());
            trace.close();
            return true;
        } catch (IOException e) {
            logger.warn("/saveLogTextsToFile/" + crashFile.getPath(), e);
        } finally {
            if (trace != null) {
                try {
                    trace.close();
                } catch (IOException e) {
                    logger.warn("/saveLogTextsToFile/", e);
                }
            }
        }
        return false;
    }

}
