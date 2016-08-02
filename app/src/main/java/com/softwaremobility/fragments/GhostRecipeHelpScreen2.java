package com.softwaremobility.fragments;


import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.softwaremobility.monin.R;


/**
 * Created by developeri on 4/1/16.
 */
public class GhostRecipeHelpScreen2 extends Fragment {
    private TextView helpText;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(),getString(R.string.font_path));
        View view = inflater.inflate(R.layout.fragment_ghost_recipehelpscreen2, container, false);

        helpText = (TextView)view.findViewById(R.id.textViewCommunity);
        helpText.setTypeface(typeface);

        return view;
    }
    public static GhostRecipeHelpScreen2 newInstance(){
        return new GhostRecipeHelpScreen2();
    }
}
