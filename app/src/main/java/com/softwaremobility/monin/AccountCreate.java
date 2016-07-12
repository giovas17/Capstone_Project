package com.softwaremobility.monin;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.softwaremobility.fragments.Social_Connect_Buttons;
import com.softwaremobility.json.JSONUtils;
import com.softwaremobility.network.Connection;
import com.softwaremobility.network.NetworkConnection;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class AccountCreate extends AppCompatActivity implements Social_Connect_Buttons.ISocialLoginListener, NetworkConnection.ResponseListener {

    private static final String TAG = AccountCreate.class.getSimpleName();
    private String provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_create);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        Typeface typeface = Typeface.createFromAsset(getAssets(),getString(R.string.font_path));
        TextView title = (TextView) findViewById(R.id.title);
        title.setTypeface(typeface);
        title.setText(getString(R.string.tittle_account_creation).toUpperCase());

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("");

        Social_Connect_Buttons social_connect_buttons = (Social_Connect_Buttons)getSupportFragmentManager()
                .findFragmentById(R.id.socialButtonsFragment);
        social_connect_buttons.setSocialLoginListener(this);

    }

    @Override
    public void onLoginSuccess(JSONObject data, String provider) {
        this.provider = provider;
        Uri endpoint = Uri.parse(getString(R.string.api_external_register));
        try {
            Map<String,String> headers = new HashMap<>();
            headers.put(getString(R.string.key_content_type), getString(R.string.header_json));
            JSONObject dat = data;
            dat.put(getString(R.string.key_createddatetime),System.currentTimeMillis());
            dat.put(getString(R.string.key_latitude), "99.903248");
            dat.put(getString(R.string.key_longitude), "20.394054321");
            NetworkConnection.with(this).withListener(this)
                    .doRequest(Connection.REQUEST.POST, endpoint, null, headers, dat);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLoginError(int errCode, String errorMessage) {
        Log.i(TAG, "onLoginError() errCode:" + errCode + ",errorMessage:" + errorMessage);
    }

    @Override
    public void onLoginCancelled() {

    }

    @Override
    public void onLoginProgress() {

    }

    @Override
    public void onSuccessfullyResponse(String response) {
        Log.i(TAG, "onLoginSuccess()" + response);
        try {
            JSONUtils.with(this).updateToken(new JSONObject(response), provider);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(this, Home.class);
        startActivity(intent);
        finish();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            overridePendingTransition(R.anim.left_in, R.anim.left_out);
        }
    }

    @Override
    public void onErrorResponse(String error, String message, int code) {
        Log.i(TAG, "onLoginError() errCode:" + code + ",errorMessage:" + message);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home){
            onBackPressed();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this,Login.class);
        startActivity(intent);
        finish();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            overridePendingTransition(R.anim.right_in, R.anim.right_in);
        }
    }
}
