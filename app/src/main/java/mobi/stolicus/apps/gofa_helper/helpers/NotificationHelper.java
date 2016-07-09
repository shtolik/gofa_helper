package mobi.stolicus.apps.gofa_helper.helpers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spanned;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import mobi.stolicus.app.gofa_helper.R;
import mobi.stolicus.apps.gofa_helper.GofaHelperActivity;

/**
 * Class for helping to show notifications in notification area
 * Created by shtolik on 23.09.2015.
 */
public class NotificationHelper {

    private static final Logger logger = LoggerFactory.getLogger(NotificationHelper.class);
    private static final int GOFA_POST_NOTIFICATION_ID = 0;

    public static void sendNotification(Context context, String text) {
        List<String> list = new ArrayList<>();
        list.add(text);
        sendNotification(context, list);
    }

    public static void sendNotification(Context context, List<String> links) {
        if (links == null || links.size() <= 0) {
            logger.warn("/sendNotification/empty text give");
            return;
        }
        Intent intent = new Intent(context, GofaHelperActivity.class);

        String firstLink = links.get(0);
        intent.setData(Uri.parse(firstLink));
        intent.setAction(GofaHelperActivity.ACTION_NOTIFICATION_CLICKED);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

// build notification
// the addAction re-use the same intent to keep the example short
        BitmapDrawable d = (BitmapDrawable) ContextCompat.getDrawable(context, R.mipmap.ic_launcher);

        String text = "";//firstLink;
        for (String link : links) {
            if (!text.isEmpty())
                text += "\n";
            text += link;
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.notification_link_in_clipboard))
                .setContentText(text)

                .setContentIntent(pIntent)
                .setTicker(firstLink)
                .setAutoCancel(true);

        builder.setLargeIcon(d.getBitmap());
        builder.setSmallIcon(R.drawable.ic_stat_gofah);

        if (links.size() > 1) {
            NotificationCompat.InboxStyle inboxStyle =
                    new NotificationCompat.InboxStyle();

            // Sets a title for the Inbox style big view
            inboxStyle.setBigContentTitle(context.getString(R.string.notification_links_in_clipboard));

            // Moves events into the big view
            for (int i = 0; i < links.size(); i++) {
                Spanned result;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    result = Html.fromHtml(links.get(i), Html.FROM_HTML_MODE_LEGACY);
                } else {
                    //noinspection deprecation
                    result = Html.fromHtml(links.get(i));
                }
                inboxStyle.addLine(result);
            }
            // Moves the big view style object into the notification object.
            builder.setStyle(inboxStyle);
            builder.setNumber(links.size());
            builder.setSubText(context.getString(R.string.notification_hint_to_open_first_link));
        } else {
            //show new chats for it.
            builder.setContentText(firstLink);
            builder.setNumber(1);
            builder.setSubText(context.getString(R.string.notification_hint_to_open_link));
        }

        Notification notification = builder.build();

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(GOFA_POST_NOTIFICATION_ID, notification);

    }
}
