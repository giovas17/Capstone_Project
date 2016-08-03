package com.softwaremobility.monin;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.softwaremobility.listeners.CroppingListener;


/**
 * Created by darkgeat on 5/3/16.
 */
public class PreviewCropImage extends AppCompatActivity {

    private CroppingListener listener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_image);

        listener = (CroppingListener) getSupportFragmentManager().findFragmentById(R.id.cropImageFragment);

        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.OnCropImage();
            }
        });
    }
}
