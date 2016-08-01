package com.softwaremobility.monin;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.softwaremobility.data.MoninContract;
import com.softwaremobility.fragments.GhostRecipeHelpScreen1;
import com.softwaremobility.fragments.GhostRecipeHelpScreen2;
import com.softwaremobility.fragments.GhostRecipeHelpScreen3;
import com.softwaremobility.listeners.OnChangeListStyleListener;
import com.softwaremobility.preferences.MoninPreferences;

public class MoninRecipes extends AppCompatActivity {
    private RelativeLayout ghostScreenRecipe;
    //private Toolbar ghostToolBarRecipe;
    private boolean isGrid = false;
    private OnChangeListStyleListener listener;
    private ViewPager viewPager;
    private RadioGroup radioGroup;
    private TextView nextTip,skipTip,gotIT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monin_recipes);

        boolean isMoodMatcher = getIntent().getBooleanExtra(getString(R.string.key_moods), false);
        boolean isMoninRecipes = getIntent().getBooleanExtra(MoninContract.MoninEntry.Key_IsMoninRecipe,true);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        Typeface typeface = Typeface.createFromAsset(getAssets(),getString(R.string.font_path));
        TextView title = (TextView) findViewById(R.id.title);
        title.setTypeface(typeface, Typeface.BOLD);
        if (isMoodMatcher){
            title.setText(getString(R.string.mood_matcher).toUpperCase());
        }else {
            title.setText(getString(R.string.monin_recipes).toUpperCase());
        }
        if (!isMoninRecipes){
            title.setText(getString(R.string.user_recipes).toUpperCase());
        }

        com.softwaremobility.fragments.MoninRecipes fragment = (com.softwaremobility.fragments.MoninRecipes)getSupportFragmentManager().findFragmentById(R.id.fragmentMoninRecipes);
        listener = (OnChangeListStyleListener) fragment;

        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("");

        ghostScreenRecipe = (RelativeLayout) findViewById(R.id.ghostScreenRecipe);

        radioGroup = (RadioGroup) findViewById(R.id.radiogroupRecipe);
        viewPager = (ViewPager) findViewById(R.id.viewpagerRecipe);
        PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        nextTip = (TextView) findViewById(R.id.textViewGotItRecipeNextTip);
        skipTip = (TextView) findViewById(R.id.textViewGotItRecipeSkipTip);
        gotIT = (TextView) findViewById(R.id.textViewRecipeGotIt);

        nextTip.setTypeface(typeface);
        nextTip.setPaintFlags(nextTip.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        skipTip.setTypeface(typeface);
        skipTip.setPaintFlags(skipTip.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        gotIT.setTypeface(typeface);
        gotIT.setPaintFlags(skipTip.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        nextTip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(viewPager.getCurrentItem() == 0){
                    nextTip.setVisibility(View.VISIBLE);
                    viewPager.setCurrentItem(1);
                    gotIT.setVisibility(View.INVISIBLE);

                }else if(viewPager.getCurrentItem() == 1){
                    nextTip.setVisibility(View.INVISIBLE);
                    viewPager.setCurrentItem(2);
                    gotIT.setVisibility(View.VISIBLE);
                }
            }
        });

        gotIT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 ghostScreenRecipe.setVisibility(View.GONE);
            }
        });

        skipTip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ghostScreenRecipe.setVisibility(View.GONE);
            }
        });


        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        radioGroup.check(R.id.radiorecipe1);
                        break;
                    case 1:
                        radioGroup.check(R.id.radiorecipe2);
                        break;
                    case 2:
                        radioGroup.check(R.id.radiorecipe3);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radiorecipe1){
                    nextTip.setVisibility(View.VISIBLE);
                    viewPager.setCurrentItem(0);
                    gotIT.setVisibility(View.INVISIBLE);
                }else if (checkedId == R.id.radiorecipe2){
                    nextTip.setVisibility(View.VISIBLE);
                    viewPager.setCurrentItem(1);
                    gotIT.setVisibility(View.INVISIBLE);
                }else{
                    nextTip.setVisibility(View.INVISIBLE);
                    viewPager.setCurrentItem(2);
                    gotIT.setVisibility(View.VISIBLE);


                }
            }
        });

        if ((!isMoninRecipes) || !MoninPreferences.getBoolean(this, MoninPreferences.SHAREDPREFERENCE_KEY.KEY_GHOST_RECIPE)){
            ghostScreenRecipe.setVisibility(View.GONE);
        }else {
            MoninPreferences.setBoolean(this, MoninPreferences.SHAREDPREFERENCE_KEY.KEY_GHOST_RECIPE,false);

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
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


    private class PagerAdapter extends FragmentPagerAdapter {

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return GhostRecipeHelpScreen1.newInstance();
                case 1:
                    return GhostRecipeHelpScreen2.newInstance();
                default:
                    return GhostRecipeHelpScreen3.newInstance();
            }
        }


        @Override
        public int getCount() {
            return 3;
        }
    }
}
