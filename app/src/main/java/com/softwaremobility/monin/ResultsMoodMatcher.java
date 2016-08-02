package com.softwaremobility.monin;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.softwaremobility.listeners.OnChangeListStyleListener;


/**
 * Created by darkgeat on 6/1/16.
 */
public class ResultsMoodMatcher extends AppCompatActivity {

    private boolean isGrid = false;
    private OnChangeListStyleListener listener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results_mood_matcher);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        Typeface typeface = Typeface.createFromAsset(getAssets(),getString(R.string.font_path));
        TextView title = (TextView) findViewById(R.id.title);
        title.setTypeface(typeface, Typeface.BOLD);
        title.setText(getString(R.string.mood_matcher).toUpperCase());
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("");

        com.softwaremobility.fragments.ResultsMoodMatcher fragment = (com.softwaremobility.fragments.ResultsMoodMatcher) getSupportFragmentManager().findFragmentById(R.id.fragment_results_mood_matcher);
        listener = (OnChangeListStyleListener) fragment;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            onBackPressed();
        }else if (item.getItemId() == R.id.action_list_type){
            item.setIcon(isGrid ? R.drawable.ic_list_recipes : R.drawable.ic_grid_recipes);
            isGrid = !isGrid;
            if (listener != null){
                listener.onChangeStyle(!isGrid);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_monin_recipes,menu);
        return true;
    }
}
