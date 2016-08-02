package com.softwaremobility.monin;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.softwaremobility.utilities.MediaUtilities;

public class CreateRecipes extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_recipe);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(getIntent().getExtras().getBoolean("editRecipe")){
            toolbar.setNavigationIcon(R.drawable.ic_clear_black);
        }
        Typeface typeface = Typeface.createFromAsset(getAssets(),getString(R.string.font_path));
        TextView title = (TextView) findViewById(R.id.title);
        title.setTypeface(typeface,Typeface.BOLD);
        title.setText(getString(R.string.recipe_creation).toUpperCase());

        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(getIntent().getExtras().getBoolean("editRecipe")){
            getMenuInflater().inflate(R.menu.menu_edit_recipe, menu);
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MediaUtilities.REQUEST_CODE){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                Log.d("RequestPermissions", "ya fueron aceptadas");
                //MediaUtilities.dispatchTakePictureIntent(getSupportFragmentManager().findFragmentById(R.id.fragmentCreateRecipe));
            }else {
                Log.d("RequestPermissions", "NO fueron aceptadas");
            }
        }
    }
}
