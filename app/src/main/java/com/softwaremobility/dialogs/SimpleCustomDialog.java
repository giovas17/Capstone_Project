package com.softwaremobility.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.softwaremobility.monin.R;

/**
 * Created by darkgeat on 6/8/16.
 */
public class SimpleCustomDialog extends Dialog {

    private Button okButton, cancelButton;

    public SimpleCustomDialog(final Context context, String Title, String message, final okListener listener) {
        super(context);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setCanceledOnTouchOutside(true);
        setContentView(R.layout.dialog_simple);

        TextView titleLabel = (TextView) findViewById(R.id.textView7);
        TextView messageLabel = (TextView) findViewById(R.id.messageDialog);

        titleLabel.setText(Title);
        messageLabel.setText(message);
        okButton = (Button) findViewById(R.id.okDialog);
        cancelButton = (Button) findViewById(R.id.cancelDialog);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.OnOkSelected();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.OnCancelSelected();
                dismiss();
            }
        });
    }

    public interface okListener{
        void OnOkSelected();
        void OnCancelSelected();
    }

    public void setOkButtonText(String textButton){
        okButton.setText(textButton);
    }

    public void hideCancelButton(){
        cancelButton.setVisibility(View.GONE);
    }
}
