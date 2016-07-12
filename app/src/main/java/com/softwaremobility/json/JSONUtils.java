package com.softwaremobility.json;

import android.content.Context;
import android.util.Log;

import com.softwaremobility.data.MoninContract;
import com.softwaremobility.data.MoninDataBase;
import com.softwaremobility.monin.R;
import com.softwaremobility.objects.Detail_Recipe;
import com.softwaremobility.objects.Direction;
import com.softwaremobility.objects.FractionOption;
import com.softwaremobility.objects.GlassObject;
import com.softwaremobility.objects.Ingredient;
import com.softwaremobility.objects.MeasureOption;
import com.softwaremobility.objects.Recipe;
import com.softwaremobility.preferences.MoninPreferences;
import com.softwaremobility.utilities.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JSONUtils {

    private static JSONUtils INSTANCE = null;
    private Context context;

    public JSONUtils(){}

    private static synchronized JSONUtils createInstance(){
        if (INSTANCE == null){
            INSTANCE = new JSONUtils();
        }
        return INSTANCE;
    }

    private static JSONUtils getInstance(){
        if (INSTANCE == null){ createInstance();}
        return INSTANCE;
    }

    public static JSONUtils with(Context c){
        getInstance().setContext(c);
        return getInstance();
    }

    private Context getContext() {
        return context;
    }

    private void setContext(Context context) {
        this.context = context;
    }

    public void updateToken(JSONObject obj, String provider){
        try {
            String userName = obj.getString(getContext().getString(R.string.key_userName));
            String token = obj.getString(getContext().getString(R.string.key_access_token));
            long expires_in = obj.getLong(context.getString(R.string.key_expires_in));
            String userId = obj.getString(getContext().getString(R.string.key_userid));
            String lastUser = MoninPreferences.getString(getContext(), MoninPreferences.SHAREDPREFERENCE_KEY.KEY_USER_NAME);
            if (!lastUser.equalsIgnoreCase(userName)){
                MoninDataBase db = new MoninDataBase(getContext());
                db.DeleteDataFromTable(MoninContract.MoninEntry.TABLE_NAME);
                db.DeleteDataFromTable(MoninContract.PeopleEntry.TABLE_NAME);
                db.DeleteDataFromTable(MoninContract.GalleryEntry.TABLE_NAME);
            }
            MoninPreferences.setString(context, MoninPreferences.SHAREDPREFERENCE_KEY.KEY_USER_NAME,userName);
            MoninPreferences.setString(context, MoninPreferences.SHAREDPREFERENCE_KEY.KEY_TOKEN,token);
            MoninPreferences.setLong(context, MoninPreferences.SHAREDPREFERENCE_KEY.KEY_TOKEN_EXPIRY, expires_in);
            MoninPreferences.setLong(context, MoninPreferences.SHAREDPREFERENCE_KEY.KEY_TOKEN_TIME, System.currentTimeMillis());
            MoninPreferences.setString(context, MoninPreferences.SHAREDPREFERENCE_KEY.KEY_PROVIDER_LOGIN, provider);
            MoninPreferences.setString(context, MoninPreferences.SHAREDPREFERENCE_KEY.KEY_USER_ID, userId);
        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    public String getErrorMessage(String response){
        try {
            JSONObject object = new JSONObject(response);
            return object.getString(context.getString(R.string.key_message));
        }catch (JSONException e){
            return e.getMessage();
        }
    }


    public String getErrorMessageDescription(String response){
        String errorMessage = null;
        try {
            JSONObject object = new JSONObject(response);
            JSONObject jsonObject = object.getJSONObject("ModelState");
            errorMessage = jsonObject.getString("Description");
            errorMessage = errorMessage.substring(1,(errorMessage.length()-1));
            return errorMessage;
        }catch (JSONException e){
            return e.getMessage();
        }
    }

    public List<Recipe> getRecipes(JSONArray array){
        List<Recipe> list = new ArrayList<>();
        for (int index = 0 ; index < array.length() ; index++){
            try {
                JSONObject object = array.getJSONObject(index);
                Recipe recipe = new Recipe();
                recipe.setId(object.getString(context.getString(R.string.key_id_recipe)));
                recipe.setDescription(object.getString(context.getString(R.string.key_description_recipe)));
                recipe.setAlcohol(object.getBoolean(context.getString(R.string.key_alcohol_recipe)));
                recipe.setCofee(object.getBoolean(context.getString(R.string.key_coffe_recipe)));
                String image = object.getString(context.getString(R.string.key_image_recipe));
                if (image != null) {
                    recipe.setImageURL(image);
                }
                recipe.setLocation(object.getString(context.getString(R.string.key_location_recipe)));
                recipe.setRating(object.getInt(context.getString(R.string.key_rating_recipe)));
                recipe.setFlagURL(object.getString(context.getString(R.string.key_flag_recipe)));
                list.add(recipe);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("JSONParser", e.getMessage());
            }
        }
        return list;
    }

    public List<Recipe> getFavoriteRecipes(JSONArray jsonArray){
        List<Recipe> list = new ArrayList<>();
        try {
            for (int index = 0; index < jsonArray.length(); index++) {

                JSONObject object = jsonArray.getJSONObject(index);
                Recipe recipe = new Recipe();
                recipe.setRating(object.getInt(context.getString(R.string.key_rating_user_recipe)));
                if(Integer.parseInt(object.getString(context.getString(R.string.key_id_user_recipe))) == 0){
                    recipe.setId(object.getString(context.getString(R.string.key_monin_recipe_id)));
                }else{
                    recipe.setId(object.getString(context.getString(R.string.key_id_user_recipe)));
                }
                boolean[] reg = Utils.getFiltersFromCategory(object.getInt(context.getString(R.string.key_category_type_id)));
                recipe.setAlcohol(reg[0]);
                recipe.setCofee(reg[1]);
                recipe.setDescription(object.getString(context.getString(R.string.key_description_user_recipe)));
                //have to sent it.
                //recipe.setAlcohol(object.getBoolean(context.getString(R.string.key_alcohol_recipe)));
                //recipe.setCofee(object.getBoolean(context.getString(R.string.key_coffe_recipe)));
                String image = object.getString(context.getString(R.string.key_image_user_recipe));
                if (image != null) {
                    recipe.setImageURL(image);
                }
                //recipe.setLocation(object.getString(context.getString(R.string.key_location_recipe)));
                //recipe.setRating(object.getInt(context.getString(R.string.key_rating_recipe)));
                recipe.setFlagURL(object.getString(context.getString(R.string.key_flag_recipe)));
                list.add(recipe);
            }
        } catch (JSONException e) {
        e.printStackTrace();
        }
        return list;
    }


    public List<Recipe> getUserRecipes(JSONArray array){
        List<Recipe> list = new ArrayList<>();
        for (int index = 0 ; index < array.length() ; index++){
            try {
                JSONObject object = array.getJSONObject(index);
                Recipe recipe = new Recipe();
                recipe.setDescription(object.getString(context.getString(R.string.key_description_user_recipe)));
                boolean[] reg = Utils.getFiltersFromCategory(object.getInt(context.getString(R.string.key_category_type_id)));
                recipe.setAlcohol(reg[0]);
                recipe.setCofee(reg[1]);
                recipe.setId(object.getString(context.getString(R.string.key_id_user_recipe)));
                String image = object.getString(context.getString(R.string.key_image_user_recipe));
                if (image != null) {
                    recipe.setImageURL(image);
                }
                //recipe.setLocation(object.getString(context.getString(R.string.key_location_recipe)));
                recipe.setRating(object.getInt(context.getString(R.string.key_rating_user_recipe)));
                recipe.setFlagURL(object.getString(context.getString(R.string.key_flag_user_recipe)));
                list.add(recipe);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("JSONParser", e.getMessage());
            }
        }
        return list;
    }

    public Detail_Recipe getDetailRecipe(String JSONStr){
        Detail_Recipe detail;
        try {
            JSONObject object = new JSONObject(JSONStr);
            String portion = object.getString(context.getString(R.string.key_final_portion));
            String glass = object.getString(context.getString(R.string.key_glass));
            boolean favorite = object.getBoolean(getContext().getString(R.string.key_is_favorite));
            int servingSize;
            if(!object.isNull(context.getString(R.string.key_serving_size))){
                servingSize = object.getInt(context.getString(R.string.key_serving_size));
            }else {
                servingSize = 0;
            }
            String instructions = object.getString(context.getString(R.string.key_instructions));
            String level = object.getString(context.getString(R.string.key_level));
            JSONArray ingredientsArray = object.getJSONArray(context.getString(R.string.key_array_ingredients));
            JSONArray directionsArray = object.getJSONArray(context.getString(R.string.key_directions));
            List<Ingredient> ingredients = new ArrayList<>();
            List<Direction> directions = new ArrayList<>();
            for (int i = 0; i < ingredientsArray.length() ; i++){
                Ingredient ingredient = new Ingredient();
                JSONObject ingredientItem = ingredientsArray.getJSONObject(i);
                ingredient.setAmount(ingredientItem.getDouble(context.getString(R.string.key_amount)));
                ingredient.setFraction(ingredientItem.getString(context.getString(R.string.key_fraction)));
                ingredient.setIngredient(ingredientItem.getString(context.getString(R.string.key_ingredient)));
                ingredient.setMeasure(ingredientItem.getString(context.getString(R.string.key_measure)));
                ingredients.add(ingredient);
            }
            for (int j = 0; j < directionsArray.length() ; j++){
                Direction direction = new Direction();
                JSONObject directionItem = directionsArray.getJSONObject(j);
                direction.setDirection(directionItem.getString(context.getString(R.string.key_direction)));
                direction.setSequence(directionItem.getInt(context.getString(R.string.key_sequence)));
                directions.add(direction);
            }
            detail = new Detail_Recipe(portion,servingSize,level,instructions,directions,ingredients, glass);
            detail.setFavorite(favorite);
            return detail;
        }catch (JSONException e){
            Log.e("JSONException ", e.toString());
            return null;
        }
    }

    public Detail_Recipe getDetailUserRecipe(String JSONStr){
        Detail_Recipe detail;
        try {
            JSONObject object = new JSONObject(JSONStr);
            String portion = object.getString(context.getString(R.string.key_final_portion));
            String glass = object.getString(context.getString(R.string.key_glass));
            int servingSize = object.getInt(context.getString(R.string.key_serving_size));
            //Don't return this variable on service
            //String instructions = object.getString(context.getString(R.string.key_instructions));
            int level = object.getInt(context.getString(R.string.key_user_level));
            JSONArray ingredientsArray = object.getJSONArray(context.getString(R.string.key_array_user_ingredients));
            JSONArray ingredientsMoninArray = object.getJSONArray(context.getString(R.string.key_array_ingredients_useds));
            JSONArray directionsArray = object.getJSONArray(context.getString(R.string.key_array_user_directions));
            List<Ingredient> ingredients = new ArrayList<>();
            List<Direction> directions = new ArrayList<>();
            //USER INGREDIENTS
            for (int i = 0; i < ingredientsArray.length() ; i++){
                Ingredient ingredient = new Ingredient();
                JSONObject ingredientItem = ingredientsArray.getJSONObject(i);
                ingredient.setMeasure(ingredientItem.getString(context.getString(R.string.key_measure_type)));
                ingredient.setFraction(ingredientItem.getString(context.getString(R.string.key_user_fraction)));
                ingredient.setMeasureId(ingredientItem.getString(context.getString(R.string.key_measure_id)));
                ingredient.setIngredient(ingredientItem.getString(context.getString(R.string.key_user_ingredient)));
                ingredient.setAmount(ingredientItem.getDouble(context.getString(R.string.key_amount)));
                if(!ingredientItem.isNull(context.getString(R.string.key_fraction_id)))
                    ingredient.setFractionID(String.valueOf(ingredientItem.getInt(context.getString(R.string.key_fraction_id))));
                //ingredient.setId(ingredientItem.getString(context.getString(R.string.key_user_ingredient_id)));
                ingredients.add(ingredient);
            }
            //DIRECTIONS
            for (int j = 0; j < directionsArray.length() ; j++){
                Direction direction = new Direction();
                JSONObject directionItem = directionsArray.getJSONObject(j);
                direction.setDirection(directionItem.getString(context.getString(R.string.key_user_directions)));
                direction.setSequence(directionItem.getInt(context.getString(R.string.key_user_sequence)));
                directions.add(direction);
            }
            //MONIN INGREDIENTS
            for (int i = 0; i < ingredientsMoninArray.length() ; i++){
                Ingredient ingredient = new Ingredient();
                JSONObject ingredientItem = ingredientsMoninArray.getJSONObject(i);
                if(ingredientItem.isNull(context.getString(R.string.key_amount)) ){
                    ingredient.setAmount(0.0);
                }else{
                    ingredient.setAmount(ingredientItem.getDouble(context.getString(R.string.key_amount)));
                }
                ingredient.setId(ingredientItem.getString(context.getString(R.string.key_monin_ingredient_id)));
                ingredient.setIngredient(ingredientItem.getString(context.getString(R.string.key_monin_ingredient)));
                ingredient.setMeasureId(ingredientItem.getString(context.getString(R.string.key_measure_id)));
                ingredient.setMeasure(ingredientItem.getString(context.getString(R.string.key_measure_type)));
                if(!ingredientItem.isNull(context.getString(R.string.key_fraction_id)))
                    ingredient.setFractionID(String.valueOf(ingredientItem.getInt(context.getString(R.string.key_fraction_id))));
                ingredient.setFraction(ingredientItem.getString(context.getString(R.string.key_user_fraction)));
                ingredients.add(ingredient);
            }
            detail = new Detail_Recipe(portion,servingSize,getLevel(level),"",directions,ingredients,glass);
            return detail;
        }catch (JSONException e){
            Log.e("JSONExpection",e.toString());
            return null;

        }
    }

    public ArrayList<Ingredient>  getMoninIngredients(String JSONStr){
        ArrayList<Ingredient> ingredients_arr;
        try {
            JSONObject object = new JSONObject(JSONStr);
            JSONArray ingredientsArray = object.getJSONArray(context.getString(R.string.key_monin_ingredients));
            ingredients_arr = new ArrayList<>();
            Ingredient ingredient;
            for (int i = 0; i < ingredientsArray.length() ; i++){
                ingredient = new Ingredient();
                JSONObject ingredientItem = ingredientsArray.getJSONObject(i);
                String nameOfIngredient = ingredientItem.getString(context.getString(R.string.key_monin_ingredients_name));
                String idOfIngredient = ingredientItem.getString(context.getString(R.string.key_monin_ingredients_id));
                ingredient.setIngredient(nameOfIngredient);
                ingredient.setId(idOfIngredient);
                ingredients_arr.add(ingredient);
            }
            return ingredients_arr;
        }catch (JSONException e){
            return null;
        }
    }

    public ArrayList<Ingredient>  addUserRecipe(String JSONStr){
        ArrayList<Ingredient> ingredients_arr;
        try {
            JSONObject object = new JSONObject(JSONStr);
            JSONArray ingredientsArray = object.getJSONArray(context.getString(R.string.key_monin_ingredients));
            ingredients_arr = new ArrayList<>();
            Ingredient ingredient;
            for (int i = 0; i < ingredientsArray.length() ; i++){
                ingredient = new Ingredient();
                JSONObject ingredientItem = ingredientsArray.getJSONObject(i);
                String nameOfIngredient = ingredientItem.getString(context.getString(R.string.key_monin_ingredients_name));
                String idOfIngredient = ingredientItem.getString(context.getString(R.string.key_monin_ingredients_id));
                ingredient.setIngredient(nameOfIngredient);
                ingredient.setId(idOfIngredient);
                ingredients_arr.add(ingredient);
            }
            return ingredients_arr;
        }catch (JSONException e){
            return null;
        }
    }

    public List<MeasureOption> getMeasureOptions(String JSONResponse){
        List<MeasureOption> options = new ArrayList<>();
        try{
            JSONObject obj = new JSONObject(JSONResponse);
            JSONArray measures_arr = obj.getJSONArray(context.getString(R.string.key_measures));
            for (int i = 0; i < measures_arr.length() ; i++){
                JSONObject object = measures_arr.getJSONObject(i);
                String text = object.getString(context.getString(R.string.key_measure));
                String id = object.getString(context.getString(R.string.key_measure_id));
                MeasureOption option = new MeasureOption(text,id);
                options.add(option);
            }
            return options;
        }catch (JSONException e){
            return null;
        }
    }

    public List<FractionOption> getFractionOptions(String JSONResponse){
        List<FractionOption> options = new ArrayList<>();
        try{
            JSONObject obj = new JSONObject(JSONResponse);
            JSONArray measures_arr = obj.getJSONArray(context.getString(R.string.key_fractions_array));
            for (int i = 0; i < measures_arr.length() ; i++){
                JSONObject object = measures_arr.getJSONObject(i);
                String text = object.getString(context.getString(R.string.key_fraction));
                String id = object.getString(context.getString(R.string.key_fraction_id));
                FractionOption option = new FractionOption(text,id);
                options.add(option);
            }
            return options;
        }catch (JSONException e){
            return null;
        }
    }


    public ArrayList<GlassObject> getGlasses(String JSONStr){
        ArrayList<GlassObject> items = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(JSONStr);
            for (int i = 0 ; i < array.length() ; i++){
                JSONObject jsonObject = array.getJSONObject(i);
                GlassObject object = new GlassObject(jsonObject.getString(getContext().getString(R.string.tag_glass_name)),
                        jsonObject.getString(getContext().getString(R.string.tag_glass_id)));
                items.add(object);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return items;
    }

    public JSONObject getNewUserRecipeJSON(String id, String description, int levelId, int finalPortion, int servingSize, boolean imperialMetric,
                                           int categoryID, List<Ingredient> ingredients, List<Direction> directions, String idGlass){
        JSONObject jsonRecipeJSON = null;
        JSONArray arrayUserIngredients;
        JSONArray arrayMoninIngredients;
        JSONArray arrayUserDirections;

        try{
            arrayUserIngredients = new JSONArray();
            arrayMoninIngredients = new JSONArray();
            arrayUserDirections = new JSONArray();
            jsonRecipeJSON = new JSONObject();
            jsonRecipeJSON.put(context.getString(R.string.key_id_user_recipe), id);
            jsonRecipeJSON.put(context.getString(R.string.key_description_user_recipe), description);
            jsonRecipeJSON.put(context.getString(R.string.key_user_level), levelId);
            jsonRecipeJSON.put(context.getString(R.string.key_final_portion), finalPortion);
            jsonRecipeJSON.put(context.getString(R.string.key_serving_size),servingSize);
            jsonRecipeJSON.put(context.getString(R.string.key_is_imperial),imperialMetric);
            jsonRecipeJSON.put(getContext().getString(R.string.key_id_glass),idGlass);
            jsonRecipeJSON.put(context.getString(R.string.key_category_type_id),categoryID);

            for (int i = 0; i < ingredients.size(); i++) {
                JSONObject jsonObject;
                if(ingredients.get(i).getId().equals("") || ingredients.get(i).getId() == null || ingredients.get(i).getId().equals("null")){
                    //USER INGREDIENTS
                    jsonObject = new JSONObject();
                    jsonObject.put(context.getString(R.string.key_user_ingredients_id), null);
                    jsonObject.put(context.getString(R.string.key_user_ingredient), ingredients.get(i).getIngredient());
                    jsonObject.put(context.getString(R.string.key_measure_id), ingredients.get(i).getMeasureId());
                    jsonObject.put(context.getString(R.string.key_amount), ingredients.get(i).getAmount());
                    if (!ingredients.get(i).getFractionID().equalsIgnoreCase("0")) {
                        jsonObject.put(context.getString(R.string.key_fraction_id), ingredients.get(i).getFractionID());
                    }else {
                        jsonObject.put(context.getString(R.string.key_fraction_id), null);
                    }
                    arrayUserIngredients.put(jsonObject);
                }else{
                    //MONIN INGREDIENTS
                    jsonObject = new JSONObject();
                    jsonObject.put(context.getString(R.string.key_monin_ingredients_used_id), null);
                    jsonObject.put(context.getString(R.string.key_monin_ingredients_ID), ingredients.get(i).getId());
                    jsonObject.put(context.getString(R.string.key_measure_id), ingredients.get(i).getMeasureId());
                    jsonObject.put(context.getString(R.string.key_amount),ingredients.get(i).getAmount());
                    if (!ingredients.get(i).getFractionID().equalsIgnoreCase("0")) {
                        jsonObject.put(context.getString(R.string.key_fraction_id), ingredients.get(i).getFractionID());
                    }else {
                        jsonObject.put(context.getString(R.string.key_fraction_id), null);
                    }
                    arrayMoninIngredients.put(jsonObject);
                }
            }
            //USER DIRECTIONS
            for (int i = 0; i < directions.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(context.getString(R.string.key_user_direction_id), null);
                jsonObject.put(context.getString(R.string.key_user_sequence), directions.get(i).getSequence());
                jsonObject.put(context.getString(R.string.key_user_directions), directions.get(i).getDirection());
                arrayUserDirections.put(jsonObject);
            }

            jsonRecipeJSON.put(context.getString(R.string.key_array_user_ingredients),arrayUserIngredients);
            jsonRecipeJSON.put(context.getString(R.string.key_array_ingredients_useds),arrayMoninIngredients);
            jsonRecipeJSON.put(context.getString(R.string.key_array_user_directions),arrayUserDirections);

        }catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonRecipeJSON;
    }

    public JSONObject getChangePasswordJSONObject(String oldPassword, String newPassword, String confirmPassword){
        JSONObject jsonObject = null;

        try{
            jsonObject = new JSONObject();
            jsonObject.put(context.getString(R.string.key_new_password),newPassword);
            jsonObject.put(context.getString(R.string.key_old_password),oldPassword);
            jsonObject.put(context.getString(R.string.key_confirm_password),newPassword);
        }catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private String getLevel(int level){
        if (level == 1){
            return getContext().getString(R.string.level_easy);
        }
        if (level == 2) {
            return getContext().getString(R.string.level_medium);
        }
        if (level == 3){
            return getContext().getString(R.string.level_difficult);
        }
        return null;
    }

}
