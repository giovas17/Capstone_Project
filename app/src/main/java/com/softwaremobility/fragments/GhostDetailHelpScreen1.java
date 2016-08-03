package com.softwaremobility.fragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.softwaremobility.monin.R;

/**
 * Created by developeri on 3/29/16.
 */
public class GhostDetailHelpScreen1 extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(),getString(R.string.font_path));
        View ghostView1 = inflater.inflate(R.layout.fragment_ghost_detailhelpscreen1,container,false);
        TextView helpText = (TextView)ghostView1.findViewById(R.id.textViewCommunityDetail);
        helpText.setTypeface(typeface);
        return ghostView1;

    }

    public static GhostDetailHelpScreen1 newInstance(){
        return new GhostDetailHelpScreen1();
    }
}


