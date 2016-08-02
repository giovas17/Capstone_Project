package com.softwaremobility.fragments;


import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.softwaremobility.adapters.MoninListAdapter;
import com.softwaremobility.data.MoninContract;
import com.softwaremobility.data.MoninDataBase;
import com.softwaremobility.dialogs.ProgressDialog;
import com.softwaremobility.json.JSONUtils;
import com.softwaremobility.listeners.DownloadListener;
import com.softwaremobility.listeners.OnChangeListStyleListener;
import com.softwaremobility.monin.R;
import com.softwaremobility.network.Connection;
import com.softwaremobility.network.NetworkConnection;
import com.softwaremobility.objects.Recipe;
import com.softwaremobility.preferences.MoninPreferences;
import com.softwaremobility.utilities.EmptyRecyclerView;
import com.softwaremobility.utilities.PermissionsMarshmallow;
import com.softwaremobility.utilities.ScrollListener;
import com.softwaremobility.utilities.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("MissingPermission")
public class MoninRecipes extends Fragment implements OnChangeListStyleListener, LoaderManager.LoaderCallbacks<Cursor>,DownloadListener, LocationListener {

    private final String TAG = MoninRecipes.class.getSimpleName();
    public static boolean shouldLoadAgain = false, shouldFinish = false;
    private boolean changingFilter = false;
    private EmptyRecyclerView listRecipes;
    private ImageView filterSelector, regionSelector;
    private ViewGroup containerView;
    private EditText search;
    private ProgressDialog progressBar;
    private TextView allRegions, myRegion, usersFilter, recipesFilter;
    private Button alcoholicButton, nonAlcoholicButton, coffeeButton;
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean isGrid = true;
    private ScrollListener listener;
    private ArrayList<CharSequence> optionsSelected;
    private RecyclerView.LayoutManager laman;
    private MoninListAdapter adapter;
    private String searchTerm = null;
    public static final int LOADER_ID = 5;
    private int currentPage = 1;
    private LocationManager locman;
    private Location currentLocation;
    private MoninDataBase dataBase;
    private boolean isMoninRecipes = true,isAlcoholic = false, isNonAlcoholic = false,
            isCoffee = false,regionFilter = true, areMorePages = true;
    private boolean showFilters = false, showFiltersRegion = false;

