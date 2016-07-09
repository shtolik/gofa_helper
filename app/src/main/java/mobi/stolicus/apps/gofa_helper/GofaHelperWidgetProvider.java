package mobi.stolicus.apps.gofa_helper;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import mobi.stolicus.app.gofa_helper.R;

/**
 * For handling clicks on widget on home screen
 * Created by shtolik on 16.08.2015.
 */
public class GofaHelperWidgetProvider extends AppWidgetProvider {

    public static PendingIntent prepareGofaHelperIntent(Context context) {
        Intent intent = new Intent(context, GofaHelperActivity.class);
        intent.setAction(GofaHelperActivity.ACTION_WIDGET_CLICKED);
        return PendingIntent.getActivity(context, 0, intent, 0);
    }

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int appWidgetId : appWidgetIds) {
            // Create an Intent to launch Activity

            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.gofa_helper_widget);
            views.setOnClickPendingIntent(R.id.helper_widget, prepareGofaHelperIntent(context));

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

}