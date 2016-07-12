package com.softwaremobility.fragments;


import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.softwaremobility.monin.R;
import com.softwaremobility.preferences.MoninPreferences;
import com.softwaremobility.utilities.PermissionsMarshmallow;

public class Home extends Fragment implements LocationListener{

    private LocationManager locman;
    private Location currentLocation;
    public static boolean shouldFinish = false;

    @SuppressWarnings("MissingPermission")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home,container,false);

        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(),getString(R.string.font_path));

        TextView officially = (TextView)view.findViewById(R.id.textOfficiallyTitle);
        TextView moodMatcher = (TextView)view.findViewById(R.id.textMoodMatcher);
        TextView myRecipes = (TextView)view.findViewById(R.id.textSlideTitle);

        officially.setTypeface(typeface,Typeface.BOLD);
        moodMatcher.setTypeface(typeface,Typeface.BOLD);
        myRecipes.setTypeface(typeface,Typeface.BOLD);

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

    @Override
    public void onResume() {
        super.onResume();
        if (shouldFinish){
            shouldFinish = false;
            getActivity().finish();
            Intent intent = new Intent(getContext(), Login.class);
            getContext().startActivity(intent);
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
