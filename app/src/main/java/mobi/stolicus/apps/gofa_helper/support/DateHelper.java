package mobi.stolicus.apps.gofa_helper.support;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * date related
 * Created by shtolik on 02.09.2015.
 */
public class DateHelper {

    private static final String FORMAT_FILENAME_STRING = "yyyyMMddHHmmssSS";
    private static final String FORMAT_HUMAN_READABLE_STRING = "yyyy/MM/dd HH:mm:ss";

    public final static SimpleDateFormat FORMAT_FILENAME = new SimpleDateFormat(
            FORMAT_FILENAME_STRING, Locale.getDefault());

    public final static SimpleDateFormat FORMAT_HUMAN_READABLE = new SimpleDateFormat(
            DateHelper.FORMAT_HUMAN_READABLE_STRING, Locale.getDefault());
}
