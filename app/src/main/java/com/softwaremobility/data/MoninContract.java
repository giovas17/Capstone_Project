package com.softwaremobility.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;

public class MoninContract {

    public static final String CONTENT_AUTHORITY = "com.itexico.monin.app";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths
    public static final String PATH_MONIN_RECIPES = "monin";
    public static final String PATH_USER_RECIPES = "user";
    public static final String PATH_PEOPLE = "people";
    public static final String PATH_GALLERY = "gallery";

    public static final class MoninEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_MONIN_RECIPES).build();

        public static final Uri CONTENT_URI_USERS = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_USER_RECIPES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MONIN_RECIPES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MONIN_RECIPES;

        public static final String TABLE_NAME = "MoninRecipesInfo";
        /** --------------------------------- Table members -------------------------------------**/
        public static final String Key_IdRecipe = "id_recipe";
        public static final String Key_Description = "description";
        public static final String Key_Rating = "rating";
        public static final String Key_IsCoffee = "isCoffee";
        public static final String Key_IsAlcoholic = "isAlcoholic";
        public static final String Key_IsNonAlcoholic = "isNonAlcoholic";
        public static final String Key_SearchTerm = "searchTerm";
        public static final String Key_Location = "location";
        public static final String Key_ImageFlag = "flagCountry";
        public static final String Key_IsCommunity = "isCommunity";
        public static final String Key_ImageUrl = "imageRecipeUrl";
        public static final String Key_ImageRecipe = "imageRecipe";
        public static final String Key_IsMoninRecipe = "isMoninRecipe";
        public static final String Key_Region = "region";
        public static final String Key_Date = "dateInMillis";

        public static Uri buildMoninUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildMoninWithRegion(boolean region, boolean isAlcoholic, boolean isCoffee, boolean isNonAlcoholic, boolean isMoninRecipes, @Nullable String termToSearch, boolean isCommunity){
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(region))
                    .appendQueryParameter(Key_IsAlcoholic, String.valueOf(isAlcoholic))
                    .appendQueryParameter(Key_IsNonAlcoholic,String.valueOf(isNonAlcoholic))
                    .appendQueryParameter(Key_IsCoffee, String.valueOf(isCoffee))
                    .appendQueryParameter(Key_IsMoninRecipe, String.valueOf(isMoninRecipes))
                    .appendQueryParameter(Key_SearchTerm, termToSearch == null ? "" : termToSearch)
                    .appendQueryParameter(Key_IsCommunity, String.valueOf(isCommunity))
                    .build();
        }
    }

    public static final class DetailRecipeEntry implements BaseColumns {

        public static final String TABLE_NAME = "DetailRecipe";
        /** --------------------------------- Table members -------------------------------------**/
        public static final String Key_Id = "id_recipe";
        public static final String Key_FinalPortion = "final_portion";
        public static final String Key_ServingSize = "serving_size";
        public static final String Key_Instructions = "instructions";
        public static final String Key_Level = "level";
        public static final String Key_Glass = "glass";
        public static final String Key_IsFavorite = "is_favorite";
        public static final String Key_Ingredients = "ingredients";
        public static final String Key_Directions = "directions";
    }

    public static final class PeopleEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_PEOPLE).build();

        public static final String TABLE_NAME = "People";
        /** --------------------------------- Table members -------------------------------------**/
        public static final String Key_Id = "id_people";
        public static final String Key_Name = "name_people";
        public static final String Key_Photo = "photo";
        public static final String Key_RecipesCount = "recipes_count";
        public static final String Key_FavoritesCount = "favorites_count";
        public static final String Key_FollowingCount = "following_count";
        public static final String Key_FollowersCount = "followers_count";
        public static final String Key_PersonalStatement = "personal_statement";
        public static final String Key_FollowByMe = "follow_by_me";
        public static final String Key_SearchTerm = "searchTerm";

        public static Uri buildPeopleUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildPeopleUriWithRegion(@Nullable String termToSearch){
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(true))
                    .appendQueryParameter(Key_SearchTerm, termToSearch == null ? "" : termToSearch)
                    .build();
        }
    }

    public static final class GalleryEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_GALLERY).build();

        public static final String TABLE_NAME = "Gallery";
        /** --------------------------------- Table members -------------------------------------**/
        public static final String Key_Id = "id_gallery";
        public static final String Key_Title = "title";
        public static final String Key_Photo = "photo";
        public static final String Key_Description = "description";
        public static final String Key_CreatedTime = "date_created";
        public static final String Key_TemplateId = "template_type";
        public static final String Key_Active = "active";
        public static final String Key_SearchTerm = "searchTerm";

        public static Uri buildGalleryUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildGalleryUriWithSearch(@Nullable String termToSearch){
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(true))
                    .appendQueryParameter(Key_SearchTerm, termToSearch == null ? "" : termToSearch)
                    .build();
        }
    }

}
