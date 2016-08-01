package com.softwaremobility.utilities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.softwaremobility.data.MoninContract;
import com.softwaremobility.data.MoninDataBase;
import com.softwaremobility.fragments.Login;
import com.softwaremobility.listeners.DownloadImageListener;
import com.softwaremobility.listeners.DownloadListener;
import com.softwaremobility.monin.R;
import com.softwaremobility.objects.Recipe;
import com.softwaremobility.preferences.MoninPreferences;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Utils {

    private static int currentDownloads = 0, totalDownloads = 0;
    private static DownloadListener listener;

    public static boolean isNetworkAvailable(Context context){
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null && info.isConnectedOrConnecting()){
            return (info.isConnected() && info.isAvailable());
        }else {
            return false;
        }
    }

    public static void logOut(final Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.confirmation));
        builder.setMessage(context.getString(R.string.message_confirmation_logout));
        builder.setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MoninPreferences.setString(context, MoninPreferences.SHAREDPREFERENCE_KEY.KEY_TOKEN, "");
                MoninPreferences.setLong(context, MoninPreferences.SHAREDPREFERENCE_KEY.KEY_TOKEN_EXPIRY, 0l);
                MoninPreferences.setString(context, MoninPreferences.SHAREDPREFERENCE_KEY.KEY_PROVIDER_LOGIN, "");
                MoninPreferences.setString(context, MoninPreferences.SHAREDPREFERENCE_KEY.KEY_USER_ID, "");
                Intent intent = new Intent(context, Login.class);
                context.startActivity(intent);
                ((Activity)context).finish();
            }
        });
        builder.setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    public static void showSimpleMessage(Context context, @Nullable String tittle, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if (tittle != null && tittle.length() > 0){
            builder.setTitle(tittle);
        }
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    public static void showSimpleMessageFinishing(final Context context, @Nullable String tittle, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if (tittle != null && tittle.length() > 0){
            builder.setTitle(tittle);
        }
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                ((Activity)context).finish();
            }
        });
        builder.create().show();
    }

    /**
     * This method will store the initial recipe without images, but triggered download images
     * @param recipe object that will be store in database
     */
    private static void prepareDataForStore(final Recipe recipe, final boolean isMoninRecipe,
                                            final boolean region, final Context context, final int current, final String term, final boolean isCommunity){
        final String TAG = "DownloadManagerMonin";
        ContentValues contentValues;
        final DownloadImageListener listener = new DownloadImageListener() {
            @Override
            public void onSuccessfullyDownloadImage(@Nullable final byte[] imageRecipe) {
                if (recipe.getFlagURL() != null && !recipe.getFlagURL().equalsIgnoreCase("null")) {
                    Glide.with(context).load(recipe.getFlagURL())
                            .asBitmap()
                            .into(new SimpleTarget<Bitmap>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {
                                @Override
                                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                    resource.compress(Bitmap.CompressFormat.PNG,50,stream);
                                    byte[] array = stream.toByteArray();
                                    //Log.d(TAG, "onSuccessFullyDownloadImage downloading flag image, Recipe: " + recipe.getDescription());
                                    onSuccessfullyDownloadAllImages(imageRecipe, array);
                                }
                            });
                }else {
                    onSuccessfullyDownloadAllImages(imageRecipe,null);
                    //Log.d(TAG, "onSuccessFullyDownloadAllImages storing data recipe no flag found Recipe: " + recipe.getDescription());
                }
            }

            @Override
            public void onSuccessfullyDownloadAllImages(@Nullable byte[] imageRecipe, @Nullable byte[] imageFlag) {
                storeRecipeInDataBase(recipe,imageRecipe,imageFlag, isMoninRecipe, region, current, context, term, isCommunity);
                Log.d(TAG, "onSuccessFullyDownloadAllImages storing data recipe, Recipe: " + recipe.getDescription());
            }
        };
        if (recipe.getImageURL() != null && !recipe.getImageURL().equalsIgnoreCase("null")) {
            Glide.with(context).load(recipe.getImageURL())
                    .asBitmap()
                    .into(new SimpleTarget<Bitmap>(com.bumptech.glide.request.target.Target.SIZE_ORIGINAL, com.bumptech.glide.request.target.Target.SIZE_ORIGINAL) {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            resource.compress(Bitmap.CompressFormat.PNG, 50, stream);
                            byte[] array = stream.toByteArray();
                            listener.onSuccessfullyDownloadImage(array);
                            stream.reset();
                            //Log.d(TAG, "onSuccessFullyDownloadImage downloading recipe image, Recipe: " + recipe.getDescription());
                        }
                    });
        }else if (recipe.getFlagURL() != null && !recipe.getFlagURL().equalsIgnoreCase("null")){
            Glide.with(context).load(recipe.getFlagURL())
                    .asBitmap()
                    .into(new SimpleTarget<Bitmap>(com.bumptech.glide.request.target.Target.SIZE_ORIGINAL, com.bumptech.glide.request.target.Target.SIZE_ORIGINAL) {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            resource.compress(Bitmap.CompressFormat.PNG,50,stream);
                            byte[] array = stream.toByteArray();
                            listener.onSuccessfullyDownloadAllImages(null, array);
                            //Log.d(TAG, "onSuccessFullyDownloadImage downloading flag image, Recipe: " + recipe.getDescription());
                        }
                    });
        }else {
            Log.d(TAG, "onSuccessFullyDownloadAllImages storing data recipe no flag and no image recipe found, Recipe: " + recipe.getDescription());
            storeRecipeInDataBase(recipe,null,null, isMoninRecipe, region, current, context,term, isCommunity);
        }
    }


    /**
     * This method stores the data in the dataBase
     * @param recipe object recived from newEntryRecipe
     * @param imageRecipe image in byte for recipe
     * @param imageFlag image in bytes for flag
     * @param isMoninRecipe
     */
    private static void storeRecipeInDataBase(Recipe recipe, @Nullable byte[] imageRecipe, @Nullable byte[] imageFlag,
                                                       boolean isMoninRecipe, boolean region, int current, Context context, String term, boolean isCommunity) {
        ContentValues registro = new ContentValues();
        registro.put(MoninContract.MoninEntry.Key_IdRecipe, recipe.getId());
        registro.put(MoninContract.MoninEntry.Key_Description, recipe.getDescription());
        registro.put(MoninContract.MoninEntry.Key_Rating, recipe.getRating());
        registro.put(MoninContract.MoninEntry.Key_IsAlcoholic, recipe.isAlcohol() ? MoninDataBase.BOOLEAN.TRUE.value : MoninDataBase.BOOLEAN.FALSE.value);
        registro.put(MoninContract.MoninEntry.Key_IsCoffee, recipe.isCofee() ? MoninDataBase.BOOLEAN.TRUE.value : MoninDataBase.BOOLEAN.FALSE.value);
        registro.put(MoninContract.MoninEntry.Key_IsMoninRecipe, isMoninRecipe ? MoninDataBase.BOOLEAN.TRUE.value : MoninDataBase.BOOLEAN.FALSE.value);
        registro.put(MoninContract.MoninEntry.Key_IsCommunity, isCommunity ? MoninDataBase.BOOLEAN.TRUE.value : MoninDataBase.BOOLEAN.FALSE.value);
        registro.put(MoninContract.MoninEntry.Key_Location, recipe.getLocation());
        registro.put(MoninContract.MoninEntry.Key_Date, System.currentTimeMillis());
        registro.put(MoninContract.MoninEntry.Key_Region, region ? MoninDataBase.BOOLEAN.TRUE.value : MoninDataBase.BOOLEAN.FALSE.value);
        registro.put(MoninContract.MoninEntry.Key_ImageRecipe, imageRecipe);
        registro.put(MoninContract.MoninEntry.Key_ImageFlag, imageFlag);
        registro.put(MoninContract.MoninEntry.Key_ImageUrl, recipe.getImageURL());
        Uri uri = MoninContract.MoninEntry.buildMoninWithRegion(region, recipe.isAlcohol(),
                recipe.isCofee(), !recipe.isAlcohol(), isMoninRecipe, term, isCommunity);
        context.getContentResolver().insert(uri, registro);
        context.getContentResolver().notifyChange(uri, null);
        currentDownloads++;
        if (currentDownloads == totalDownloads){
            listener.OnLoadingFinished();
        }
    }

    public static void storeRecipesAndRefreshLoader(final List<Recipe> recipes, final Context context,
                                                    boolean isMonin, final boolean isRegion, final Uri uri,
                                                    final int current, DownloadListener downloadListener){

        listener = downloadListener;
        String term = uri.getQueryParameter(MoninContract.MoninEntry.Key_SearchTerm);
        String isCommunityText = uri.getQueryParameter(MoninContract.MoninEntry.Key_IsCommunity);
        boolean isCommunity = convertStringToBoolean(isCommunityText);
        currentDownloads = 0;
        totalDownloads = recipes.size();
        for (Recipe recipe : recipes){
            prepareDataForStore(recipe,isMonin,isRegion,context, current, term, isCommunity);
        }
        if (totalDownloads == 0){
            listener.OnLoadingFinished();
        }
        String isAlcoholicS = uri.getQueryParameter(MoninContract.MoninEntry.Key_IsAlcoholic);
        String isNonAlcoholicS = uri.getQueryParameter(MoninContract.MoninEntry.Key_IsNonAlcoholic);
        String isCoffeeS = uri.getQueryParameter(MoninContract.MoninEntry.Key_IsCoffee);
        boolean isAlcoholic = convertStringToBoolean(isAlcoholicS);
        boolean isCoffee = convertStringToBoolean(isCoffeeS);
        boolean isNonAlcoholic = convertStringToBoolean(isNonAlcoholicS);
        MoninPreferences.setBoolean(context, MoninPreferences.SHAREDPREFERENCE_KEY.KEY_IS_ALCOHOLIC, isAlcoholic);
        MoninPreferences.setBoolean(context, MoninPreferences.SHAREDPREFERENCE_KEY.KEY_IS_COFFEE, isCoffee);
        MoninPreferences.setBoolean(context, MoninPreferences.SHAREDPREFERENCE_KEY.KEY_IS_NON_ALCOHOLIC, isNonAlcoholic);
        MoninPreferences.setBoolean(context, MoninPreferences.SHAREDPREFERENCE_KEY.KEY_FILTER_BY_REGION, isRegion);
        MoninPreferences.setInteger(context, isMonin ?
                MoninPreferences.SHAREDPREFERENCE_KEY.KEY_CURRENT_PAGE_MONIN :
                MoninPreferences.SHAREDPREFERENCE_KEY.KEY_CURRENT_PAGE_USER_RECIPES, current);
    }

    public static boolean convertStringToBoolean(String value){
        return Boolean.parseBoolean(value);
    }

    public static int convertBooleanToInt(boolean value){ return value ? 1 : 0;}

    public static ProgressDialog createSimpleProgressDialog(Context context, String title){
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setTitle(title);
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    public static Map<String,String> addRegionParameters(Context context, Map<String,String> params, @Nullable Location location, @Nullable boolean regionFilter){
        params.put(context.getString(R.string.key_country_code), Locale.getDefault().getCountry());
        params.put(context.getString(R.string.key_my_region),String.valueOf(convertBooleanToInt(regionFilter)));
        if (MoninPreferences.getBoolean(context, MoninPreferences.SHAREDPREFERENCE_KEY.KEY_GPS_ALLOWED)){
            if (location != null){
                params.put(context.getString(R.string.key_latitude).toLowerCase(),String.valueOf(location.getLatitude()));
                params.put(context.getString(R.string.key_longitude).toLowerCase(),String.valueOf(location.getLongitude()));
            }
        }else {
            params.put(context.getString(R.string.key_latitude).toLowerCase(),"0");
            params.put(context.getString(R.string.key_longitude).toLowerCase(),"0");
        }
        return params;
    }

    public static int getColor(boolean isAlcohol, boolean isCoffee){
        int color = 0;
        if (isAlcohol){
            color = R.color.alcoholic_drinks;
        }else if (isCoffee){
            color = R.color.cofees;
        }else {
            color = R.color.non_alcoholic_drinks;
        }
        return color;
    }

    /**
     * This method return two flags that represents isAlcoholic and isCoffee values
     * @param categoryId An Integer given by the server to represent the type of drink
     * @return A boolean array that index 0 is isAlcoholic and index 1 is isCoffee
     */
    public static boolean[] getFiltersFromCategory(int categoryId){
        boolean[] regreso = new boolean[2];
        switch (categoryId){
            case 1:
                regreso[0] = true;
                regreso[1] = false;
                break;
            case 2:
                regreso[0] = false;
                regreso[1] = false;
                break;
            case 3:
                regreso[0] = false;
                regreso[1] = true;
            break;
            default:
                regreso[0] = true;
                regreso[1] = true;
                break;
        }
        return regreso;
    }

    public static void hideKeyboard(Activity context){
        View view = context.getCurrentFocus();
        if (view != null){
            ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(view.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public static boolean containsDigit(String s) {
        boolean containsDigit = false;

        if (s != null && !s.isEmpty()) {
            for (char c : s.toCharArray()) {
                if (containsDigit = Character.isDigit(c)) {
                    break;
                }
            }
        }

        return containsDigit;
    }
}
