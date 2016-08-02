package com.softwaremobility.monin;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.softwaremobility.objects.Ingredient;

import java.util.ArrayList;

public class MoodMatcher extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_matcher);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        Typeface typeface = Typeface.createFromAsset(getAssets(),getString(R.string.font_path));

        TextView title = (TextView) findViewById(R.id.title);
        title.setTypeface(typeface, Typeface.BOLD);
        boolean isUserCreation = getIntent().getBooleanExtra("userCreation", false);
        ArrayList<Ingredient> ingredients = getIntent().getParcelableArrayListExtra("ingredients");
        title.setText(getString(R.string.mood_matcher).toUpperCase());
        if(isUserCreation){
            title.setText(getString(R.string.recipe_creation).toUpperCase());
        }
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
}
