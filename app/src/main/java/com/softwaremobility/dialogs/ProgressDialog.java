package com.softwaremobility.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.widget.TextView;

import com.softwaremobility.monin.R;

/**
 * Created by darkgeat on 5/10/16.
 */
public class ProgressDialog extends Dialog {

    private String messageDialog;

    public ProgressDialog(Context context, String message) {
        super(context);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setCanceledOnTouchOutside(false);
        setContentView(R.layout.dialog_progress);

        messageDialog = message;

        TextView text = (TextView)findViewById(R.id.textViewTitleDialogProgress);
        text.setText(messageDialog);
    }

    public void setMessageDialog(String messageDialog) {
        this.messageDialog = messageDialog;
    }
}
