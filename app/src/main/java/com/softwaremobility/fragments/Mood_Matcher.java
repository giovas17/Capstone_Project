package com.softwaremobility.fragments;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;


import com.softwaremobility.dialogs.ProgressDialog;
import com.softwaremobility.json.JSONUtils;
import com.softwaremobility.monin.*;
import com.softwaremobility.network.Connection;
import com.softwaremobility.network.NetworkConnection;
import com.softwaremobility.objects.ButtonMoodMatcher;
import com.softwaremobility.objects.Detail_Recipe;
import com.softwaremobility.objects.Direction;
import com.softwaremobility.objects.Ingredient;
import com.softwaremobility.objects.Recipe;
import com.softwaremobility.preferences.MoninPreferences;
import com.softwaremobility.utilities.PermissionsMarshmallow;
import com.softwaremobility.utilities.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("MissingPermission")
public class Mood_Matcher extends Fragment implements LocationListener{

    private final String TAG = Mood_Matcher.class.getSimpleName();

    private boolean isAlcoholic = false, isNonAlcoholic = false, isCoffee = false;
    private Button alcoholicButton;
    private Button nonAlcoholicButton;
    private Button coffeeButton;
    private GridLayout containerButtons;
    private List<ButtonMoodMatcher> buttons;
    private ArrayList<CharSequence> optionsSelected;
    private boolean isUserCreation = false;
    private boolean editRecipe = false;
    private boolean changeImageRecipe = false;
    private String recipeId;
    private List<Ingredient> ingredients;
    private List<Direction> directions;
    private Recipe recipe = null;
    private Detail_Recipe detail_recipe = null;
    private Location currentLocation;
    private LocationManager locman;
    private int levelId, categoryID, servingSize;
    private String id_glass_type;
    private boolean imperialMetric;
    private int width;
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isUserCreation = getActivity().getIntent().getBooleanExtra("userCreation",false);
        if (isUserCreation){
            levelId = getActivity().getIntent().getIntExtra("levelID",0);
            categoryID = getActivity().getIntent().getIntExtra("categoryID", 0);
            servingSize = getActivity().getIntent().getIntExtra("servingSize", 0);
            imperialMetric = getActivity().getIntent().getBooleanExtra("imperialMetric",false);
            recipe = getActivity().getIntent().getParcelableExtra("recipe");
            detail_recipe = getActivity().getIntent().getParcelableExtra("detailRecipe");
            ingredients = detail_recipe.getIngredients();
            directions = detail_recipe.getDirections();
            id_glass_type = getActivity().getIntent().getStringExtra("GlassID");
            editRecipe = getActivity().getIntent().getExtras().getBoolean("editRecipe");
            recipeId = getActivity().getIntent().getExtras().getString("recipeId");
            changeImageRecipe = getActivity().getIntent().getExtras().getBoolean("changeImageRecipe");
        }
        width = getResources().getDisplayMetrics().widthPixels;
        Log.d("Measures","Width: " + width);
        width = (width / 2) - 40;
        Log.d("Measures","Width column: " + width);
        progressDialog = new ProgressDialog(getContext(),getString(R.string.please_wait));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_mood_matcher,container,false);

        final View newContainer = v.findViewById(R.id.filtersSelectorMoodMatcher);
        alcoholicButton = (Button) newContainer.findViewById(R.id.buttonAlcoholic);
        nonAlcoholicButton = (Button) newContainer.findViewById(R.id.buttonNonAlcoholic);
        coffeeButton = (Button) newContainer.findViewById(R.id.buttonCoffee);

        containerButtons = (GridLayout) v.findViewById(R.id.containerMoodButtons);
        optionsSelected = new ArrayList<>();

        alcoholicButton.setTextColor(getResources().getColor(isAlcoholic ? R.color.text_red_color : R.color.black_button));
        nonAlcoholicButton.setTextColor(getResources().getColor(isNonAlcoholic ? R.color.text_red_color : R.color.black_button));
        coffeeButton.setTextColor(getResources().getColor(isCoffee ? R.color.text_red_color : R.color.black_button));
        Button letSeeButton = (Button) v.findViewById(R.id.buttonLetSee);
        containerButtons.setUseDefaultMargins(true);
        Log.d("Measures","Width column: " + containerButtons.getWidth());
        letSeeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(optionsSelected.isEmpty()){
                    Utils.showSimpleMessage(getActivity(),getString(R.string.error),getString(R.string.error_message_empty_moodmatcher));
                    return;
                }
                MoninPreferences.setBoolean(getContext(), MoninPreferences.SHAREDPREFERENCE_KEY.KEY_IS_ALCOHOLIC, isAlcoholic);
                MoninPreferences.setBoolean(getContext(), MoninPreferences.SHAREDPREFERENCE_KEY.KEY_IS_COFFEE, isCoffee);
                MoninPreferences.setBoolean(getContext(), MoninPreferences.SHAREDPREFERENCE_KEY.KEY_IS_NON_ALCOHOLIC, isNonAlcoholic);
                MoninPreferences.setInteger(getContext(), MoninPreferences.SHAREDPREFERENCE_KEY.KEY_CURRENT_PAGE_MONIN, 1);
                Log.d(TAG, optionsSelected.toString());
                Intent intent = new Intent(getActivity(), com.softwaremobility.monin.ResultsMoodMatcher.class);
                intent.putExtra(getString(R.string.key_moods), true);
                intent.putCharSequenceArrayListExtra(getString(R.string.key_moodsIds), optionsSelected);
                startActivity(intent);
            }
        });

        alcoholicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAlcoholic = !isAlcoholic;
                isNonAlcoholic = false;
                isCoffee = false;
                alcoholicButton.setTextColor(getResources().getColor(isAlcoholic ? R.color.text_red_color : R.color.black_button));
                nonAlcoholicButton.setTextColor(getResources().getColor(R.color.black_button));
                coffeeButton.setTextColor(getResources().getColor(R.color.black_button));
            }
        });

        nonAlcoholicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isNonAlcoholic = !isNonAlcoholic;
                isCoffee = false;
                isAlcoholic = false;
                nonAlcoholicButton.setTextColor(getResources().getColor(isNonAlcoholic ? R.color.text_red_color : R.color.black_button));
                coffeeButton.setTextColor(getResources().getColor(R.color.black_button));
                alcoholicButton.setTextColor(getResources().getColor(R.color.black_button));
            }
        });

        coffeeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isCoffee = !isCoffee;
                isAlcoholic = false;
                isNonAlcoholic = false;
                coffeeButton.setTextColor(getResources().getColor(isCoffee ? R.color.text_red_color : R.color.black_button));
                alcoholicButton.setTextColor(getResources().getColor(R.color.black_button));
                nonAlcoholicButton.setTextColor(getResources().getColor(R.color.black_button));
            }
        });

        if (isUserCreation){
            newContainer.setVisibility(View.GONE);
            TextView label = (TextView)v.findViewById(R.id.labelMoodMatcher);
            label.setText(getString(R.string.moods_userrecipe_label));
            letSeeButton.setVisibility(View.GONE);
            Button saveAndShare = (Button)v.findViewById(R.id.buttonSaveAndShare);
            saveAndShare.setVisibility(View.VISIBLE);
            saveAndShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //sendRecipeToServer(recipe,directions,ingredients);
                }
            });
            saveAndShare.performClick();
        }else {
            getButtons();
        }

        boolean isGPSAllowed = MoninPreferences.getBoolean(getContext(), MoninPreferences.SHAREDPREFERENCE_KEY.KEY_GPS_ALLOWED);
        if (isGPSAllowed){
            if (PermissionsMarshmallow.permissionForGPSGranted(getActivity())){
                locman = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                currentLocation = locman.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                locman.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,2000,this);
            }
        }

        return v;
    }



    private void getButtons() {
        Map<String,String> headers = new HashMap<>();
        headers.put(getString(R.string.header_authorization), getString(R.string.header_generic_token,
                MoninPreferences.getString(getContext(), MoninPreferences.SHAREDPREFERENCE_KEY.KEY_GENERIC_APP_SECRET)));
        Map<String,String> params = new HashMap<>();
        Utils.addRegionParameters(getContext(),params,currentLocation, false);
        progressDialog.show();
        NetworkConnection.with(getActivity()).withListener(new NetworkConnection.ResponseListener() {
            @Override
            public void onSuccessfullyResponse(String response) {
                buttons = JSONUtils.with(getActivity()).getButtonsMoodMatcher(response);
                drawButtons();
                if (progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onErrorResponse(String error, String message, int code) {
                if (progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
                if (code == 105) {
                    Utils.showSimpleMessageFinishing(getActivity(),"",getString(R.string.error_no_internet));
                }
            }
        }).doRequest(Connection.REQUEST.GET, Uri.parse(getString(R.string.api_mood_matcher)), params, headers, null);
    }

    private void drawButtons() {
        int i = 0;
        int column = 0;
        for (ButtonMoodMatcher button : buttons){
            ToggleButton tButton = new ToggleButton(getContext());
            tButton.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            tButton.setId(button.getText().hashCode());
            tButton.setTextOff(button.getText());
            tButton.setWidth(width - 50);
            tButton.setTextSize(9f);
            tButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_gray));
            tButton.setText(button.getText());
            tButton.setTextOn(button.getText());
            tButton.setTag(button.getId());
            tButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        optionsSelected.add((String) buttonView.getTag());
                    } else {
                        optionsSelected.remove(buttonView.getTag());
                    }
                }
            });
            containerButtons.addView(tButton, new GridLayout.LayoutParams(
                    GridLayout.spec(i),
                    GridLayout.spec(column,1,1)
            ));
            column++;
            if (column > 1) {
                column = 0;
                i++;
            }
        }
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
