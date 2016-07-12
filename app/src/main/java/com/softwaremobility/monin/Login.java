package com.softwaremobility.monin;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.softwaremobility.dialogs.ForgotPassword;
import com.softwaremobility.preferences.MoninPreferences;
import com.softwaremobility.utilities.Utils;


public class Login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        Typeface typeface = Typeface.createFromAsset(getAssets(),getString(R.string.font_path));
        TextView title = (TextView) findViewById(R.id.title);
        title.setTypeface(typeface, Typeface.BOLD);
        title.setText(getString(R.string.titte_login).toUpperCase());

        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isTest = MoninPreferences.getBoolean(Login.this, MoninPreferences.SHAREDPREFERENCE_KEY.KEY_TYPE_BUILD);
                isTest = !isTest;
                MoninPreferences.setBoolean(Login.this, MoninPreferences.SHAREDPREFERENCE_KEY.KEY_TYPE_BUILD,isTest);
                Utils.showSimpleMessage(Login.this,"Changing Mode", "You change the config to: " + (isTest ? "Test" : "Production") + " mode");
            }
        });

        setSupportActionBar(toolbar);
        setTitle("");

    }

    public void ForgotPassword(View view) {
        ForgotPassword dialog = new ForgotPassword(this);
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.login_container);
        fragment.onActivityResult(requestCode,resultCode,data);
    }
}
