package com.softwaremobility.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static com.softwaremobility.utilities.Utils.convertBooleanToInt;
import static com.softwaremobility.utilities.Utils.convertStringToBoolean;

public class MoninProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MoninDataBase dataBase;

    static final int MONIN_RECIPES_WITH_REGION = 101;
    static final int USER_RECIPES_WITH_REGION = 201;
    static final int PEOPLE_WITH_REGION = 301;
    static final int GALLERY_WITH_SEARCH = 401;

    static UriMatcher buildUriMatcher(){
        // 1) The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case. Add the constructor below.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MoninContract.CONTENT_AUTHORITY;

        // 2) Use the addURI function to match each of the types.  Use the constants from
        // WeatherContract to help define the types to the UriMatcher.

        matcher.addURI(authority,MoninContract.PATH_MONIN_RECIPES + "/*", MONIN_RECIPES_WITH_REGION);
        matcher.addURI(authority,MoninContract.PATH_USER_RECIPES + "/*", USER_RECIPES_WITH_REGION);
        matcher.addURI(authority,MoninContract.PATH_PEOPLE + "/*", PEOPLE_WITH_REGION);
        matcher.addURI(authority,MoninContract.PATH_GALLERY + "/*", GALLERY_WITH_SEARCH);

        // 3) Return the new matcher!
        return matcher;
    }

    // isAlcoholic = ? AND isCoffee = ? AND isMoninRecipe = ? AND isCommunity = ?
    private static String sRecipesSelection =
                    MoninContract.MoninEntry.Key_IsAlcoholic + " = ? AND " +
                    MoninContract.MoninEntry.Key_IsCoffee + " = ? AND " +
                    MoninContract.MoninEntry.Key_IsMoninRecipe + " = ? AND " +
                    MoninContract.MoninEntry.Key_IsCommunity + " = ?";

    // isCoffee = ? AND isMoninRecipe = ? AND isCommunity = ?
    private static String sCoffeeRecipesSelection =
                    MoninContract.MoninEntry.Key_IsCoffee + " = ? AND " +
                    MoninContract.MoninEntry.Key_IsMoninRecipe + " = ? AND " +
                    MoninContract.MoninEntry.Key_IsCommunity + " = ?";

    // isCommunity = ?
    private static String communityRecipesFilterSelection =
            MoninContract.MoninEntry.Key_IsCommunity + " = ?";

    // isMoninRecipe = ? AND isCommunity = ?
    private static String defaultFilterSelection =
                    MoninContract.MoninEntry.Key_IsMoninRecipe + " = ? AND " +
                            MoninContract.MoninEntry.Key_IsCommunity + " = ?";

    @Override
    public boolean onCreate() {
        dataBase = new MoninDataBase(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        switch (sUriMatcher.match(uri)){
            case MONIN_RECIPES_WITH_REGION: {
                String isAlcoholicS = uri.getQueryParameter(MoninContract.MoninEntry.Key_IsAlcoholic);
                String isNonAlcoholicS = uri.getQueryParameter(MoninContract.MoninEntry.Key_IsNonAlcoholic);
                String isCoffeeS = uri.getQueryParameter(MoninContract.MoninEntry.Key_IsCoffee);
                String isMoninRecipesS = uri.getQueryParameter(MoninContract.MoninEntry.Key_IsMoninRecipe);
                String termSearch = uri.getQueryParameter(MoninContract.MoninEntry.Key_SearchTerm);
                String isCommunityText = uri.getQueryParameter(MoninContract.MoninEntry.Key_IsCommunity);
                boolean isAlcoholic = convertStringToBoolean(isAlcoholicS);
                boolean isCoffee = convertStringToBoolean(isCoffeeS);
                boolean isNonAlcoholic = convertStringToBoolean(isNonAlcoholicS);
                boolean isMoninRecipes = convertStringToBoolean(isMoninRecipesS);
                boolean isCommunity = convertStringToBoolean(isCommunityText);
                selection = defaultFilterSelection;
                selectionArgs = new String[]{String.valueOf(convertBooleanToInt(isMoninRecipes)),String.valueOf(convertBooleanToInt(isCommunity))};
                if (!(!isAlcoholic && !isCoffee && !isNonAlcoholic)) {
                    selection = sRecipesSelection;
                    selectionArgs = new String[]{String.valueOf(convertBooleanToInt(isAlcoholic)), String.valueOf(convertBooleanToInt(isCoffee)), String.valueOf(convertBooleanToInt(isMoninRecipes)),
                            String.valueOf(convertBooleanToInt(isCommunity))};
                }
                if (isAlcoholic || isNonAlcoholic){
                    selection = sRecipesSelection;
                    selectionArgs = new String[]{String.valueOf(convertBooleanToInt(isAlcoholic)),String.valueOf(convertBooleanToInt(false)), String.valueOf(convertBooleanToInt(isMoninRecipes)),String.valueOf(convertBooleanToInt(isCommunity))};
                }
                if (isCoffee){
                    selection = sCoffeeRecipesSelection;
                    selectionArgs = new String[]{String.valueOf(convertBooleanToInt(true)), String.valueOf(convertBooleanToInt(isMoninRecipes)),String.valueOf(convertBooleanToInt(isCommunity))};
                }
                if (isCommunity){
                    selection = communityRecipesFilterSelection;
                    selectionArgs = new String[]{String.valueOf(convertBooleanToInt(true))};
                }
                if (termSearch != null && termSearch.length() > 0){
                    if(selection == null){
                        selection = MoninContract.MoninEntry.Key_Description + " LIKE ?";
                        selectionArgs = new String[]{"%" + termSearch + "%"};
                    }else {
                        selection = selection + " AND " + MoninContract.MoninEntry.Key_Description + " LIKE ?";
                        String[] aux = new String[selectionArgs.length + 1];
                        for (int i = 0 ; i < selectionArgs.length ; i++){
                            aux[i] = selectionArgs[i];
                        }
                        aux[aux.length - 1] = "%" + termSearch + "%";
                        selectionArgs = aux;
                    }

                }
                return dataBase.getInstanceDataBase().query(MoninContract.MoninEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
            }
            case PEOPLE_WITH_REGION:{
                String termSearch = uri.getQueryParameter(MoninContract.PeopleEntry.Key_SearchTerm);
                if (termSearch != null && termSearch.length() > 0){
                    if(selection == null){
                        selection = MoninContract.PeopleEntry.Key_Name + " LIKE ?";
                        selectionArgs = new String[]{"%" + termSearch + "%"};
                    }else {
                        selection = selection + " AND " + MoninContract.PeopleEntry.Key_Name + " LIKE ?";
                        String[] aux = new String[]{selectionArgs[0],selectionArgs[1], "%" + termSearch + "%"};
                        selectionArgs = new String[]{aux[0],aux[1],aux[2]};
                    }

                }
                return dataBase.getInstanceDataBase().query(MoninContract.PeopleEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
            }
            case GALLERY_WITH_SEARCH:{
                String termSearch = uri.getQueryParameter(MoninContract.GalleryEntry.Key_SearchTerm);
                if (termSearch != null && termSearch.length() > 0){
                    if(selection == null){
                        selection = MoninContract.GalleryEntry.Key_Title + " LIKE ?";
                        selectionArgs = new String[]{"%" + termSearch + "%"};
                    }else {
                        selection = selection + " AND " + MoninContract.GalleryEntry.Key_Title + " LIKE ?";
                        String[] aux = new String[]{selectionArgs[0],selectionArgs[1], "%" + termSearch + "%"};
                        selectionArgs = new String[]{aux[0],aux[1],aux[2]};
                    }

                }
                return dataBase.getInstanceDataBase().query(MoninContract.GalleryEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
            }
            default: {
                return null;
            }
        }
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)){
            case MONIN_RECIPES_WITH_REGION:
                return MoninContract.MoninEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri returnURI;
        switch (sUriMatcher.match(uri)){
            case MONIN_RECIPES_WITH_REGION: {
                dataBase.getInstanceDataBase().beginTransaction();
                long _id = dataBase.getInstanceDataBase().insert(MoninContract.MoninEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnURI = MoninContract.MoninEntry.buildMoninUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                dataBase.getInstanceDataBase().setTransactionSuccessful();
                dataBase.getInstanceDataBase().endTransaction();
                break;
            }
            case PEOPLE_WITH_REGION: {
                dataBase.getInstanceDataBase().beginTransaction();
                long _id = dataBase.getInstanceDataBase().insert(MoninContract.PeopleEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnURI = MoninContract.PeopleEntry.buildPeopleUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                dataBase.getInstanceDataBase().setTransactionSuccessful();
                dataBase.getInstanceDataBase().endTransaction();
                break;
            }
            case GALLERY_WITH_SEARCH: {
                dataBase.getInstanceDataBase().beginTransaction();
                long _id = dataBase.getInstanceDataBase().insert(MoninContract.GalleryEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnURI = MoninContract.GalleryEntry.buildGalleryUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                dataBase.getInstanceDataBase().setTransactionSuccessful();
                dataBase.getInstanceDataBase().endTransaction();
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnURI;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int rowsDeleted;

        switch (sUriMatcher.match(uri)){
            case MONIN_RECIPES_WITH_REGION: {
                rowsDeleted = dataBase.getInstanceDataBase().delete(MoninContract.MoninEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case PEOPLE_WITH_REGION: {
                rowsDeleted = dataBase.getInstanceDataBase().delete(MoninContract.PeopleEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case GALLERY_WITH_SEARCH: {
                rowsDeleted = dataBase.getInstanceDataBase().delete(MoninContract.GalleryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if(rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MONIN_RECIPES_WITH_REGION: {
                dataBase.getInstanceDataBase().beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = dataBase.getInstanceDataBase().insert(MoninContract.MoninEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    dataBase.getInstanceDataBase().setTransactionSuccessful();
                } finally {
                    dataBase.getInstanceDataBase().endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }
            case PEOPLE_WITH_REGION: {
                dataBase.getInstanceDataBase().beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = dataBase.getInstanceDataBase().insert(MoninContract.PeopleEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    dataBase.getInstanceDataBase().setTransactionSuccessful();
                } finally {
                    dataBase.getInstanceDataBase().endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }
            case GALLERY_WITH_SEARCH: {
                dataBase.getInstanceDataBase().beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = dataBase.getInstanceDataBase().insert(MoninContract.GalleryEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    dataBase.getInstanceDataBase().setTransactionSuccessful();
                } finally {
                    dataBase.getInstanceDataBase().endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }
            default:
                return super.bulkInsert(uri, values);
        }
    }


}