    private static String[] columns = new String[]{
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isMoninRecipes = getActivity().getIntent().getBooleanExtra(MoninContract.MoninEntry.Key_IsMoninRecipe,true);

        boolean isGPSAllowed = MoninPreferences.getBoolean(getContext(), MoninPreferences.SHAREDPREFERENCE_KEY.KEY_GPS_ALLOWED);
        if (isGPSAllowed){
            if (PermissionsMarshmallow.permissionForGPSGranted(getActivity())){
                locman = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                currentLocation = locman.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                locman.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,2000,this);
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        currentPage = isMoninRecipes ?
                MoninPreferences.getInteger(getContext(), MoninPreferences.SHAREDPREFERENCE_KEY.KEY_CURRENT_PAGE_MONIN) :
                MoninPreferences.getInteger(getContext(), MoninPreferences.SHAREDPREFERENCE_KEY.KEY_CURRENT_PAGE_USER_RECIPES);
        isAlcoholic = MoninPreferences.getBoolean(getContext(), MoninPreferences.SHAREDPREFERENCE_KEY.KEY_IS_ALCOHOLIC);
        isCoffee = MoninPreferences.getBoolean(getContext(), MoninPreferences.SHAREDPREFERENCE_KEY.KEY_IS_COFFEE);
        isNonAlcoholic = MoninPreferences.getBoolean(getContext(), MoninPreferences.SHAREDPREFERENCE_KEY.KEY_IS_NON_ALCOHOLIC);
        regionFilter = MoninPreferences.getBoolean(getContext(), MoninPreferences.SHAREDPREFERENCE_KEY.KEY_FILTER_BY_REGION);
        getLoaderManager().initLoader(LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_monin_recipes, container, false);

        LinearLayout searchBar = (LinearLayout) view.findViewById(R.id.searchBar);
        progressBar = new ProgressDialog(getActivity(),getString(R.string.please_wait));

        dataBase = new MoninDataBase(getActivity());

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refreshingLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent,R.color.text_orange_color,R.color.text_red_color);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                int auxiliarPage = 1;
                searchTerm = search.getText().toString();
                if (currentPage > 1){
                    auxiliarPage = currentPage;
                    currentPage = 1;
                }
                MakeAnUpdate(false,false);
                currentPage = auxiliarPage;
            }
        });

        filterSelector = (ImageView) view.findViewById(R.id.filterSelectorSwitch);
        regionSelector = (ImageView) view.findViewById(R.id.regionselectorSwitch);
        search = (EditText) view.findViewById(R.id.editTextSearchRecipe);
        search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean action = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchTerm = v.getText().toString();
                    currentPage = 1;
                    if (!progressBar.isShowing()){ progressBar.show();}
                    MakeAnUpdate(true, true);
                    Utils.hideKeyboard(getActivity());
                }
                return action;
            }
        });
        containerView = (ViewGroup) view.findViewById(R.id.container_filters);

        filterSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilters = !showFilters;
                if (showFilters){
                    filterSelector.setImageResource(R.drawable.filter_active_icon);
                    if (showFiltersRegion){
                        regionSelector.performClick();
                    }
                    // ------- Add view ----------
                    addFiltersView();
                }else {
                    filterSelector.setImageResource(R.drawable.filter_inactive_icon);
                    // ------- Remove View ---------
                    removeFiltersSelector();
                }
            }
        });
        if (!isMoninRecipes){
            regionSelector.setVisibility(View.GONE);
        }
        regionSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFiltersRegion = !showFiltersRegion;
                if (showFiltersRegion){
                    regionSelector.setImageResource(R.drawable.region_active_icon);
                    if (showFilters){
                        filterSelector.performClick();
                    }
                    // ------ Add View --------
                    addFiltersRegionsView();
                }else {
                    regionSelector.setImageResource(R.drawable.region_inactive_icon);
                    // ------- Remove View --------
                    removeFiltersRegionSelector();
                }
            }
        });
        adapter = new MoninListAdapter(getActivity(), isGrid, isMoninRecipes);
        listRecipes = (EmptyRecyclerView) view.findViewById(R.id.moninRecipesRecycler);
        View empty = view.findViewById(R.id.emptyView);
        listRecipes.setEmptyView(empty);
        laman = isGrid ? new GridLayoutManager(getContext(),2) : new LinearLayoutManager(getContext());
        refreshScrollListener(false);
        listRecipes.setLayoutManager(laman);
        listRecipes.setHasFixedSize(true);

        listRecipes.setAdapter(adapter);
        if (dataBase.isEmpty(MoninContract.MoninEntry.TABLE_NAME,MoninContract.MoninEntry.Key_IdRecipe,
                MoninContract.MoninEntry.Key_IsMoninRecipe,isMoninRecipes ? "1" : "0")) {
            makeFirstLoad();
        }

        boolean isGPSAllowed = MoninPreferences.getBoolean(getContext(), MoninPreferences.SHAREDPREFERENCE_KEY.KEY_GPS_ALLOWED);
        if (isGPSAllowed){
            if (PermissionsMarshmallow.permissionForGPSGranted(getActivity())){
                locman = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                currentLocation = locman.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                locman.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,2000,this);
            }
        }

        return view;
    }

    private void makeFirstLoad() {
        boolean isProgress = progressBar.isShowing();
        if (!isProgress){
            progressBar.show();
        }
        if(Utils.isNetworkAvailable(getActivity())) {
            currentPage = 1;
            MakeAnUpdate(false, true);
        }else {
            if (progressBar.isShowing()){
                progressBar.dismiss();
            }
            Utils.showSimpleMessage(getActivity(),getString(R.string.error),getString(R.string.error_no_internet));
        }
    }

    private void refreshScrollListener(boolean shouldDeleteCurrent){
        if (shouldDeleteCurrent){currentPage = 1;}
        listener = new ScrollListener(laman,isGrid,currentPage-1) {
            @Override
            public void onLoadMore(int current_page) {
                if (current_page == currentPage && areMorePages && !swipeRefreshLayout.isRefreshing()){
                    currentPage++;
                    MakeAnUpdate(false,true);
                }
            }
        };
        listRecipes.addOnScrollListener(listener);
    }

    private void removeFiltersSelector() {
        containerView.removeViewAt(0);
    }

    private void addFiltersView() {
        final ViewGroup newContainer = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.layout_filters_selection,
                containerView, false);
        alcoholicButton = (Button) newContainer.findViewById(R.id.buttonAlcoholic);
        nonAlcoholicButton = (Button) newContainer.findViewById(R.id.buttonNonAlcoholic);
        coffeeButton = (Button) newContainer.findViewById(R.id.buttonCoffee);

        alcoholicButton.setTextColor(getResources().getColor(isAlcoholic ? R.color.text_red_color : R.color.black_button));
        nonAlcoholicButton.setTextColor(getResources().getColor(isNonAlcoholic ? R.color.text_red_color : R.color.black_button));
        coffeeButton.setTextColor(getResources().getColor(isCoffee ? R.color.text_red_color : R.color.black_button));

        alcoholicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAlcoholic = !isAlcoholic;
                isNonAlcoholic = false;
                isCoffee = false;
                alcoholicButton.setTextColor(getResources().getColor(isAlcoholic ? R.color.text_red_color : R.color.black_button));
                nonAlcoholicButton.setTextColor(getResources().getColor(R.color.black_button));
                coffeeButton.setTextColor(getResources().getColor(R.color.black_button));
                changingFilter = true;
                MakeAnUpdate(true,true);
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
                changingFilter = true;
                MakeAnUpdate(true,true);
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
                changingFilter = true;
                MakeAnUpdate(true, true);
            }
        });
        containerView.addView(newContainer, 0);
    }

    private void removeFiltersRegionSelector() {
        containerView.removeViewAt(0);
    }

    private void addFiltersRegionsView() {
        final ViewGroup newContainer = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.layout_filters_region_selection,
                containerView, false);

        allRegions = (TextView) newContainer.findViewById(R.id.filterAllRecipes);
        myRegion = (TextView) newContainer.findViewById(R.id.filterYourRegion);
        if (regionFilter){
            allRegions.setTextColor(getContext().getResources().getColor(R.color.black_button));
            myRegion.setTextColor(getContext().getResources().getColor(R.color.text_red_color));
        }else {
            allRegions.setTextColor(getContext().getResources().getColor(R.color.text_red_color));
            myRegion.setTextColor(getContext().getResources().getColor(R.color.black_button));
        }

        allRegions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean change = true;
                if (!regionFilter) change = false;
                regionFilter = false;
                allRegions.setTextColor(getContext().getResources().getColor(R.color.text_red_color));
                myRegion.setTextColor(getContext().getResources().getColor(R.color.black_button));
                changingFilter = true;
                if (change) {
                    MakeAnUpdate(true,true);
                }
            }
        });

        myRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean change = true;
                if (regionFilter) change = false;
                regionFilter = true;
                allRegions.setTextColor(getContext().getResources().getColor(R.color.black_button));
                myRegion.setTextColor(getContext().getResources().getColor(R.color.text_red_color));
                changingFilter = true;
                if (change){
                    MakeAnUpdate(true,true);
                }
            }
        });
        containerView.addView(newContainer, 0);
    }



    public void MakeAnUpdate(final boolean shouldRemoveData, final boolean shouldShowProgressDialog){
        Uri uri;
        Map<String,String> headers = new HashMap<>();
        if (MoninPreferences.getBoolean(getContext(), MoninPreferences.SHAREDPREFERENCE_KEY.KEY_IS_NO_LOGIN)){  //No Login
            headers.put(getString(R.string.header_authorization), getString(R.string.header_generic_token,
                    MoninPreferences.getString(getContext(), MoninPreferences.SHAREDPREFERENCE_KEY.KEY_GENERIC_APP_SECRET)));
        }else {  //------------------------------- Normal Login ----------------------------------------------------
            headers.put(getString(R.string.header_authorization), getString(R.string.header_token,
                    MoninPreferences.getString(getContext(), MoninPreferences.SHAREDPREFERENCE_KEY.KEY_TOKEN)));
        }
        Map<String,String> params = new HashMap<>();
        params.put(getString(R.string.key_pages), String.valueOf(currentPage));
        if (isMoninRecipes) {
            uri = Uri.parse(getString(R.string.api_monin_recipes));
            headers.clear();
            headers.put(getString(R.string.header_authorization), getString(R.string.header_generic_token,
                    MoninPreferences.getString(getContext(), MoninPreferences.SHAREDPREFERENCE_KEY.KEY_GENERIC_APP_SECRET)));
            params.put(getString(R.string.key_category), getCategory(isAlcoholic, isCoffee, isNonAlcoholic));
            Utils.addRegionParameters(getContext(),params,currentLocation, regionFilter);
        } else { // ------------- User Recipes ------------
            uri = Uri.parse(getString(R.string.api_user_recipes));
            params.put(getString(R.string.key_categoryId), getCategory(isAlcoholic, isCoffee, isNonAlcoholic));
        }
        if (searchTerm != null && searchTerm.length() > 0){
            params.put(getString(R.string.key_search_term),searchTerm);
        }
        if (shouldShowProgressDialog) {
            swipeRefreshLayout.setRefreshing(true);
        }
        NetworkConnection.with(getActivity()).withListener(new NetworkConnection.ResponseListener() {
            @Override
            public void onSuccessfullyResponse(String response) {
                try {
                    JSONObject object = new JSONObject(response);
                    JSONArray array; List<Recipe> dummyItems = null;
                    Uri uri;
                    searchTerm = "";
                    if (isMoninRecipes) {
                        array = object.getJSONArray(getString(R.string.key_monin_recipes));
                        dummyItems = JSONUtils.with(getActivity()).getRecipes(array);
                    } else {
                        array = object.getJSONArray(getString(R.string.key_user_recipes));
                        dummyItems = JSONUtils.with(getActivity()).getUserRecipes(array);
                    }
                    uri = MoninContract.MoninEntry.buildMoninWithRegion(regionFilter,isAlcoholic,
                            isCoffee,isNonAlcoholic,isMoninRecipes,searchTerm, false);
                    areMorePages = object.getBoolean(getString(R.string.key_more_pages));
                    refreshScrollListener(shouldRemoveData);
                    if (shouldRemoveData){
                        dataBase.DeleteDataFromTable(MoninContract.MoninEntry.TABLE_NAME);
                    }
                    Utils.storeRecipesAndRefreshLoader(dummyItems, getActivity(), isMoninRecipes,
                            regionFilter, uri, currentPage, MoninRecipes.this);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onErrorResponse(String error, String message, int code) {
                if (swipeRefreshLayout.isRefreshing()) swipeRefreshLayout.setRefreshing(false);
                if (code == 105){
                    if (getLoaderManager().getLoader(LOADER_ID).isStarted()){
                        getLoaderManager().getLoader(LOADER_ID).cancelLoad();
                    }
                    getLoaderManager().restartLoader(LOADER_ID,null,MoninRecipes.this);
                    if (progressBar.isShowing()){
                        progressBar.dismiss();
                    }
                    //Utils.showSimpleMessage(getContext(),getString(R.string.error), getString(R.string.error_no_internet));
                }
                if (code == 404){
                    getLoaderManager().restartLoader(LOADER_ID,null,MoninRecipes.this);
                    if (progressBar.isShowing()){
                        progressBar.dismiss();
                    }
                }
            }
        }).doRequest(Connection.REQUEST.GET, uri, params, headers, null);
    }

    @Override
    public void onPause() {
        if (swipeRefreshLayout.isRefreshing()) swipeRefreshLayout.setRefreshing(false);
        super.onPause();
    }

    @Override
    public void onChangeStyle(boolean isGridStyle) {
        Log.d(TAG, "Im on OnChangeStyle, isGrid: " + isGridStyle);
        isGrid = isGridStyle;
        laman = isGrid ? new GridLayoutManager(getContext(),2) : new LinearLayoutManager(getContext());
        refreshScrollListener(false);
        adapter.setIsGrid(isGrid);
        listRecipes.setLayoutManager(laman);
        listRecipes.setHasFixedSize(true);
        listRecipes.setAdapter(adapter);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = MoninContract.MoninEntry.Key_Rating + " DESC";
        Uri uri = MoninContract.MoninEntry.buildMoninWithRegion(regionFilter, isAlcoholic, isCoffee, isNonAlcoholic, isMoninRecipes, searchTerm, false);
        Loader<Cursor> loader = new CursorLoader(getActivity(),uri,columns,null,null,sortOrder);
        return loader;
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (swipeRefreshLayout.isRefreshing()) swipeRefreshLayout.setRefreshing(false);
        if (progressBar.isShowing()) progressBar.dismiss();
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    public static String getCategory(boolean isAlcoholic, boolean isCoffee, boolean isNonAlcoholic){
        if (isAlcoholic){
            return "1";
        }
        if (isCoffee){
            return "3";
        }
        if (isNonAlcoholic){
            return "2";
        }else {
            return "0";
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (shouldFinish){
            Home.shouldFinish = true;
            getActivity().finish();
            shouldFinish = false;
        }
        if (shouldLoadAgain){
            shouldLoadAgain = false;
            getLoaderManager().restartLoader(LOADER_ID,null,this);
        }
    }

    @Override
    public void OnLoadingFinished() {
        boolean isProgress = progressBar.isShowing();
        if (isProgress){ progressBar.dismiss();}
        if (changingFilter){
            getLoaderManager().restartLoader(LOADER_ID,null,this);
            changingFilter = false;
        }else {
            getLoaderManager().restartLoader(LOADER_ID,null,this);
           // getLoaderManager().getLoader(LOADER_ID).onContentChanged();
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
