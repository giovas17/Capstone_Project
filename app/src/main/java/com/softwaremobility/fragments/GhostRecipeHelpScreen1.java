package com.softwaremobility.fragments;


import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.softwaremobility.monin.R;


/**
 * Created by developeri on 3/31/16.
 */
public class GhostRecipeHelpScreen1 extends Fragment {
    private Toolbar ghostToolBarRecipe;
    private TextView helpText,testText;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(),getString(R.string.font_path));
        View view = inflater.inflate(R.layout.fragment_ghost_recipehelpscreen1, container, false);
        ghostToolBarRecipe = (Toolbar)view.findViewById(R.id.toolbarGhostRecipe1);
        ghostToolBarRecipe.inflateMenu(R.menu.menu_monin_recipes);
        setHasOptionsMenu(true);

        helpText = (TextView)view.findViewById(R.id.textViewCommunity);
        helpText.setTypeface(typeface);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Do something that differs the Activity's menu here
        super.onCreateOptionsMenu(menu, inflater);

    }

    public static GhostRecipeHelpScreen1 newInstance(){
        return new GhostRecipeHelpScreen1();
    }
}
