package mobi.stolicus.apps.gofa_helper.helpers;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Static helper methods for handling clipboard
 * Created by shtolik on 20.08.2015.
 */
public class ClipboardHelper {
    private static final Logger logger = LoggerFactory.getLogger(ClipboardHelper.class);
    private static final String TAG = ClipboardHelper.class.getSimpleName();

    @SuppressWarnings("deprecation")
    public static String getTextFromClipboard(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager cm = (android.text.ClipboardManager) context.
                    getSystemService(Context.CLIPBOARD_SERVICE);
            return cm.getText().toString();
        } else {
            final ClipboardManager cm = (ClipboardManager) context.
                    getSystemService(Context.CLIPBOARD_SERVICE);
            return getTextFromPrimaryClipboard(cm);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static String getTextFromPrimaryClipboard(ClipboardManager cm) {
        ClipData clipData = cm.getPrimaryClip();
        String currentTextInClipboard = null;
        if (clipData != null && clipData.getItemCount() > 0) {
            logger.debug("/onPrimaryClipChanged/clip changed, clipData: "
                    + clipData.getItemAt(0) + ", desc:" + clipData.getDescription());
            ClipData.Item item = clipData.getItemAt(0);

            if (item == null || item.getText() == null) {
                logger.debug("/onPrimaryClipChanged/item empty");
            } else {
                logger.debug("/onPrimaryClipChanged/item==" + item.toString());
                CharSequence cs = item.getText();
                currentTextInClipboard = cs.toString().trim();
            }
        }
        return currentTextInClipboard;
    }

    public static boolean checkIfClipboardListeningPossible(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    public static boolean copyToClipboard(Context context, String text) {
        try {
            int sdk = android.os.Build.VERSION.SDK_INT;
            if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
                android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context
                        .getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setText(text);
            } else {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context
                        .getSystemService(Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData
                        .newPlainText(text, text);
                clipboard.setPrimaryClip(clip);
            }
            return true;
        } catch (Exception e) {
            logger.warn("/copyToClipboard/" + e.getMessage(), e);
            return false;
        }
    }

}
