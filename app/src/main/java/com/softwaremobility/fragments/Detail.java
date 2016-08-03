package com.softwaremobility.fragments;


import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.softwaremobility.data.MoninContract;
import com.softwaremobility.data.MoninDataBase;
import com.softwaremobility.dialogs.ProgressDialog;
import com.softwaremobility.dialogs.SimpleCustomDialog;
import com.softwaremobility.json.JSONUtils;
import com.softwaremobility.monin.R;
import com.softwaremobility.network.Connection;
import com.softwaremobility.network.NetworkConnection;
import com.softwaremobility.objects.Detail_Recipe;
import com.softwaremobility.objects.Direction;
import com.softwaremobility.objects.Ingredient;
import com.softwaremobility.objects.Share;
import com.softwaremobility.preferences.MoninPreferences;
import com.softwaremobility.utilities.MediaUtilities;
import com.softwaremobility.utilities.ShareUtils;
import com.softwaremobility.utilities.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import static com.softwaremobility.utilities.Utils.getColor;

public class Detail extends Fragment implements NetworkConnection.ResponseListener {

    public static final int EDIT_RECIPE = 22;
    private static String TAG = Detail.class.getSimpleName();
    private String name, id;
    private int num_servings = 1;
    private TextView servingsButton, ingredientsText, complexity, glass, units;
    private byte[] imageRecipe, imageFlag;
    private Detail_Recipe recipe;
    private Typeface typeface;
    private MoninDataBase dataBase;
    private boolean isAlcoholic, isCoffee, isMoninRecipe;
    private boolean isFavorite = false, isImperialAllowed = true;
    private Cursor cursor = null;
    private ProgressDialog dialog;
    private ViewGroup containerDirections;
    private String imageUrl;
    private String localPath;
    private ImageView favorite;
    private TextView nameRecipeText;
    private ImageView imageRecipeDetail, imageFlagRecipeDetail, imageBadge, share;
    private boolean isImperial = true;
    private String[] unitsTypes;
    private SimpleCustomDialog dialogSimple;
    private StringBuilder contentShare;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        contentShare = new StringBuilder();
        unitsTypes = getActivity().getResources().getStringArray(R.array.units_creation_recipe);
        name = getActivity().getIntent().getStringExtra(MoninContract.MoninEntry.Key_Description);
        id = getActivity().getIntent().getStringExtra(MoninContract.MoninEntry.Key_IdRecipe);
        imageRecipe = getActivity().getIntent().getByteArrayExtra(MoninContract.MoninEntry.Key_ImageRecipe);
        imageUrl = getActivity().getIntent().getStringExtra(MoninContract.MoninEntry.Key_ImageUrl);
        imageFlag = getActivity().getIntent().getByteArrayExtra(MoninContract.MoninEntry.Key_ImageFlag);
        isAlcoholic = getActivity().getIntent().getBooleanExtra(MoninContract.MoninEntry.Key_IsAlcoholic, false);
        isCoffee = getActivity().getIntent().getBooleanExtra(MoninContract.MoninEntry.Key_IsCoffee, false);
        isMoninRecipe = getActivity().getIntent().getBooleanExtra(MoninContract.MoninEntry.Key_IsMoninRecipe, false);
        isImperialAllowed = MoninPreferences.getBoolean(getContext(), MoninPreferences.SHAREDPREFERENCE_KEY.KEY_IS_IMPERIAL);
        Log.d(TAG, "idRecipe: " + id);
        typeface = Typeface.createFromAsset(getActivity().getAssets(), getString(R.string.font_path));
        if (imageRecipe != null) {
            new SavePhotoTask().execute(imageRecipe);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_detail, container, false);

        nameRecipeText = (TextView) v.findViewById(R.id.textDescriptionDetail);
        glass = (TextView)v.findViewById(R.id.textGlassDetail);
        imageRecipeDetail = (ImageView) v.findViewById(R.id.imageRecipeDetail);
        imageFlagRecipeDetail = (ImageView) v.findViewById(R.id.imageFlagRecipeDetail);
        imageBadge = (ImageView) v.findViewById(R.id.imageBadgeRecipeDetail);
        share = (ImageView) v.findViewById(R.id.imageShareRecipeDetail);
        favorite = (ImageView) v.findViewById(R.id.imageFavoriteRecipeDetail);
        containerDirections = (ViewGroup) v.findViewById(R.id.containerDirectionsCreation);
        servingsButton = (TextView) v.findViewById(R.id.buttonServings);
        ingredientsText = (TextView) v.findViewById(R.id.textIngredientsDetail);

