package com.softwaremobility.services;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.support.v4.app.TaskStackBuilder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.softwaremobility.adapters.MoninListAdapter;
import com.softwaremobility.data.MoninContract;
import com.softwaremobility.fragments.Detail;
import com.softwaremobility.monin.MoninRecipes;
import com.softwaremobility.monin.R;
import com.softwaremobility.widgets.MoninWidget;

import static com.softwaremobility.utilities.Utils.convertBooleanToInt;
import static com.softwaremobility.utilities.Utils.convertStringToBoolean;

/**
 * Created by darkgeat on 9/8/16.
 */
public class MoninWidgetIntentService extends RemoteViewsService {

    private int appWidgetId;
    private AppWidgetManager appWidgetManager;
    private static final String[] DB_COLUMNS = new String[]{
            MoninContract.MoninEntry.Key_IdRecipe,
            MoninContract.MoninEntry.Key_Description,
            MoninContract.MoninEntry.Key_Location,
            MoninContract.MoninEntry.Key_Rating,
            MoninContract.MoninEntry.Key_Date,
            MoninContract.MoninEntry.Key_IsMoninRecipe,
            MoninContract.MoninEntry.Key_IsCoffee,
            MoninContract.MoninEntry.Key_IsAlcoholic,
            MoninContract.MoninEntry.Key_ImageFlag,
            MoninContract.MoninEntry.Key_ImageRecipe,
            MoninContract.MoninEntry.Key_ImageUrl
    };
    private static String sRecipesSelection =
            MoninContract.MoninEntry.Key_IsAlcoholic + " = ? AND " +
            MoninContract.MoninEntry.Key_IsCoffee + " = ? AND " +
            MoninContract.MoninEntry.Key_IsMoninRecipe + " = ? AND " +
            MoninContract.MoninEntry.Key_IsCommunity + " = ?";


    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {

            private Cursor cursor = null;

            @Override
            public void onCreate() {

            }

            @Override
            public void onDataSetChanged() {
                if (cursor != null){
                    cursor.close();
                }

                final long identityToken = Binder.clearCallingIdentity();
                boolean region,isAlcoholic,isCoffee,isNonAlcoholic,isMoninRecipes,isCommunity;
                region = isAlcoholic = isCoffee = isNonAlcoholic = isCommunity = false;
                isMoninRecipes = true;

                Uri moninUri = MoninContract.MoninEntry.buildMoninWithRegion(region,isAlcoholic,isCoffee,isNonAlcoholic,isMoninRecipes,null,isCommunity);

                String[] selectionArgs = new String[]{String.valueOf(convertBooleanToInt(isAlcoholic)), String.valueOf(convertBooleanToInt(isCoffee)), String.valueOf(convertBooleanToInt(isMoninRecipes)),
                        String.valueOf(convertBooleanToInt(isCommunity))};

                cursor = getContentResolver().query(moninUri,DB_COLUMNS,sRecipesSelection,selectionArgs,null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (cursor != null){
                    cursor.close();
                    cursor = null;
                }
            }

            @Override
            public int getCount() {
                return cursor == null ? 0 : cursor.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION || cursor == null ||
                        !cursor.moveToPosition(position)){
                    return null;
                }
                RemoteViews remoteViews = new RemoteViews(getPackageName(),R.layout.item_list_recipes);


                return remoteViews;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(),R.layout.monin_widget);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
