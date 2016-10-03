package com.softwaremobility.widgets;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import com.softwaremobility.fragments.Detail;
import com.softwaremobility.fragments.MoninRecipes;
import com.softwaremobility.monin.R;
import com.softwaremobility.services.MoninWidgetIntentService;

/**
 * Implementation of App Widget functionality.
 */
public class MoninWidget extends AppWidgetProvider{


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {


        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            int layoutId = R.layout.monin_widget;
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),layoutId);

            Intent intentPrincipal = new Intent(context, com.softwaremobility.monin.MoninRecipes.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intentPrincipal, 0);
            remoteViews.setOnClickPendingIntent(R.id.logoWidget, pendingIntent);

            Intent clickIntentTemplate = new Intent(context, Detail.class);

            PendingIntent clickPendingIntentTemplate = TaskStackBuilder.create(context)
                    .addNextIntentWithParentStack(clickIntentTemplate)
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            remoteViews.setPendingIntentTemplate(R.id.listWidget, clickPendingIntentTemplate);
            remoteViews.setEmptyView(R.id.listWidget, R.id.emptyWidget);

            appWidgetManager.updateAppWidget(appWidgetId,remoteViews);

        }

    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (MoninRecipes.ACTION_DATA_UPDATED.equals(intent.getAction())){

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                    new ComponentName(context, getClass()));
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.listWidget);
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setRemoteAdapter(Context context, @NonNull final RemoteViews views) {

        views.setRemoteAdapter(R.id.listWidget,
                new Intent(context, MoninWidgetIntentService.class));

    }

    @SuppressWarnings("deprecation")
    private void setRemoteAdapterV11(Context context, @NonNull final RemoteViews views) {

        views.setRemoteAdapter(0, R.id.listWidget,
                new Intent(context, MoninWidgetIntentService.class));

    }
}