        /*servingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                num_servings++;
                updateUI();
            }
        });*/
        complexity = (TextView) v.findViewById(R.id.buttonComplexity);
        units = (TextView) v.findViewById(R.id.buttonUnits);

        refreshViews();

        return v;
    }

    private void refreshViews(){
        dataBase = new MoninDataBase(getActivity());
        cursor = dataBase.getRecipe(id);

        dialog = new ProgressDialog(getActivity(), getString(R.string.please_wait));
        dialog.show();

        nameRecipeText.setBackgroundColor(getResources().getColor(getColor(isAlcoholic, isCoffee)));
        nameRecipeText.setTypeface(typeface);
        nameRecipeText.setText(name);

        if (imageRecipe != null && imageRecipe.length > 0){
            Glide.with(getContext()).load(imageRecipe).into(imageRecipeDetail);
        }else {
            Glide.with(getContext()).load(imageUrl).into(imageRecipeDetail);
        }
        Glide.with(getActivity()).load(imageFlag).override(60, 40).into(imageFlagRecipeDetail);
        if (!isMoninRecipe) {
            imageBadge.setVisibility(View.GONE);
        }
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    final Uri localImageUri = Uri.fromFile(new File(localPath));
                    List<Share> apps = new ArrayList<>();
                    apps.add(new Share(R.drawable.settings_facebook_icon,"Facebook",ShareUtils.FACEBOOK));
                    apps.add(new Share(R.drawable.settings_instagram_icon,"Instagram",ShareUtils.INSTAGRAM));
                    apps.add(new Share(R.drawable.settings_twitter_icon,"Twitter", ShareUtils.TWITTER));
                    apps.add(new Share(R.drawable.settings_weibo_icon,"Weibo",ShareUtils.WEIBO));
                    apps.add(new Share(R.drawable.settings_googleplus_icon,"Google+",ShareUtils.GOOGLE_PLUS));
                    apps.add(new Share(R.drawable.ic_wechat,"WeChat",ShareUtils.WECHAT));
                    ShareUtils shareUtils = new ShareUtils(getContext(),getActivity());
                    shareUtils.newShareDialog(name,Uri.parse(imageUrl),Uri.parse(contentShare.toString()),localImageUri,apps);
                }catch (NullPointerException e){
                    e.printStackTrace();
                }
            }
        });
        favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MoninPreferences.getBoolean(getContext(), MoninPreferences.SHAREDPREFERENCE_KEY.KEY_IS_NO_LOGIN)){
                    SimpleCustomDialog dialog = new SimpleCustomDialog(getContext(), getString(R.string.error_title_no_login), getString(R.string.error_no_login), new SimpleCustomDialog.okListener() {
                        @Override
                        public void OnOkSelected() {
                            com.softwaremobility.fragments.MoninRecipes.shouldFinish = true;
                            Home.shouldFinish = true;
                            getActivity().finish();
                        }

                        @Override
                        public void OnCancelSelected() {

                        }
                    });
                    dialog.setOkButtonText(getString(R.string.action_login));
                    dialog.show();
                }else {
                    dialog = new ProgressDialog(getContext(),getString(R.string.please_wait));
                    dialog.show();
                    isFavorite = !isFavorite;
                    com.softwaremobility.fragments.MoninRecipes.shouldLoadAgain = true;
                    favorite.setImageResource(isFavorite ? R.drawable.details_fav_icon_pressed : R.drawable.details_fav_icon);
                    Uri uri = Uri.parse(getString(isMoninRecipe ? R.string.api_monin_recipes_favorites : R.string.api_user_recipe_favorites));
                    Map<String, String> headers = new HashMap<>();
                    headers.put(getString(R.string.header_authorization), getString(R.string.header_token,
                            MoninPreferences.getString(getContext(), MoninPreferences.SHAREDPREFERENCE_KEY.KEY_TOKEN)));
                    headers.put(getString(R.string.key_content_type), getString(R.string.header_json));
                    JSONObject object = new JSONObject();
                    if (isMoninRecipe) {
                        try {
                            object.put(getString(R.string.key_id_monin_recipe_favorite), id);
                            object.put(getString(R.string.key_created_date_time_unix), System.currentTimeMillis());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        uri = uri.buildUpon().appendQueryParameter(getString(R.string.key_id_user_recipe_favorite), id).build();
                    }
                    NetworkConnection.with(getContext()).withListener(new NetworkConnection.ResponseListener() {
                        @Override
                        public void onSuccessfullyResponse(String response) {
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(MoninContract.DetailRecipeEntry.Key_IsFavorite, isFavorite);
                            MoninDataBase db = new MoninDataBase(getContext());
                            db.updateDetailRecipe(id, contentValues);
                            db.updateFavoriteInMoninRecipes(id, isFavorite);
                            if (dialog.isShowing()){
                                dialog.dismiss();
                                dialog = new ProgressDialog(getActivity(), getString(R.string.please_wait));
                            }
                        }

                        @Override
                        public void onErrorResponse(String error, String message, int code) {

                        }
                    }).doRequest(Connection.REQUEST.POST, uri, null, headers, isMoninRecipe ? object : null);
                }
            }
        });
        units.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isImperial = !isImperial;
                updateUI();
            }
        });

        if (cursor != null && cursor.getCount() > 0) {
            recipe = createDetailFromCursor(cursor);
            updateUI();
            if (!isImperialAllowed && isImperial){
                units.performClick();
            }
        } else {
            Uri uri;
            Map<String, String> headers = new HashMap<>();
            if (isMoninRecipe){
                boolean isNoLogin = MoninPreferences.getBoolean(getContext(), MoninPreferences.SHAREDPREFERENCE_KEY.KEY_IS_NO_LOGIN);
                if (isNoLogin){
                    uri = Uri.parse(getString(R.string.api_monin_recipes) + "/" + id + "/open");
                    headers.put(getString(R.string.header_authorization), getString(R.string.header_generic_token,
                            MoninPreferences.getString(getContext(), MoninPreferences.SHAREDPREFERENCE_KEY.KEY_GENERIC_APP_SECRET)));
                }else {
                    uri = Uri.parse(getString(R.string.api_monin_recipes) + "/" + id + "/close");
                    headers.put(getString(R.string.header_authorization), getString(R.string.header_token,
                            MoninPreferences.getString(getContext(), MoninPreferences.SHAREDPREFERENCE_KEY.KEY_TOKEN)));
                }
            }else {
                uri = Uri.parse(getString(R.string.api_user_recipes) + "/" + id);
                headers.put(getString(R.string.header_authorization), getString(R.string.header_token,
                        MoninPreferences.getString(getContext(), MoninPreferences.SHAREDPREFERENCE_KEY.KEY_TOKEN)));
            }

            NetworkConnection.with(getActivity()).withListener(this).doRequest(Connection.REQUEST.GET, uri, null, headers, null);
        }
    }

    private class SavePhotoTask extends AsyncTask<byte[], String, String> {
        @Override
        protected String doInBackground(byte[]... jpeg) {

            try {
                localPath = MediaUtilities.createImageFile(getActivity()).getPath();
                File photo = new File(localPath);
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(photo));
                bos.write(jpeg[0]);
                bos.flush();
                bos.close();
            } catch (IOException e) {
            }

            return (null);
        }
    }

    private void createDirectionsFromRecipe(List <Direction> directions_arr){
        containerDirections.removeAllViews();
        contentShare.append("\n\n").append(getString(R.string.key_directions)).append("\n");
        if (directions_arr != null){
            for(Direction direction : directions_arr){
                View view = LayoutInflater.from(getContext()).inflate(R.layout.item_list_directions, null);
                LinearLayout layoutDirections = (LinearLayout) view.findViewById(R.id.layoutDirections);
                layoutDirections.setBackgroundColor(getResources().getColor(getColor(isAlcoholic, isCoffee)));
                TextView sequenceNumber = (TextView) view.findViewById(R.id.sequenceNumber);
                TextView textDirection = (TextView) view.findViewById(R.id.textDirection);
                sequenceNumber.setText(String.valueOf(direction.getSequence()).concat("."));
                textDirection.setText(direction.getDirection());
                contentShare.append("\t").append(String.valueOf(direction.getSequence()).concat(".")).append(direction.getDirection()).append("\n");
                containerDirections.addView(view);
            }
        }
    }

    private void updateUI() {
        contentShare = new StringBuilder();
        contentShare.append(name);
        servingsButton.setText(getString(R.string.servings_button,num_servings));
        units.setText(unitsTypes[isImperial ? 0 : 1]);
        //Add all directions to a group layout
        createDirectionsFromRecipe(recipe.getDirections());
        contentShare.append("\n\n").append(getString(R.string.ingredients)).append("\n");
        complexity.setText(recipe.getLevel());
        StringBuilder builder = new StringBuilder();
        isFavorite = recipe.isFavorite();
        favorite.setImageResource(recipe.isFavorite() ? R.drawable.details_fav_icon_pressed : R.drawable.details_fav_icon);
        for (Ingredient ingredient : recipe.getIngredients()){
            double fluidOzConstantMililiters = 0.03381402;
            if (isImperial && ingredient.getMeasure().contains("ml")){
                double convertedFraction = 0;
                double convertedMililiters = ingredient.getAmount() * fluidOzConstantMililiters;
                if (!ingredient.getFraction().equalsIgnoreCase("null")){
                    String numerator = ingredient.getFraction();
                    int index = numerator.indexOf("/");
                    String rest = numerator.substring(index+1);
                    numerator = numerator.substring(0, index);
                    convertedFraction = (Double.parseDouble(numerator) / Double.parseDouble(rest)) * fluidOzConstantMililiters;
                }
                convertedMililiters += convertedFraction;
                DecimalFormat decimalFormat = new DecimalFormat("##0.00");
                if (!ingredient.getMeasure().equalsIgnoreCase("null") && Utils.containsDigit(ingredient.getMeasure())){
                    String value = ingredient.getMeasure().trim();
                    int index = value.indexOf(" ");
                    value = value.substring(0,index);
                    convertedMililiters += Double.parseDouble(value) * fluidOzConstantMililiters;
                }
                builder.append(decimalFormat.format(convertedMililiters)).append(" ").append("oz. ");
                contentShare.append("\t").append(decimalFormat.format(convertedMililiters)).append(" ").append("oz. ").append("\n");
            }else if (!isImperial && ingredient.getMeasure().trim().equalsIgnoreCase("oz.")){
                double convertedFraction = 0;
                double convertedOz = ingredient.getAmount() / fluidOzConstantMililiters;
                if (!ingredient.getFraction().equalsIgnoreCase("null")){
                    String numerator = ingredient.getFraction();
                    int index = numerator.indexOf("/");
                    String rest = numerator.substring(index+1);
                    numerator = numerator.substring(0, index);
                    convertedFraction = (Double.parseDouble(numerator) / Double.parseDouble(rest)) / fluidOzConstantMililiters;
                }
                convertedOz += convertedFraction;
                if (!ingredient.getMeasure().equalsIgnoreCase("null") && Utils.containsDigit(ingredient.getMeasure())){
                    String value = ingredient.getMeasure().trim();
                    int index = value.indexOf(" ");
                    value = value.substring(0,index);
                    convertedOz += Double.parseDouble(value) / fluidOzConstantMililiters;
                }
                DecimalFormat decimalFormat = new DecimalFormat("##0.00");
                builder.append(decimalFormat.format(convertedOz)).append(" ").append("ml ");
                contentShare.append("\t").append(decimalFormat.format(convertedOz)).append(" ").append("ml ").append("\n");
            }else {
                contentShare.append("\t");
                if (ingredient.getAmount() > 0) {
                    builder.append(Integer.parseInt(String.valueOf((int) ingredient.getAmount())) * num_servings).append(" ");
                    contentShare.append(Integer.parseInt(String.valueOf((int) ingredient.getAmount())) * num_servings).append(" ");
                }
                if (!ingredient.getFraction().equalsIgnoreCase("null")){
                    String numerator = ingredient.getFraction();
                    int index = numerator.indexOf("/");
                    String rest = numerator.substring(index);
                    numerator = numerator.substring(0, index);
                    int mul = num_servings * Integer.parseInt(numerator);
                    String frac = String.valueOf(mul) + rest;
                    builder.append(frac).append(" ");
                    contentShare.append(frac).append(" ");
                }
                if (!ingredient.getMeasure().equalsIgnoreCase("null")){
                    builder.append(ingredient.getMeasure()).append(" ");
                    contentShare.append(ingredient.getMeasure()).append(" ");
                }
            }

            builder.append(ingredient.getIngredient()).append("\n\n");
            contentShare.append(ingredient.getIngredient()).append("\n");
        }
        ingredientsText.setText(builder.toString());
        String type = recipe.getGlass() != null && !recipe.getGlass().equalsIgnoreCase("null") ? recipe.getGlass() : getString(R.string.no_glass_type);
        glass.setText(getString(R.string.glass_type,type));
        if (dialog.isShowing()) dialog.dismiss();
    }

    private Detail_Recipe createDetailFromCursor(Cursor cursor) {
        cursor.moveToFirst();
        recipe = new Detail_Recipe();
        recipe.setServingsSize(cursor.getInt(cursor.getColumnIndex(MoninContract.DetailRecipeEntry.Key_ServingSize)));
        recipe.setInstructions(cursor.getString(cursor.getColumnIndex(MoninContract.DetailRecipeEntry.Key_Instructions)));
        recipe.setFinalPortion(cursor.getString(cursor.getColumnIndex(MoninContract.DetailRecipeEntry.Key_FinalPortion)));
        recipe.setDirections(createDirections(cursor.getString(cursor.getColumnIndex(MoninContract.DetailRecipeEntry.Key_Directions))));
        recipe.setIngredients(createIngredients(cursor.getString(cursor.getColumnIndex(MoninContract.DetailRecipeEntry.Key_Ingredients))));
        recipe.setGlass(cursor.getString(cursor.getColumnIndex(MoninContract.DetailRecipeEntry.Key_Glass)));
        recipe.setLevel(cursor.getString(cursor.getColumnIndex(MoninContract.DetailRecipeEntry.Key_Level)));
        int favorite = cursor.getInt(cursor.getColumnIndex(MoninContract.DetailRecipeEntry.Key_IsFavorite));
        recipe.setFavorite(favorite == 1);
        return recipe;
    }

    private List<Direction> createDirections(String string) {
        // sequence_number|direction@
        List<Direction> directionList = new ArrayList<>();
        Vector<String> dir = new Vector<>();
        string = string.concat("@");
        int index = string.indexOf("@");
        /*if (index == -1 && string.length() > 0){
            string = string + "@";
            index = string.indexOf("@");
        }*/
        while (index != -1){
            String item = string.substring(0,index);
            dir.add(item);
            string = string.substring(index + 1);
            index = string.indexOf("@");
        }
        index = 0;
        for (int i = 0; i < dir.size() ; i++){
            Direction direction = new Direction();
            String direc = dir.get(i);
            if (direc.length() > 0) {
                index = direc.indexOf("|");
                direction.setSequence(Integer.parseInt(direc.substring(0, index)));
                direc = direc.substring(index + 1);
                direction.setDirection(direc);
                directionList.add(direction);
            }
        }
        return directionList;
    }

    private List<Ingredient> createIngredients(String string){
        // amount|fraction|ingredient|measure|measureId|fractionId|id@
        List<Ingredient> ingredientList = new ArrayList<>();
        Vector<String> ingre = new Vector<>();
        string = string.concat("@");
        int index = string.indexOf("@");
        /*if (index == -1 && string.length() > 0){
            string = string + "@";
            index = string.indexOf("@");
        }*/
        while (index != -1){
            String item = string.substring(0,index);
            ingre.add(item);
            string = string.substring(index + 1);
            index = string.indexOf("@");
        }
        index = 0;
        for (int i = 0; i < ingre.size(); i++) {
            Ingredient ingredient = new Ingredient();
            String ingr = ingre.get(i);
            if (ingr.length() > 0) {
                index = ingr.indexOf("|");
                ingredient.setAmount(Double.parseDouble(ingr.substring(0, index)));
                ingr = ingr.substring(index + 1);
                index = ingr.indexOf("|");
                ingredient.setFraction(ingr.substring(0, index));
                ingr = ingr.substring(index + 1);
                index = ingr.indexOf("|");
                ingredient.setIngredient(ingr.substring(0, index));
                ingr = ingr.substring(index + 1);
                index = ingr.indexOf("|");
                ingredient.setMeasure(ingr.substring(0, index));
                ingr = ingr.substring(index + 1);
                index = ingr.indexOf("|");
                ingredient.setMeasureId(ingr.substring(0, index));
                ingr = ingr.substring(index + 1);
                index = ingr.indexOf("|");
                ingredient.setFractionID(ingr.substring(0, index));
                ingr = ingr.substring(index + 1);
                ingredient.setId(ingr);
                ingredientList.add(ingredient);
            }
        }
        return ingredientList;
    }

    @Override
    public void onSuccessfullyResponse(String response) {
        Log.d(TAG, response);
        if(isMoninRecipe){
            recipe = JSONUtils.with(getActivity()).getDetailRecipe(response);
        }else{
            recipe = JSONUtils.with(getActivity()).getDetailUserRecipe(response);
            imageUrl = JSONUtils.with(getActivity()).getPhotoFromDetailCall(response);
            if (imageUrl != null && imageUrl.length() > 0){
                dataBase.updateImageUlrRecipe(id,imageUrl);
            }
        }
        dataBase.newEntryDetailRecipe(recipe, id);
        cursor = dataBase.getRecipe(id);
        recipe = createDetailFromCursor(cursor);
        updateUI();
        if (!isImperialAllowed && isImperial){
            units.performClick();
        }
    }

    @Override
    public void onErrorResponse(String error, String message, int code) {
        if (dialog.isShowing()) dialog.dismiss();
        Utils.showSimpleMessageFinishing(getActivity(), getString(R.string.error), getString(R.string.error_nodetail));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_profile : {
                Intent intent = new Intent(getActivity(), com.softwaremobility.monin.CreateRecipes.class);
                intent.putExtra(getString(R.string.key_name), MoninPreferences.getString(getContext(),MoninPreferences.SHAREDPREFERENCE_KEY.KEY_USER_NAME));
                intent.putExtra(getString(R.string.key_id), String.valueOf(MoninPreferences.SHAREDPREFERENCE_KEY.KEY_PROVIDER_LOGIN));
                intent.putExtra("editRecipe",true);
                intent.putExtra("fileImagePath",localPath);
                intent.putExtra("recipe", recipe);
                intent.putExtra(MoninContract.MoninEntry.Key_Description,name);
                intent.putExtra(MoninContract.MoninEntry.Key_IdRecipe,id);
                //imageRecipe = getActivity().getIntent().getByteArrayExtra(MoninContract.MoninEntry.Key_ImageRecipe);
                intent.putExtra(MoninContract.MoninEntry.Key_ImageUrl,imageUrl);
                intent.putExtra(MoninContract.MoninEntry.Key_ImageFlag,imageFlag);
                intent.putExtra(MoninContract.MoninEntry.Key_IsAlcoholic,isAlcoholic);
                intent.putExtra(MoninContract.MoninEntry.Key_IsCoffee,isCoffee);
                intent.putExtra(MoninContract.MoninEntry.Key_IsMoninRecipe,isMoninRecipe);
                startActivityForResult(intent,EDIT_RECIPE);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == EDIT_RECIPE){
            if(resultCode == Activity.RESULT_OK){
                isAlcoholic = data.getExtras().getBoolean("isAlcohol",isAlcoholic);
                isCoffee = data.getExtras().getBoolean("isCoffee",isCoffee);
                name = data.getExtras().getString("name", name);
                MoninRecipes.shouldLoadAgain = true;
                dialogSimple = new SimpleCustomDialog(getContext(), getString(R.string.success_title), getString(R.string.alert_message_recipe_updated), new SimpleCustomDialog.okListener() {
                    @Override
                    public void OnOkSelected() {
                        dialogSimple.dismiss();
                        refreshViews();
                    }

                    @Override
                    public void OnCancelSelected() {

                    }
                });
                dialogSimple.hideCancelButton();
                dialogSimple.show();
            }

            if(resultCode == com.softwaremobility.fragments.CreateRecipes.DELETE_RECIPE){
                com.softwaremobility.fragments.MoninRecipes.shouldLoadAgain = true;
                Utils.showSimpleMessageFinishing(getActivity(),getString(R.string.success_title),getString(R.string.alert_message_recipe_deleted));
            }
        }
    }
}
