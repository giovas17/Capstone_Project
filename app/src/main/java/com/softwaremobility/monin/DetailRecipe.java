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
import com.softwaremobility.fragments.GhostDetailHelpScreen1;
import com.softwaremobility.fragments.GhostDetailHelpScreen2;
import com.softwaremobility.preferences.MoninPreferences;

public class DetailRecipe extends AppCompatActivity {
    private RelativeLayout ghostScreenDetail;
    private ViewPager viewPager;
    private RadioGroup radioGroup;
    private TextView nextTip,skipTip,gotIT;
    private Toolbar toolbarGhost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        Typeface typeface = Typeface.createFromAsset(getAssets(),getString(R.string.font_path));

        TextView title = (TextView) findViewById(R.id.title);
        title.setTypeface(typeface,Typeface.BOLD);
        title.setText(getString(R.string.detail_recipe).toUpperCase());

        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("");

        toolbarGhost = (Toolbar)findViewById(R.id.toolbarGhostDetail);
        radioGroup = (RadioGroup) findViewById(R.id.radiogroupDetail);
        viewPager = (ViewPager) findViewById(R.id.viewpagerDetail);
        PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        nextTip = (TextView) findViewById(R.id.textViewGotItDetailNextTip);
        skipTip = (TextView) findViewById(R.id.textViewGotItDetailSkipTip);
        gotIT = (TextView) findViewById(R.id.textViewDetailGotIt);

        nextTip.setTypeface(typeface);
        nextTip.setPaintFlags(nextTip.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        skipTip.setTypeface(typeface);
        skipTip.setPaintFlags(skipTip.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        gotIT.setTypeface(typeface);
        gotIT.setPaintFlags(skipTip.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);


        nextTip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextTip.setVisibility(View.INVISIBLE);
                viewPager.setCurrentItem(1);
                gotIT.setVisibility(View.VISIBLE);
            }
        });

        gotIT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toolbarGhost.setVisibility(View.INVISIBLE);
                ghostScreenDetail.setVisibility(View.GONE);
            }
        });

        TextView skipTip = (TextView) findViewById(R.id.textViewGotItDetailSkipTip);
        skipTip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toolbarGhost.setVisibility(View.INVISIBLE);
                ghostScreenDetail.setVisibility(View.GONE);
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
                        radioGroup.check(R.id.radiodetail1);
                        break;
                    case 1:
                        radioGroup.check(R.id.radiodetail2);
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
                if (checkedId == R.id.radiodetail1){
                    nextTip.setVisibility(View.VISIBLE);
                    viewPager.setCurrentItem(0);
                    gotIT.setVisibility(View.INVISIBLE);
                }else if (checkedId == R.id.radiodetail2){
                    nextTip.setVisibility(View.INVISIBLE);
                    viewPager.setCurrentItem(1);
                    gotIT.setVisibility(View.VISIBLE);
                }
            }
        });

        ghostScreenDetail = (RelativeLayout) findViewById(R.id.ghostScreenDetail);


        if (!MoninPreferences.getBoolean(this, MoninPreferences.SHAREDPREFERENCE_KEY.KEY_GHOST_DETAIL)){
            toolbarGhost.setVisibility(View.INVISIBLE);
            ghostScreenDetail.setVisibility(View.GONE);
        }else {
            MoninPreferences.setBoolean(this, MoninPreferences.SHAREDPREFERENCE_KEY.KEY_GHOST_DETAIL,false);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user_profile, menu);
        if(getIntent().getExtras().getBoolean(MoninContract.MoninEntry.Key_IsMoninRecipe)){
            MenuItem menuItem = menu.getItem(0);
            menuItem.setVisible(false);
        }
        MenuItem menuItem2 = menu.getItem(1);
        menuItem2.setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private class PagerAdapter extends FragmentPagerAdapter {

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return GhostDetailHelpScreen1.newInstance();
                case 1:
                    return GhostDetailHelpScreen2.newInstance();
                default:
                    return GhostDetailHelpScreen1.newInstance();
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
