package com.softwaremobility.listeners;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;

/**
 * Created by darkgeat on 11/9/16.
 */

public class AdsListener extends AdListener {

    private Context context;
    private String errorMessage;
    private final String TAG = AdsListener.class.getSimpleName();

    public AdsListener(Context c){
        context = c;
    }

    @Override
    public void onAdClosed() {
        Log.d(TAG,"AdClosed");
    }

    @Override
    public void onAdFailedToLoad(int errorCode) {
        errorMessage = "";
        switch (errorCode){
            case AdRequest.ERROR_CODE_INTERNAL_ERROR:
                errorMessage = "Internal error";
                break;
            case AdRequest.ERROR_CODE_INVALID_REQUEST:
                errorMessage = "Invalid request";
                break;
            case AdRequest.ERROR_CODE_NETWORK_ERROR:
                errorMessage = "Network error";
                break;
        }
        Log.d(TAG,"AdFailedToLoad" + errorCode + "messaage: " + errorMessage);
    }

    @Override
    public void onAdLeftApplication() {
        Log.d(TAG,"AdLeftApplication");
    }

    @Override
    public void onAdOpened() {
        Log.d(TAG,"AdOpened");
    }

    @Override
    public void onAdLoaded() {
        Log.d(TAG,"AdLoaded");
    }
}
