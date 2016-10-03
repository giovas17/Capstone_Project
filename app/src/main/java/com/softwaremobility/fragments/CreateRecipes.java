package com.softwaremobility.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.softwaremobility.adapters.AutoCompleteAdapter;
import com.softwaremobility.data.MoninContract;
import com.softwaremobility.data.MoninDataBase;
import com.softwaremobility.dialogs.CompleteImage;
import com.softwaremobility.dialogs.Measure;
import com.softwaremobility.dialogs.ProgressDialog;
import com.softwaremobility.dialogs.SimpleCustomDialog;
import com.softwaremobility.json.JSONUtils;
import com.softwaremobility.listeners.MeasureListener;
import com.softwaremobility.monin.R;
import com.softwaremobility.network.Connection;
import com.softwaremobility.network.NetworkConnection;
import com.softwaremobility.objects.AutoCompleteTextCustom;
import com.softwaremobility.objects.Detail_Recipe;
import com.softwaremobility.objects.Direction;
import com.softwaremobility.objects.FractionOption;
import com.softwaremobility.objects.GlassObject;
import com.softwaremobility.objects.Ingredient;
import com.softwaremobility.objects.MeasureOption;
import com.softwaremobility.objects.Recipe;
import com.softwaremobility.preferences.MoninPreferences;
import com.softwaremobility.utilities.PermissionsMarshmallow;
import com.softwaremobility.utilities.PictureTools;
import com.softwaremobility.utilities.Utils;
import com.weiwangcn.betterspinner.library.BetterSpinner;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.softwaremobility.utilities.Utils.getColor;

@SuppressWarnings("MissingPermission")
public class CreateRecipes extends Fragment implements MeasureListener, LocationListener{

    private static final String TAG = CreateRecipes.class.getSimpleName();
    public static final int DELETE_RECIPE = 25;
    public static boolean shouldFinish = false;
    private Recipe recipe = new Recipe();
    private Detail_Recipe detailRecipe = new Detail_Recipe();
    private ImageView image;
    private TextView textMeasure;
    private EditText title,textDirection;
    private AutoCompleteTextCustom textIngredient;
    private final int PICK_IMAGE_REQUEST = 23;
    private ViewGroup containerIngredients, containerDirections;
    private int unitsOption = 0, complexityOption = 0, typeOption = 0;
    private Ingredient ingredient_selected = new Ingredient();
    private List<MeasureOption> optionsMeasure = null;
    private List<FractionOption> optionsFractions = null;
    private Location currentLocation;
    private BetterSpinner autoCompleteTextView;
    private ArrayList<GlassObject> options;
    private String idGlass = "";
    private String localImagePath;
    private boolean editRecipe = false;
    private String name, id;
    private byte[] imageRecipe, imageFlag;
    private String imageUrl;
    private boolean isAlcoholic, isCoffee, isMoninRecipe;
    private String glass = "";
    private ImageView addButton,addDirection;
    private boolean didChangeImageRecipe = false;
    private ProgressDialog dialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        editRecipe = getActivity().getIntent().getExtras().getBoolean("editRecipe");
        if(editRecipe){
            localImagePath = getActivity().getIntent().getExtras().getString("fileImagePath");
            detailRecipe = getActivity().getIntent().getExtras().getParcelable("recipe");
            name = getActivity().getIntent().getStringExtra(MoninContract.MoninEntry.Key_Description);
            id = getActivity().getIntent().getStringExtra(MoninContract.MoninEntry.Key_IdRecipe);
            //imageRecipe = getActivity().getIntent().getByteArrayExtra(MoninContract.MoninEntry.Key_ImageRecipe);
            imageUrl = getActivity().getIntent().getStringExtra(MoninContract.MoninEntry.Key_ImageUrl);
            imageFlag = getActivity().getIntent().getByteArrayExtra(MoninContract.MoninEntry.Key_ImageFlag);
            isAlcoholic = getActivity().getIntent().getBooleanExtra(MoninContract.MoninEntry.Key_IsAlcoholic, false);
            isCoffee = getActivity().getIntent().getBooleanExtra(MoninContract.MoninEntry.Key_IsCoffee, false);
            isMoninRecipe = getActivity().getIntent().getBooleanExtra(MoninContract.MoninEntry.Key_IsMoninRecipe, false);
        }
        boolean isGPSAllowed = MoninPreferences.getBoolean(getContext(), MoninPreferences.SHAREDPREFERENCE_KEY.KEY_GPS_ALLOWED);
        if (isGPSAllowed){
            if (PermissionsMarshmallow.permissionForGPSGranted(getActivity())){
                LocationManager locman = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                currentLocation = locman.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                locman.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,2000,this);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_recipe_creation, container, false);

