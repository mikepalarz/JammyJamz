package com.palarz.mike.jammyjamz.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.RemoteViews;

import com.bumptech.glide.request.target.AppWidgetTarget;
import com.bumptech.glide.request.transition.Transition;
import com.palarz.mike.jammyjamz.GlideApp;
import com.palarz.mike.jammyjamz.R;
import com.palarz.mike.jammyjamz.activity.PostSearch;
import com.palarz.mike.jammyjamz.model.Post;

public class JammyJamzWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int numberOfWidgets = appWidgetIds.length;

        for (int i = 0; i < numberOfWidgets; i++){
            int appWidgetID = appWidgetIds[i];

            // Setting things up for the RemoteViewsService so that the ListView can start getting populated
            Intent intent = new Intent(context, JammyJamzRemoteViewsService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetID);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.app_widget);

            // Setting up the adapter and empty view
            remoteViews.setRemoteAdapter(R.id.app_widget_listview, intent);
            remoteViews.setEmptyView(R.id.app_widget_listview, R.id.app_widget_no_data_textview);

            // Setting up the on-click for the button
            Intent buttonIntent = new Intent(context, PostSearch.class);
            buttonIntent.putExtra(PostSearch.EXTRA_LAUNCH_DIALOG, true);
            PendingIntent buttonPendingIntent = PendingIntent.getActivity(context, 0, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.app_widget_button, buttonPendingIntent);

            appWidgetManager.updateAppWidget(appWidgetID, remoteViews);
        }
    }
}
