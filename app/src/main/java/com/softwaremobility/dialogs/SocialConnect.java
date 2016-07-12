package com.softwaremobility.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

import com.softwaremobility.fragments.Social_Connect_Buttons;
import com.softwaremobility.monin.R;


public class SocialConnect extends Dialog {

    private Activity a;
    private Social_Connect_Buttons fragmentButtons;

    public SocialConnect(Activity activity, Social_Connect_Buttons.ISocialLoginListener listener) {
        super(activity);
        a = activity;
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setCanceledOnTouchOutside(true);
        setContentView(R.layout.dialog_social_connect);

        fragmentButtons = (Social_Connect_Buttons) ((AppCompatActivity) activity).getSupportFragmentManager()
                .findFragmentById(R.id.fragmentButtons);
        fragmentButtons.setSocialLoginListener(listener);
    }

    @Override
    public void dismiss() {
        ((AppCompatActivity)a).getSupportFragmentManager().beginTransaction().remove(fragmentButtons).commit();
        super.dismiss();
    }
}