        image = (ImageView) v.findViewById(R.id.imageRecipeCreation);
        dialog = new ProgressDialog(getContext(),getString(R.string.please_wait));
        if(!editRecipe){
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logo_panache);
            Bitmap blurBitmap = blur(bitmap);
            image.setImageBitmap(blurBitmap);
        }else{
            Bitmap bitmap = BitmapFactory.decodeFile(localImagePath);
            image.setImageBitmap(bitmap);
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            recipe.setImageURL(localImagePath);
        }

        final Map<String,String> headers = new HashMap<>();
        headers.put(getString(R.string.header_authorization), getString(R.string.header_token,
                MoninPreferences.getString(getContext(), MoninPreferences.SHAREDPREFERENCE_KEY.KEY_TOKEN)));
        NetworkConnection.with(getContext()).withListener(new NetworkConnection.ResponseListener() {
            @Override
            public void onSuccessfullyResponse(String response) {
                optionsMeasure = JSONUtils.with(getActivity()).getMeasureOptions(response);
                optionsFractions = JSONUtils.with(getActivity()).getFractionOptions(response);
                FractionOption noneOption = new FractionOption(getString(R.string.none),"0");
                optionsFractions.add(0,noneOption);
                callGlasses(headers);
            }

            @Override
            public void onErrorResponse(String error, String message, int code) {

            }
        }).doRequest(Connection.REQUEST.GET, Uri.parse(getString(R.string.api_user_recipes_measure_options)), null, headers, null);

        final TextView textUnits = (TextView) v.findViewById(R.id.buttonUnitsCreation);
        textUnits.setText(getResources().getStringArray(R.array.units_creation_recipe)[unitsOption]);
        textUnits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (unitsOption == 0) {
                    unitsOption++;
                } else {
                    unitsOption--;
                }
                textUnits.setText(getResources().getStringArray(R.array.units_creation_recipe)[unitsOption]);
            }
        });

        final TextView textComplexity = (TextView)v.findViewById(R.id.buttonComplexityCreation);

        if(editRecipe){
            textComplexity.setText(detailRecipe.getLevel());
        }else{
            textComplexity.setText(getResources().getStringArray(R.array.complexity_creation_recipe)[complexityOption]);
        }
        textComplexity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                complexityOption++;
                if (complexityOption >= getResources().getStringArray(R.array.complexity_creation_recipe).length) {
                    complexityOption = 0;
                }
                textComplexity.setText(getResources().getStringArray(R.array.complexity_creation_recipe)[complexityOption]);
                detailRecipe.setLevel(getResources().getStringArray(R.array.complexity_creation_recipe)[complexityOption]);
            }
        });

        final TextView textType = (TextView)v.findViewById(R.id.buttonTypeCreation);
        if(editRecipe){
            typeOption = getCategory(isAlcoholic,isCoffee);

        }
        textType.setText(getResources().getStringArray(R.array.filters_recipe)[typeOption]);
        textType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                typeOption++;
                if (typeOption >= getResources().getStringArray(R.array.filters_recipe).length){
                    typeOption = 0;
                }
                textType.setText(getResources().getStringArray(R.array.filters_recipe)[typeOption]);
                setColorRecipe();
            }
        });
        containerIngredients = (ViewGroup)v.findViewById(R.id.containerIngredientsCreation);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.item_new_ingredient, null);
        textMeasure = (TextView)view.findViewById(R.id.textNewMeasure);
        textMeasure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Measure dialog = new Measure(getContext(), CreateRecipes.this, ingredient_selected, optionsMeasure, optionsFractions);
                dialog.show();
            }
        });
        textIngredient = (AutoCompleteTextCustom) view.findViewById(R.id.textNewIngredient);
        textIngredient.setThreshold(3);
        textIngredient.setLoadingIndicator((android.widget.ProgressBar) view.findViewById(R.id.loading));
        textIngredient.setAdapter(new AutoCompleteAdapter(getContext(), R.layout.item_autocomplete_text));
        textIngredient.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Ingredient ingredient = (Ingredient) textIngredient.getAdapter().getItem(position);
                if (!ingredient.getIngredient().equals(getString(R.string.loading))) {
                    ArrayAdapter<Ingredient> listAdapter = (ArrayAdapter<Ingredient>) textIngredient.getAdapter();
                    ingredient_selected.setId(ingredient.getId());
                    ingredient_selected.setIngredient(ingredient.getIngredient());
                    textIngredient.setAdapter(null);
                    textIngredient.setText(ingredient.getIngredient());
                    textIngredient.setAdapter(listAdapter);
                    textIngredient.setEnabled(false);
                } else {
                    textIngredient.setText("");
                }
            }
        });
        textIngredient.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() > 0) {
                    textIngredient.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                } else {
                    textIngredient.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.plus_add_icon, 0);
                }
            }
        });
        addButton = (ImageView)view.findViewById(R.id.addingButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!textMeasure.getText().toString().equalsIgnoreCase(getString(R.string.enter_measure)) &&
                        !textIngredient.getText().toString().equalsIgnoreCase("")) {
                    View view = LayoutInflater.from(getContext()).inflate(R.layout.item_new_ingredient, null);
                    ImageView drag = (ImageView) view.findViewById(R.id.dragItemImage);
                    drag.setVisibility(View.VISIBLE);
                    ImageView deleteIcon = (ImageView) view.findViewById(R.id.addingButton);
                    TextView measureText = (TextView) view.findViewById(R.id.textNewMeasure);
                    if (ingredient_selected.getFraction().equalsIgnoreCase(getString(R.string.none))) {
                        ingredient_selected.setFraction("");
                    }
                    measureText.setText(getString(R.string.measure_text_format, ingredient_selected.getAmount() == 0 ? "" : ingredient_selected.getAmount(), ingredient_selected.getFraction(), ingredient_selected.getMeasure()));
                    measureText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    textMeasure.setText(getString(R.string.enter_measure));
                    textMeasure.setTextColor(getResources().getColor(R.color.disabledLoginButton));
                    AutoCompleteTextCustom ingredientText = (AutoCompleteTextCustom) view.findViewById(R.id.textNewIngredient);
                    if(ingredient_selected.getIngredient().equals("")){
                        ingredientText.setText(textIngredient.getText().toString());
                        ingredient_selected.setIngredient(textIngredient.getText().toString());
                    }else{
                        ingredientText.setText(ingredient_selected.getIngredient());
                    }
                    ingredientText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    textIngredient.setText("");
                    ingredientText.setEnabled(false);
                    view.setTag(ingredient_selected);
                    deleteIcon.setImageResource(R.drawable.delete_item_icon);
                    deleteIcon.setTag(view);
                    deleteIcon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            containerIngredients.removeView((View) v.getTag());
                        }
                    });
                    view.setTag(ingredient_selected);
                    if (containerIngredients.getChildCount() == 1) {
                        containerIngredients.addView(view, 0);
                    } else {
                        containerIngredients.addView(view, containerIngredients.getChildCount() - 1);
                    }
                    textIngredient.setEnabled(true);
                    ingredient_selected = new Ingredient();
                }
            }
        });
        containerIngredients.addView(view);
        if(editRecipe){
            addIngredientsAuto();
        }
        //containerIngredients.addView(view);
        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), getString(R.string.font_path));
        title = (EditText) v.findViewById(R.id.textDescriptionCreateRecipe);
        title.setTypeface(typeface, Typeface.BOLD);
        title.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.plus_create_title_icon, 0);
        title.setPadding(5, 5, 188, 5);
        setColorRecipe();
        title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() > 0) {
                    title.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    title.setPadding(5, 5, 5, 5);
                } else {
                    //Assign your image again to the view, otherwise it will always be gone even if the text is 0 again.
                    title.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.plus_create_title_icon, 0);
                    title.setPadding(5, 5, 188, 5);
                }
            }
        });
        if(editRecipe){
            title.setText(name);
            title.setBackgroundColor(getResources().getColor(getColor(isAlcoholic, isCoffee)));
        }

        TextView cameraShoot = (TextView)v.findViewById(R.id.cameraButtonCreation);
        cameraShoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), com.softwaremobility.monin.PreviewCropImage.class);
                intent.putExtra(getString(R.string.tag_photo),true);
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            }
        });

        TextView imagePicker = (TextView)v.findViewById(R.id.galleryButtonCreation);
        imagePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), com.softwaremobility.monin.PreviewCropImage.class);
                intent.putExtra(getString(R.string.tag_gallery),true);
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            }
        });

        detailRecipe.setServingsSize(1);

        containerDirections = (ViewGroup)v.findViewById(R.id.containerDirectionsCreation);
        final View contDirections = LayoutInflater.from(getContext()).inflate(R.layout.item_new_direction, null);
        containerDirections.addView(contDirections);

        textDirection = (EditText)contDirections.findViewById(R.id.textNewDirection);
        addDirection = (ImageView)contDirections.findViewById(R.id.addingButtonDirections);
        addDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (textDirection.getText().length() > 0) {
                    Direction directionAdded = new Direction();
                    View view = LayoutInflater.from(getContext()).inflate(R.layout.item_new_direction, null);
                    directionAdded.setDirection(textDirection.getText().toString());
                    EditText textDir = (EditText) view.findViewById(R.id.textNewDirection);
                    textDir.setVisibility(View.GONE);
                    textDirection.setText("");
                    TextView numSequence = (TextView) view.findViewById(R.id.sequenceNumber);
                    numSequence.setText(containerDirections.getChildCount() + ".");
                    directionAdded.setSequence(containerDirections.getChildCount());
                    LinearLayout cont = (LinearLayout) view.findViewById(R.id.addedDirection);
                    cont.setVisibility(View.VISIBLE);
                    EditText direction = (EditText) view.findViewById(R.id.textNewDirectionAdded);
                    direction.setText(directionAdded.getDirection());
                    ImageView drag = (ImageView) view.findViewById(R.id.dragItemImageDirection);
                    drag.setVisibility(View.VISIBLE);
                    view.setTag(directionAdded);
                    ImageView delete = (ImageView) view.findViewById(R.id.addingButtonDirections);
                    delete.setImageResource(R.drawable.delete_item_icon);
                    delete.setTag(view);
                    delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            containerDirections.removeView((View) v.getTag());
                            for (int i = 0; i < containerDirections.getChildCount() - 1; i++) {
                                Direction dir = (Direction) containerDirections.getChildAt(i).getTag();
                                dir.setSequence(i + 1);
                                containerDirections.getChildAt(i).setTag(dir);
                                ((TextView) containerDirections.getChildAt(i).findViewById(R.id.sequenceNumber)).setText(dir.getSequence() + ".");
                            }
                        }
                    });
                    if (containerDirections.getChildCount() == 1) {
                        containerDirections.addView(view, 0);

                    } else {
                        containerDirections.addView(view, containerDirections.getChildCount() - 1);
                    }

                }else {
                    textDirection.setError(getString(R.string.error_field_required));
                    textDirection.requestFocus();
                }
            }
        });
        if(editRecipe){
            addDirectionsAuto();
        }
        final Button nextStep = (Button)v.findViewById(R.id.buttonNextStep);
        if(editRecipe){
           nextStep.setText(R.string.delete_recipe);
            nextStep.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SimpleCustomDialog dialog = new SimpleCustomDialog(getContext(),
                            getString(R.string.delete_recipe), getString(R.string.message_confirmation_delete_recipe),
                            new SimpleCustomDialog.okListener() {
                        @Override
                        public void OnOkSelected() {
                            deleteRecipeFromServer();
                        }

                        @Override
                        public void OnCancelSelected() {

                        }
                    });
                    dialog.setYesNoButtons();
                    dialog.show();
                }
            });
            v.findViewById(R.id.dividerDetail2).setVisibility(View.VISIBLE);
            Button buttonMood = (Button) v.findViewById(R.id.buttonMoodMatcher);
            buttonMood.setVisibility(View.GONE);
            buttonMood.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

        }else{
            nextStep.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (textIngredient.getText().length() > 0 && textMeasure.getText().length() > 0){
                        addButton.performClick();
                    }
                    if (textDirection.getText().length() > 0){
                        addDirection.performClick();
                    }
                    if (passValidation()){
                        nextStep.setEnabled(false);
                        createRecipeFromData();
                        detailRecipe.setIngredients(createIngredientsFromTag());
                        detailRecipe.setDirections(createDirectionsFromTag());
                        sendRecipeToServer(recipe,detailRecipe.getDirections(),detailRecipe.getIngredients());
                    }
                }
            });
        }

        autoCompleteTextView = (BetterSpinner) v.findViewById(R.id.autoComplete);
        return v;
    }

    private void saveRecipeIndataBase(Recipe recipe) {
        new MoninDataBase(getActivity()).newEntryRecipe(recipe, false);
    }

    private void sendRecipeToServer(final Recipe recipe, final List<Direction> directions, final List<Ingredient> ingredients){
        if ((dialog != null) && !dialog.isShowing()){
            dialog.show();
        }
        Uri uri = null;
        Map<String,String> headers = new HashMap<>();
        headers.put(getString(R.string.header_authorization), getString(R.string.header_token, MoninPreferences.getString(getActivity(), MoninPreferences.SHAREDPREFERENCE_KEY.KEY_TOKEN)));
        headers.put(getString(R.string.key_content_type), getString(R.string.header_json));
        JSONObject jsonObject;
        if(editRecipe){
            uri = Uri.parse(getString(R.string.api_user_recipes));
            jsonObject = JSONUtils.with(getActivity()).getNewUserRecipeJSON(recipe.getId(),recipe.getDescription(),
                    getLevelID(complexityOption),0,detailRecipe.getServingsSize(), getImperialMetric(unitsOption), getCategoryID(typeOption),ingredients,directions,idGlass);
        }else{
            uri = Uri.parse(getString(R.string.api_user_recipes_v2));
            jsonObject = JSONUtils.with(getActivity()).getNewUserRecipeJSON(recipe.getId(),recipe.getDescription(),
                    getLevelID(complexityOption),0,detailRecipe.getServingsSize(), getImperialMetric(unitsOption), getCategoryID(typeOption),ingredients,directions,idGlass);
        }
        NetworkConnection.with(getActivity()).withListener(new NetworkConnection.ResponseListener() {
            @Override
            public void onSuccessfullyResponse(String response) {
                Log.d(TAG,response);
                try {
                    JSONObject object = new JSONObject(response);
                    int id = object.getInt(getString(R.string.key_id).toUpperCase());
                    recipe.setId(String.valueOf(id));
                    if(editRecipe){ //Is editing recipe
                        if(didChangeImageRecipe){ //Change image from editing recipe
                            sendImageRecipe(getImageInBytes(recipe.getImageURL(),recipe.isImageFromBitmap()),recipe.getId());
                        }else{
                            MoninDataBase moninDataBase = new MoninDataBase(getContext());
                            moninDataBase.updateRecipe(recipe,false);
                            moninDataBase.updateDetailRecipe(detailRecipe, recipe.getId());
                            Intent i = new Intent(getContext(), com.softwaremobility.monin.CreateRecipes.class);
                            i.putExtra("isAlcohol",recipe.isAlcohol());
                            i.putExtra("isCoffee",recipe.isCofee());
                            i.putExtra("name",recipe.getDescription());
                            getActivity().setResult(Activity.RESULT_OK,i);
                            getActivity().finish();
                        }
                    }else{
                        saveRecipeIndataBase(recipe);
                        sendImageRecipe(getImageInBytes(recipe.getImageURL(),recipe.isImageFromBitmap()),recipe.getId());
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onErrorResponse(String error, String message, int code) {
                if (code == 105) {
                    if (dialog.isShowing()){
                        dialog.dismiss();
                    }
                    Utils.showSimpleMessageFinishing(getActivity(),getString(R.string.error_no_internet),getString(R.string.error_no_internet));
                }
            }
        }).doRequest(editRecipe ? Connection.REQUEST.PUT : Connection.REQUEST.POST, uri, null, headers, jsonObject);
    }

    private void sendImageRecipe(final byte [] image, final String idRecipe){
        Map<String,String> headers = new HashMap<>();
        headers.put(getString(R.string.header_authorization), getString(R.string.header_token, MoninPreferences.getString(getActivity(), MoninPreferences.SHAREDPREFERENCE_KEY.KEY_TOKEN)));
        Uri uri = Uri.parse(getString(R.string.api_user_recipes_photo) + "?recipeID=" + idRecipe);
        NetworkConnection.with(getActivity()).withListener(new NetworkConnection.ResponseListener() {
            @Override
            public void onSuccessfullyResponse(String response) {
                Log.d(TAG,response);
                if (dialog.isShowing()){
                    dialog.dismiss();
                }
                if(!editRecipe){
                    MoninRecipes.shouldLoadAgain = true;
                    Utils.showSimpleMessageFinishing(getActivity(),getString(R.string.title_recipe_creation_success),getString(R.string.message_recipe_creation_success));
                }else{
                    MoninDataBase moninDataBase = new MoninDataBase(getContext());
                    moninDataBase.updateRecipe(recipe,false);
                    moninDataBase.updateDetailRecipe(detailRecipe, String.valueOf(idRecipe));
                    Intent i = new Intent(getContext(), com.softwaremobility.monin.CreateRecipes.class);
                    i.putExtra("isAlcohol",recipe.isAlcohol());
                    i.putExtra("isCoffee",recipe.isCofee());
                    getActivity().setResult(Activity.RESULT_OK,i);
                    getActivity().finish();
                }
            }
            @Override
            public void onErrorResponse(String error, String message, int code) {
                if (code == 105) {
                    if (dialog.isShowing()){
                        dialog.dismiss();
                    }
                    Utils.showSimpleMessageFinishing(getActivity(),getString(R.string.error_title_no_internet),getString(R.string.error_no_internet));
                }
            }
        }).doRequest(Connection.REQUEST.POST, uri, null, headers, null,image);

    }


    private byte[] getImageInBytes(String imagePath, boolean isFromBitmap){
        if (imagePath == null || imagePath.length() == 0){
            return null;
        }
        Bitmap bitmap;
        if (isFromBitmap){
            bitmap = BitmapFactory.decodeFile(imagePath);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);
            byte[] array = stream.toByteArray();
            return array;
        }else {
            try {
                File imageFile = new File(imagePath);
                //bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                bitmap = PictureTools.decodeSampledBitmapFromUri(imageFile.getAbsolutePath(),1280,720);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);
                byte[] array = stream.toByteArray();
                return array;
            } catch (Exception e){
                bitmap = null;
                return null;
            }

        }
    }

    public void deleteRecipeFromServer(){

        Uri uri = Uri.parse(getString( R.string.api_user_recipes) + "/" + id);
        Map<String, String> headers = new HashMap<>();
        headers.put(getString(R.string.header_authorization), getString(R.string.header_token,
                MoninPreferences.getString(getContext(), MoninPreferences.SHAREDPREFERENCE_KEY.KEY_TOKEN)));
        NetworkConnection.with(getActivity()).withListener(new NetworkConnection.ResponseListener() {
            @Override
            public void onSuccessfullyResponse(String response) {
                Log.d(TAG,response);
                MoninDataBase moninDataBase = new MoninDataBase(getContext());
                moninDataBase.deleteDetailRecipe(id);
                moninDataBase.deleteRecipe(id);
                Intent i = new Intent(getContext(),Detail.class);
                getActivity().setResult(DELETE_RECIPE,i);
                getActivity().finish();
            }

            @Override
            public void onErrorResponse(String error, String message, int code) {
                if (code == 105) {
                    Utils.showSimpleMessageFinishing(getActivity(),getString(R.string.error_title_no_internet),getString(R.string.error_no_internet));
                }
            }
        }).doRequest(Connection.REQUEST.DELETE, uri, null, headers, null);
    }

    public void addIngredientsAuto(){
        for(Ingredient ingredient_selected : detailRecipe.getIngredients()){
            View view = LayoutInflater.from(getContext()).inflate(R.layout.item_new_ingredient, null);
            ImageView drag = (ImageView) view.findViewById(R.id.dragItemImage);
                drag.setVisibility(View.VISIBLE);
                ImageView deleteIcon = (ImageView) view.findViewById(R.id.addingButton);
                TextView measureText = (TextView) view.findViewById(R.id.textNewMeasure);
                if (ingredient_selected.getFraction().equalsIgnoreCase(getString(R.string.none))) {
                    ingredient_selected.setFraction("");
                }
                measureText.setText(getString(R.string.measure_text_format, ingredient_selected.getAmount() == 0 ? "" :String.valueOf((int)ingredient_selected.getAmount()), ingredient_selected.getFraction(), ingredient_selected.getMeasure()));
                measureText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                textMeasure.setText(getString(R.string.enter_measure));
                textMeasure.setTextColor(getResources().getColor(R.color.disabledLoginButton));
                AutoCompleteTextCustom ingredientText = (AutoCompleteTextCustom) view.findViewById(R.id.textNewIngredient);
                if(ingredient_selected.getIngredient().equals("")){
                    ingredientText.setText(textIngredient.getText().toString());
                    ingredient_selected.setIngredient(textIngredient.getText().toString());
                }else{
                    ingredientText.setText(ingredient_selected.getIngredient());
                }
                ingredientText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                textIngredient.setText("");
                ingredientText.setEnabled(false);
                //Pass the measure ingredient
                view.setTag(ingredient_selected);
                deleteIcon.setImageResource(R.drawable.delete_item_icon);
                deleteIcon.setTag(view);
                deleteIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        containerIngredients.removeView((View) v.getTag());
                    }
                });
                view.setTag(ingredient_selected);
                if (containerIngredients.getChildCount() == 1) {
                    containerIngredients.addView(view, 0);
                } else {
                    containerIngredients.addView(view, containerIngredients.getChildCount() - 1);
                }
                textIngredient.setEnabled(true);
                //ingredient_selected = new Measure_Ingredient();
        }
    }

    public void addDirectionsAuto(){

        for(Direction directionAdded : detailRecipe.getDirections()){
            View view = LayoutInflater.from(getContext()).inflate(R.layout.item_new_direction, null);
            directionAdded.setDirection(directionAdded.getDirection());
            EditText textDir = (EditText) view.findViewById(R.id.textNewDirection);
            textDir.setVisibility(View.GONE);
            TextView numSequence = (TextView) view.findViewById(R.id.sequenceNumber);
            numSequence.setText(containerDirections.getChildCount() + ".");
            directionAdded.setSequence(containerDirections.getChildCount());
            LinearLayout cont = (LinearLayout) view.findViewById(R.id.addedDirection);
            cont.setVisibility(View.VISIBLE);
            EditText direction = (EditText) view.findViewById(R.id.textNewDirectionAdded);
            direction.setText(directionAdded.getDirection());
            ImageView drag = (ImageView) view.findViewById(R.id.dragItemImageDirection);
            drag.setVisibility(View.VISIBLE);
            view.setTag(directionAdded);
            ImageView delete = (ImageView) view.findViewById(R.id.addingButtonDirections);
            delete.setImageResource(R.drawable.delete_item_icon);
            delete.setTag(view);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    containerDirections.removeView((View) v.getTag());
                    for (int i = 0; i < containerDirections.getChildCount() - 1; i++) {
                        Direction dir = (Direction) containerDirections.getChildAt(i).getTag();
                        dir.setSequence(i + 1);
                        containerDirections.getChildAt(i).setTag(dir);
                        ((TextView) containerDirections.getChildAt(i).findViewById(R.id.sequenceNumber)).setText(dir.getSequence() + ".");
                    }
                }
            });
            if (containerDirections.getChildCount() == 1) {
                containerDirections.addView(view, 0);

            } else {
                containerDirections.addView(view, containerDirections.getChildCount() - 1);
            }
        }

    }

    public static int getCategory(boolean isAlcoholic, boolean isCoffee){
        int index = 0;
        if(isAlcoholic){
            index = 0;
        }
        else if(isCoffee){
            index = 2;
        }else{
            index = 1;
        }
        return index;
    }


    private void callGlasses(Map<String,String> headers) {
        Map<String,String> params = new HashMap<>();
        params = Utils.addRegionParameters(getContext(),params,currentLocation,true);
        NetworkConnection.with(getContext()).withListener(new NetworkConnection.ResponseListener() {
            @Override
            public void onSuccessfullyResponse(String response) {
                options = JSONUtils.with(getContext()).getGlasses(response);
                String[] autoCompleteOptions = new String[options.size()];
                for (int i = 0 ; i < options.size() ; i++){
                    autoCompleteOptions[i] = options.get(i).getName();
                }
                ArrayAdapter<String> adapterAutocomplete = new ArrayAdapter<>(getContext(),
                        android.R.layout.simple_spinner_dropdown_item,autoCompleteOptions);
                autoCompleteTextView.setAdapter(adapterAutocomplete);
                autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                        idGlass = options.get(position).getId();
                        glass = options.get(position).getName();
                    }
                });
                if(editRecipe){
                    autoCompleteTextView.setText(autoCompleteOptions[getSpinnerIndex(detailRecipe.getGlass(),autoCompleteOptions)]);
                }
            }

            @Override
            public void onErrorResponse(String error, String message, int code) {

            }
        }).doRequest(Connection.REQUEST.GET,Uri.parse(getString(R.string.api_glasses)),params,headers,null);
    }

    public boolean getImperialMetric(int unitsOption){
        boolean imperial = true;
        if(unitsOption == 1){
            imperial = false;//Is metric
        }
        return imperial;
    }

    public int getSpinnerIndex(String value, String[] spinner){
        for(int i = 0; i < spinner.length; i++){
            if(spinner[i].equalsIgnoreCase(value)){
                return i;
            }
        }
        return 0;
    }

    public int getLevelID(int complexityOption){
        int level = 0;
        switch (complexityOption){
            case 0: //EASY
                level = 1;
                break;
            case 1: //MEDIUM
                level = 2;
                break;
            case 2: //DIFFICULT
                level = 3;
                break;
        }
        return  level;
    }

    public int getCategoryID(int typeOption){
        int categoryID = 0;
        switch (typeOption){
            case 0: //ALCOHOLIC
                categoryID = 1;
                break;
            case 1: //NON ALCOHOCL
                categoryID = 2;
                break;
            case 2: //NCOFEEE
                categoryID = 3;
                break;
            default://OTHERS
                categoryID = 0;
                break;
        }
        return  categoryID;
    }

    private ArrayList<Ingredient> createIngredientsFromTag(){
        ArrayList<Ingredient> ingredients_arr = new ArrayList<>();
        for(int i = 0; i < containerIngredients.getChildCount(); i++){
            View view = containerIngredients.getChildAt(i);
            if(view != null){
                Ingredient ingredient = (Ingredient) view.getTag();
                if(ingredient != null){
                    ingredients_arr.add(ingredient);
                }
            }
        }
        return ingredients_arr;
    }

    private boolean hasAtLeastOneMoninIngredient(){
        boolean hasAtLeastOneMoninIngredient = false;
        ArrayList<Ingredient> ingredients_arr = new ArrayList<>();
        for(int i = 0; i <= containerIngredients.getChildCount(); i++){
            View view = containerIngredients.getChildAt(i);
            if(view != null){
                Ingredient ingredient = (Ingredient) view.getTag();
                if(ingredient != null){
                    ingredients_arr.add(ingredient);
                    if(ingredient.getId() != null && (!(ingredient.getId().equals("")))){
                        hasAtLeastOneMoninIngredient = true;
                    }
                }
            }
        }
        return hasAtLeastOneMoninIngredient;
    }

    private ArrayList<Direction> createDirectionsFromTag(){
        ArrayList<Direction> directions_arr = new ArrayList<>();
        for(int i = 0; i < containerDirections.getChildCount(); i++){
            View view = containerDirections.getChildAt(i);
            if(view != null){
                Direction ingredient = (Direction) view.getTag();
                if(ingredient != null){
                    directions_arr.add(ingredient);
                }
            }
        }
        return directions_arr;
    }

    private void createRecipeFromData() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmss", Locale.US);
        if (!editRecipe) {
            recipe.setId(getString(R.string.id_generator_format, dateFormat.format(new Date())));
        }else {
            recipe.setId(id);
        }
        recipe.setDescription(title.getText().toString());
        recipe.setLocation( Locale.getDefault().getCountry());
    }

    private boolean passValidation() {
        if (title.getText().length() == 0){
            Utils.showSimpleMessage(getContext(),null,getString(R.string.error_name_required));
            return false;
        }
        if (autoCompleteTextView.getText().length() == 0){
            Utils.showSimpleMessage(getContext(),null,getString(R.string.error_glass_type_required));
            return false;
        }
        if (containerDirections.getChildCount() == 1){
            Utils.showSimpleMessage(getContext(),null,getString(R.string.error_directions_required));
            return false;
        }
        if (containerIngredients.getChildCount() == 1){
                Utils.showSimpleMessage(getContext(),null,getString(R.string.error_monin_ingredients_required));
            return false;
        }else{
            if(!hasAtLeastOneMoninIngredient()){
                Utils.showSimpleMessage(getContext(),null,getString(R.string.error_monin_ingredients_required));
                return false;
            }
        }
        if (recipe.getImageURL() == null || recipe.getImageURL().equalsIgnoreCase("")){
            Utils.showSimpleMessage(getContext(),null,getString(R.string.error_photo_required));
            return false;
        }

        return true;
    }

    private void setColorRecipe() {
        switch (typeOption){
            case 0:{
                title.setBackgroundColor(getResources().getColor(R.color.alcoholic_drinks));
                recipe.setAlcohol(true);
                recipe.setCofee(false);
                break;
            }case 1:{
                title.setBackgroundColor(getResources().getColor(R.color.non_alcoholic_drinks));
                recipe.setAlcohol(false);
                recipe.setCofee(false);
                break;
            }default:{
                title.setBackgroundColor(getResources().getColor(R.color.cofees));
                recipe.setAlcohol(false);
                recipe.setCofee(true);
            }
        }
    }

    public Bitmap blur(Bitmap image) {
        final float BLUR_RADIUS = 25f;
        if (null == image) return null;

        Bitmap outputBitmap = Bitmap.createBitmap(image);
        String arq = System.getProperty("os.arch");
        if (!arq.equalsIgnoreCase("aarch64") && !arq.endsWith("64")) {
            final RenderScript renderScript = RenderScript.create(getActivity());
            Allocation tmpIn = Allocation.createFromBitmap(renderScript, image);
            Allocation tmpOut = Allocation.createFromBitmap(renderScript, outputBitmap);

            //Intrinsic Gaussian blur filter
            ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
            theIntrinsic.setRadius(BLUR_RADIUS);
            theIntrinsic.setInput(tmpIn);
            theIntrinsic.forEach(tmpOut);
            tmpOut.copyTo(outputBitmap);
        }
        return outputBitmap;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == PICK_IMAGE_REQUEST){
            if (resultCode == Activity.RESULT_OK){
                Glide.with(this).load(data.getStringExtra(getString(R.string.tag_picture))).asBitmap().fitCenter()
                        .into(new SimpleTarget<Bitmap>(com.bumptech.glide.request.target.Target.SIZE_ORIGINAL, com.bumptech.glide.request.target.Target.SIZE_ORIGINAL) {
                            @Override
                            public void onResourceReady(final Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                image.setImageBitmap(resource);
                                Log.d(TAG,"Image width: " + resource.getWidth());
                                Log.d(TAG,"Image height: " + resource.getHeight());
                                Log.d(TAG,"Image size: " + (resource.getHeight() * resource.getRowBytes()));
                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                resource.compress(Bitmap.CompressFormat.JPEG,100,stream);
                                byte[] array = stream.toByteArray();
                                didChangeImageRecipe = true;
                                final Bitmap bitmap2 = Bitmap.createScaledBitmap(resource, (int) (resource.getWidth() * 0.5), (int) (resource.getHeight() * 0.5), false);
                                image.setImageBitmap(bitmap2);
                                image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                image.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        CompleteImage dialog = new CompleteImage(getContext(), resource);
                                        dialog.show();
                                    }
                                });
                                recipe.setImageURL(data.getStringExtra(getString(R.string.tag_picture)));
                                recipe.setImageFromBitmap(true);
                            }
                        });
            }
        }
        if(requestCode == Detail.EDIT_RECIPE){
            if (resultCode == Activity.RESULT_OK){
                Intent i = new Intent(getContext(),Detail.class);
                getActivity().setResult(Activity.RESULT_OK,i);
                getActivity().finish();
            }
        }
    }

    @Override
    public void onMeasureSelected(Ingredient object) {
        ingredient_selected = object;
        if (ingredient_selected.getFraction().equalsIgnoreCase(getString(R.string.none))){
            ingredient_selected.setFraction("");
        }
        textMeasure.setText(getString(R.string.measure_text_format, String.valueOf((int)object.getAmount()),object.getFraction(), object.getMeasure()));
        textMeasure.setTextColor(Color.BLACK);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (shouldFinish){
            shouldFinish = false;
            MoninRecipes.shouldLoadAgain = true;
            getActivity().finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_recipe : {
                if (textIngredient.getText().length() > 0 && textMeasure.getText().length() > 0){
                    addButton.performClick();
                }
                if (textDirection.getText().length() > 0){
                    addDirection.performClick();
                }
                if (passValidation()){
                    SimpleCustomDialog dialog = new SimpleCustomDialog(getContext(), getString(R.string.alert_title_update_recipe), getString(R.string.alert_message_update_recipe), new SimpleCustomDialog.okListener() {
                        @Override
                        public void OnOkSelected() {
                            createRecipeFromData();
                            detailRecipe.setIngredients(createIngredientsFromTag());
                            detailRecipe.setDirections(createDirectionsFromTag());
                            sendRecipeToServer(recipe,detailRecipe.getDirections(),detailRecipe.getIngredients());
                        }

                        @Override
                        public void OnCancelSelected() {

                        }
                    });
                    dialog.setYesNoButtons();
                    dialog.show();
                }
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
