package com.softwaremobility.dialogs;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.softwaremobility.monin.R;
import com.softwaremobility.network.Connection;
import com.softwaremobility.network.NetworkConnection;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ForgotPassword extends Dialog implements NetworkConnection.ResponseListener {

    private EditText email;

    public ForgotPassword(final Context context) {
        super(context);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setCanceledOnTouchOutside(true);
        setContentView(R.layout.dialog_forgot_password);

        email = (EditText) findViewById(R.id.emailForgot);
        Button okButton = (Button) findViewById(R.id.okForgot);
        Button cancelButton = (Button) findViewById(R.id.cancelForgot);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidEmail()) {
                    try {
                        JSONObject object = new JSONObject();
                        object.put(context.getString(R.string.key_email), email.getText().toString());
                        Map<String, String> headers = new HashMap<>();
                        headers.put(context.getString(R.string.key_content_type), context.getString(R.string.header_json));
                        Uri uri = Uri.parse(context.getString(R.string.api_forgot_password));
                        NetworkConnection.with(context).withListener(ForgotPassword.this)
                                .doRequest(Connection.REQUEST.POST, uri, null, headers, object);
                    } catch (JSONException e) {
                    }
                }
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private boolean isValidEmail() {
        email.setError(null);
        boolean goodPattern = Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches();
        if (email.getText().length() > 0){
            if (!goodPattern){
                email.setError(getContext().getString(R.string.error_invalid_email));
                email.requestFocus();
                return false;
            }
            return true;
        }else {
            email.setError(getContext().getString(R.string.error_field_required));
            email.requestFocus();
            return false;
        }

    }

    @Override
    public void onSuccessfullyResponse(String response) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(getContext().getString(R.string.forgot_password_success_message));
        builder.setPositiveButton(getContext().getString(R.string.ok), new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        builder.create().show();
        dismiss();
    }

    @Override
    public void onErrorResponse(String error, String message, int code) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(getContext().getString(R.string.forgot_password_error_message));
        builder.setPositiveButton(getContext().getString(R.string.ok), new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        builder.create().show();
        email.requestFocus();
    }
}
