package com.softwaremobility.widgets;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.softwaremobility.fragments.MoninRecipes;
import com.softwaremobility.monin.R;
import com.softwaremobility.services.MoninWidgetIntentService;

/**
 * Implementation of App Widget functionality.
 */
public class MoninWidget extends AppWidgetProvider{

    static void updateAppWidget(Context context) {
        context.startService(new Intent(context, MoninWidgetIntentService.class));
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        updateAppWidget(context);

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
            context.startService(new Intent(context, MoninWidgetIntentService.class));
        }
    }
}

