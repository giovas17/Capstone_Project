package com.softwaremobility.monin;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.softwaremobility.encryption.Encryptor;
import com.softwaremobility.network.NetworkConnection;
import com.softwaremobility.preferences.MoninPreferences;

public class Splash extends AppCompatActivity {

    private static final String TAG = Splash.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        NetworkConnection.testPath(getString(R.string.base_path_requests));
        NetworkConnection.productionPath(getString(R.string.base_path_production));

        String cipher = MoninPreferences.getString(this, MoninPreferences.SHAREDPREFERENCE_KEY.KEY_GENERIC_APP_SECRET);
        if (cipher.length()<1){
            String key = getString(R.string.key);
            String initVector = getString(R.string.initVector);
            String appId = getString(R.string.AppID);
            String appSecret = getString(R.string.AppSecret);
            String separator = getString(R.string.separator);
            String token = appId + separator + appSecret;
            cipher = Encryptor.encrypt(key,initVector,token);
            cipher = cipher.replace("\n","");
            Log.d(TAG, "Encryptor Generic Key: " + cipher);
            MoninPreferences.setString(this, MoninPreferences.SHAREDPREFERENCE_KEY.KEY_GENERIC_APP_SECRET,cipher);
        }

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    long tokenExpiryTime = MoninPreferences.getLong(Splash.this, MoninPreferences.SHAREDPREFERENCE_KEY.KEY_TOKEN_EXPIRY);
                    long tokenTimeSaved = MoninPreferences.getLong(Splash.this, MoninPreferences.SHAREDPREFERENCE_KEY.KEY_TOKEN_TIME);
                    long currentTime = System.currentTimeMillis();
                    tokenExpiryTime = tokenExpiryTime + tokenTimeSaved;

                    boolean showWalkThrough = MoninPreferences.getBoolean(Splash.this, MoninPreferences.SHAREDPREFERENCE_KEY.KEY_INITIAL_WALKTHROUGH);
                    Intent intent = tokenExpiryTime < currentTime ? new Intent(Splash.this, Login.class) : new Intent(Splash.this, Home.class);
                    startActivity(intent);
                    finish();
                }
            }
        };
        thread.start();
    }
}
