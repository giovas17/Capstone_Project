package com.softwaremobility.fragments;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.softwaremobility.adapters.RecipeAdapter;
import com.softwaremobility.json.JSONUtils;
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

/**
 * Created by darkgeat on 6/1/16.
 */
@SuppressWarnings("MissingPermission")
public class ResultsMoodMatcher extends Fragment implements LocationListener, OnChangeListStyleListener {

    private static final String TAG = ResultsMoodMatcher.class.getSimpleName();
    private int currentPage = 1;
    private boolean isAlcoholic = false, isCoffee = false, isNonAlcoholic = false;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LocationManager locman;
    private Location currentLocation;
    private ScrollListener listener;
    private ArrayList<CharSequence> optionsSelected;
    private RecyclerView.LayoutManager laman;
    private EmptyRecyclerView listRecipes;
    private RecipeAdapter adapter;
    private ArrayList<Recipe> recipes = new ArrayList<>();
    private boolean isGrid = true, areMorePages = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isAlcoholic = MoninPreferences.getBoolean(getContext(), MoninPreferences.SHAREDPREFERENCE_KEY.KEY_IS_ALCOHOLIC);
        isCoffee = MoninPreferences.getBoolean(getContext(), MoninPreferences.SHAREDPREFERENCE_KEY.KEY_IS_COFFEE);
        isNonAlcoholic = MoninPreferences.getBoolean(getContext(), MoninPreferences.SHAREDPREFERENCE_KEY.KEY_IS_NON_ALCOHOLIC);
        optionsSelected = getActivity().getIntent().getCharSequenceArrayListExtra(getString(R.string.key_moodsIds));
        boolean isGPSAllowed = MoninPreferences.getBoolean(getContext(), MoninPreferences.SHAREDPREFERENCE_KEY.KEY_GPS_ALLOWED);
        if (isGPSAllowed){
            if (PermissionsMarshmallow.permissionForGPSGranted(getActivity())){
                locman = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                currentLocation = locman.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                locman.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,2000,this);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_results_mood_matcher,container,false);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refreshingResultsLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent,R.color.text_orange_color,R.color.text_red_color);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                int auxiliarPage = 1;
                if (currentPage > 1){
                    auxiliarPage = currentPage;
                    currentPage = 1;
                }
                MakeAnUpdate();
                currentPage = auxiliarPage;
            }
        });

        adapter = new RecipeAdapter(getContext(),isGrid,recipes);
        listRecipes = (EmptyRecyclerView) view.findViewById(R.id.resultsRecipesRecycler);
        View empty = view.findViewById(R.id.emptyViewResults);
        listRecipes.setEmptyView(empty);
        laman = isGrid ? new GridLayoutManager(getContext(),2) : new LinearLayoutManager(getContext());
        refreshScrollListener(false);
        listRecipes.setLayoutManager(laman);
        listRecipes.setHasFixedSize(true);

        listRecipes.setAdapter(adapter);
        MakeAnUpdate();
        return view;
    }

    public void MakeAnUpdate(){
        Uri.Builder uriBuilder = Uri.parse(getString(R.string.api_mood_matcher_search)).buildUpon();
        Map<String,String> headers = new HashMap<>();
        headers.put(getString(R.string.header_authorization), getString(R.string.header_generic_token,
                MoninPreferences.getString(getContext(), MoninPreferences.SHAREDPREFERENCE_KEY.KEY_GENERIC_APP_SECRET)));
        Map<String,String> params = new HashMap<>();
        params.put(getString(R.string.key_pages), String.valueOf(currentPage));
        params.put(getString(R.string.key_categoryId), MoninRecipes.getCategory(isAlcoholic, isCoffee, isNonAlcoholic));
        if (optionsSelected.size() > 0) {
            for (CharSequence sequence : optionsSelected) {
                uriBuilder.appendQueryParameter(getString(R.string.key_moodsIds), sequence.toString());
            }
        }
        Uri uri = uriBuilder.build();
        swipeRefreshLayout.setRefreshing(true);
        NetworkConnection.with(getActivity()).withListener(new NetworkConnection.ResponseListener() {
            @Override
            public void onSuccessfullyResponse(String response) {
                try {
                    JSONObject object = new JSONObject(response);
                    JSONArray array; List<Recipe> dummyItems = null;
                    array = object.getJSONArray(getString(R.string.key_monin_recipes));
                    dummyItems = JSONUtils.with(getActivity()).getRecipes(array);
                    areMorePages = object.getBoolean(getString(R.string.key_more_pages));
                    refreshScrollListener(false);
                    for (Recipe recipe : dummyItems){
                        recipes.add(recipe);
                        adapter.notifyItemInserted(recipes.size() - 1);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onErrorResponse(String error, String message, int code) {
                if (swipeRefreshLayout.isRefreshing()) swipeRefreshLayout.setRefreshing(false);
                if (code == 105){
                    Utils.showSimpleMessage(getContext(),getString(R.string.error_title_no_internet), getString(R.string.error_no_internet));
                }
                if (code == 404){
                    Utils.showSimpleMessage(getContext(), "", getString(R.string.error_no_recipes_found));
                }
            }
        }).doRequest(Connection.REQUEST.GET, uri, params, headers, null);
    }

    private void refreshScrollListener(boolean shouldDeleteCurrent){
        if (shouldDeleteCurrent){currentPage = 1;}
        listener = new ScrollListener(laman,isGrid,currentPage-1) {
            @Override
            public void onLoadMore(int current_page) {
                if (current_page > currentPage && areMorePages){
                    currentPage++;
                    MakeAnUpdate();
                }
            }
        };
        listRecipes.addOnScrollListener(listener);
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
    public void onStop() {
        NetworkConnection.with(getContext()).cancelRequest();
        super.onStop();
    }
}
