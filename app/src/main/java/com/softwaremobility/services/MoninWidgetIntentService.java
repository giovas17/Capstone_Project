package com.softwaremobility.services;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.softwaremobility.data.MoninContract;
import com.softwaremobility.monin.R;
import com.softwaremobility.widgets.MoninWidget;

/**
 * Created by darkgeat on 9/8/16.
 */
public class MoninWidgetIntentService extends IntentService {

    private int appWidgetId;
    private AppWidgetManager appWidgetManager;

    public MoninWidgetIntentService() {
        super("MoninWidgetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        boolean region,isAlcoholic,isCoffee,isNonAlcoholic,isMoninRecipes,isCommunity;
        region = isAlcoholic = isCoffee = isNonAlcoholic = isCommunity = false;
        isMoninRecipes = true;

        Uri moninUri = MoninContract.MoninEntry.buildMoninWithRegion(region,isAlcoholic,isCoffee,isNonAlcoholic,isMoninRecipes,null,isCommunity);

        appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, MoninWidget.class));

        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {

        }

        CharSequence widgetText = getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(getPackageName(), R.layout.monin_widget);
        views.setTextViewText(R.id.appwidget_text, widgetText);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}
