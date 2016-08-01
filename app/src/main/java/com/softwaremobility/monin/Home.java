package com.softwaremobility.monin;

import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.softwaremobility.data.MoninContract;
import com.softwaremobility.dialogs.SimpleCustomDialog;
import com.softwaremobility.preferences.MoninPreferences;
import com.softwaremobility.utilities.Utils;

import java.util.Locale;

public class Home extends AppCompatActivity {

    private RelativeLayout ghostScreen;
    private Intent intent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        Typeface typeface = Typeface.createFromAsset(getAssets(),getString(R.string.font_path));
        Toolbar ghostToolbar = (Toolbar) findViewById(R.id.toolbarGhost);
        ghostToolbar.inflateMenu(R.menu.menu_home);

        ghostScreen = (RelativeLayout) findViewById(R.id.ghostScreenHome);

        if (!MoninPreferences.getBoolean(this, MoninPreferences.SHAREDPREFERENCE_KEY.KEY_GHOST_HOME)){
            ghostScreen.setVisibility(View.GONE);
        }else {
            MoninPreferences.setBoolean(this, MoninPreferences.SHAREDPREFERENCE_KEY.KEY_GHOST_HOME,false);
        }

        TextView textGhost = (TextView) findViewById(R.id.textViewCommunity);
        textGhost.setTypeface(typeface);

        TextView textGotIt = (TextView) findViewById(R.id.textViewGotIt);
        textGotIt.setTypeface(typeface);
        textGotIt.setPaintFlags(textGotIt.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        textGotIt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ghostScreen.setVisibility(View.GONE);
            }
        });

        ImageView moninLogo = (ImageView)findViewById(R.id.titleImage);
        moninLogo.setVisibility(View.VISIBLE);
        moninLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.showSimpleMessage(Home.this, "Data", "Region: " + Locale.getDefault().getCountry() +
                        "\nSprint: 3\nVersion: 3");
            }
        });

        TextView title = (TextView) findViewById(R.id.title);
        title.setVisibility(View.GONE);

        setSupportActionBar(toolbar);
        setTitle("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings){
            if (MoninPreferences.getBoolean(this, MoninPreferences.SHAREDPREFERENCE_KEY.KEY_IS_NO_LOGIN)){
                Intent intent = new Intent(this, Login.class);
                startActivity(intent);
                finish();
            }else {
                Utils.logOut(this);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void Selector(View view) {
        if (view.getId() == R.id.imageOfficialMonin){
            intent = new Intent(this, MoninRecipes.class);
            startActivity(intent);
        }else {
            boolean isNoLogin = MoninPreferences.getBoolean(this, MoninPreferences.SHAREDPREFERENCE_KEY.KEY_IS_NO_LOGIN);
            if (isNoLogin){
                SimpleCustomDialog dialog = new SimpleCustomDialog(this, getString(R.string.error_title_no_login), getString(R.string.error_no_login), new SimpleCustomDialog.okListener() {
                    @Override
                    public void OnOkSelected() {
                        Intent intent = new Intent(Home.this, Login.class);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void OnCancelSelected() {

                    }
                });
                dialog.setOkButtonText(getString(R.string.action_login));
                dialog.show();
            }else {
                intent = new Intent(this, MoninRecipes.class);
                intent.putExtra(MoninContract.MoninEntry.Key_IsMoninRecipe, false);
                startActivity(intent);
            }
        }
    }
}
