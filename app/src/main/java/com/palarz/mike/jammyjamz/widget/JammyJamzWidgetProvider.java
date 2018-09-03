package com.palarz.mike.jammyjamz.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.palarz.mike.jammyjamz.R;
import com.palarz.mike.jammyjamz.activity.PostSearch;

public class JammyJamzWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int numberOfWidgets = appWidgetIds.length;

        for (int i = 0; i < numberOfWidgets; i++){
            int appWidgetID = appWidgetIds[i];

            // Creating a PendingIntent to launch PostSearch
            Intent intent = new Intent(context, PostSearch.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            // Setting an onClickListener to the button within the widget
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.app_widget);
            remoteViews.setOnClickPendingIntent(R.id.app_widget_button, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetID, remoteViews);
        }
    }
}
