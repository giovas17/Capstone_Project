package com.softwaremobility.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.Window;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.softwaremobility.monin.R;

/**
 * Created by darkgeat on 3/17/16.
 */
public class CompleteImage extends Dialog {

    private ImageView preview;

    public CompleteImage(Context context, String url){
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCanceledOnTouchOutside(true);
        setContentView(R.layout.dialog_image_complete);

        preview = (ImageView)findViewById(R.id.imageViewPreview);
        Glide.with(context).load(url).into(preview);
    }

    public CompleteImage(Context context, Bitmap bitmap){
        super(context);
        int widthScreen = (context.getResources().getDisplayMetrics().widthPixels * 90) / 100;
        int heightScreen = (context.getResources().getDisplayMetrics().heightPixels * 90) / 100;
        int width = (bitmap.getWidth() * 90) / 100;
        int height = (bitmap.getHeight() * 90) / 100;

        while(width > widthScreen || height > heightScreen){
            width = width * 90 / 100;
            height = height * 90 / 100;
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCanceledOnTouchOutside(true);
        setContentView(R.layout.dialog_image_complete);

        bitmap = Bitmap.createScaledBitmap(bitmap, width, height,false);

        preview = (ImageView)findViewById(R.id.imageViewPreview);
        preview.setImageBitmap(bitmap);
    }

}
