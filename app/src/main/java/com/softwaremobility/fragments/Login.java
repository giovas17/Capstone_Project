package com.softwaremobility.fragments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.softwaremobility.monin.R;
import com.softwaremobility.network.Connection;
import com.softwaremobility.network.NetworkConnection;
import com.softwaremobility.preferences.MoninPreferences;
import com.softwaremobility.utilities.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Login extends Fragment implements View.OnClickListener, TextView.OnEditorActionListener,
        Social_Connect_Buttons.ISocialLoginListener,NetworkConnection.ResponseListener {

    public static final String TAG = Login.class.getSimpleName();
    private final int ACTION_SIGNIN = 1000;
    private String provider = null;

    private EditText mUserNameEditText;
    private EditText mPasswordEditText;
    private TextView mForgotPassword;
    private Button mLogInButton;
    private ProgressDialog progressDialog;


    public Login() {

    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setIndeterminate(true);
        progressDialog.setTitle(getString(R.string.please_wait));
        progressDialog.setMessage(getString(R.string.login_progress));
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_login, container, false);

        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(),"fonts/mermaid.ttf");

        TextView mLabel = (TextView) v.findViewById(R.id.label);
        mForgotPassword = (TextView) v.findViewById(R.id.forgot_password);

        mLabel.setTypeface(typeface);
        mForgotPassword.setTypeface(typeface);
        mForgotPassword.setPaintFlags(mForgotPassword.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        mLogInButton = (Button) v.findViewById(R.id.login);
        mLogInButton.setOnClickListener(this);

        mUserNameEditText = (EditText) v.findViewById(R.id.login_username);

        mPasswordEditText = (EditText) v.findViewById(R.id.login_password);
        mPasswordEditText.setTypeface(Typeface.DEFAULT);
        mPasswordEditText.setTransformationMethod(new PasswordTransformationMethod());
        mPasswordEditText.setImeActionLabel(getString(R.string.action_login), ACTION_SIGNIN);
        mPasswordEditText.setOnEditorActionListener(this);

        Button createAccountButton = (Button) v.findViewById(R.id.create_account);
        createAccountButton.setOnClickListener(this);

        Button socialConnectButton = (Button) v.findViewById(R.id.social_connect);
        socialConnectButton.setOnClickListener(this);

        Button noLogin = (Button) v.findViewById(R.id.noLoginButton);
        noLogin.setOnClickListener(this);

        return v;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.login: {
                if (Utils.isNetworkAvailable(getContext())) {
                    login();
                }
                break;
            }
            case R.id.social_connect: {
                if (Utils.isNetworkAvailable(getContext())) {
                    SocialConnect dialogButtons = new SocialConnect(getActivity(), this);
                    dialogButtons.show();
                }
                break;
            }
            case R.id.create_account: {
                Intent intent = new Intent(getActivity(), AccountCreate.class);
                startActivity(intent);
                getActivity().finish();
                break;
            }
            case R.id.noLoginButton: {
                MoninPreferences.setLong(getContext(), MoninPreferences.SHAREDPREFERENCE_KEY.KEY_TOKEN_EXPIRY,0);
                MoninPreferences.setLong(getContext(), MoninPreferences.SHAREDPREFERENCE_KEY.KEY_TOKEN_TIME,0);
                MoninPreferences.setString(getContext(), MoninPreferences.SHAREDPREFERENCE_KEY.KEY_TOKEN,"");
                MoninPreferences.setString(getContext(), MoninPreferences.SHAREDPREFERENCE_KEY.KEY_USER_NAME, "");
                MoninPreferences.setBoolean(getContext(), MoninPreferences.SHAREDPREFERENCE_KEY.KEY_IS_NO_LOGIN, true);
                MoninDataBase db = new MoninDataBase(getContext());
                db.DeleteDataFromTable(MoninContract.MoninEntry.TABLE_NAME);
                db.DeleteDataFromTable(MoninContract.PeopleEntry.TABLE_NAME);
                db.DeleteDataFromTable(MoninContract.GalleryEntry.TABLE_NAME);
                goHome();
                break;
            }
            default: {
                break;
            }
        }
    }


    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        boolean action = false;
        if (actionId == ACTION_SIGNIN) {
            onClick(mLogInButton);
            action = true;
        }
        return action;
    }

    @Override
    public void onLoginSuccess(JSONObject data, String provider) {
        Log.i(TAG, "onLoginSuccess()");
        this.provider = provider;
        Uri endpoint = Uri.parse(getString(R.string.api_external_register));
        boolean progress = progressDialog.isShowing();
        if (!progress){
            progressDialog.show();
        }
        try {
            Map<String,String> headers = new HashMap<>();
            headers.put(getString(R.string.key_content_type), getString(R.string.header_json));
            JSONObject dat = data;
            dat.put(getString(R.string.key_createddatetime),System.currentTimeMillis());
            dat.put(getString(R.string.key_latitude), "99.903248");
            dat.put(getString(R.string.key_longitude), "20.394054321");
            MoninPreferences.setBoolean(getContext(), MoninPreferences.SHAREDPREFERENCE_KEY.KEY_IS_NO_LOGIN, false);
            NetworkConnection.with(getActivity()).withListener(this)
                    .doRequest(Connection.REQUEST.POST, endpoint, null, headers, dat);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onLoginError(int errCode, String errorMessage) {
        Log.i(TAG, "onLoginError() errCode:" + errCode + ",errorMessage:" + errorMessage);
        String errorMessa = JSONUtils.with(getActivity()).getErrorMessage(errorMessage);
        Utils.showSimpleMessage(getContext(), getString(R.string.error), errorMessa);
    }

    @Override
    public void onLoginCancelled() {
        Log.i(TAG, "onLoginCancelled()");
    }

    @Override
    public void onLoginProgress() {
        Log.i(TAG, "onLoginProgress()");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Fragment fragment = null;
        fragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.fragmentButtons);
        if (fragment != null){
            fragment.onActivityResult(requestCode,resultCode,data);
        }
    }

    @Override
    public void onSuccessfullyResponse(String response) {
        Log.i(TAG, "onLoginSuccess()" + response);
        if (progressDialog.isShowing()) progressDialog.dismiss();
        try {
            MoninPreferences.setBoolean(getContext(), MoninPreferences.SHAREDPREFERENCE_KEY.KEY_IS_NO_LOGIN, false);
            JSONUtils.with(getActivity()).updateToken(new JSONObject(response),provider);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        goHome();
    }

    @Override
    public void onErrorResponse(String error, String message, int code) {
        Log.i(TAG, "onLoginError() errCode:" + code + ",errorMessage:" + message);
        if (progressDialog.isShowing()) progressDialog.dismiss();
        if (code == 400){
            showErrorBadCredentialsMessage();
        }else if (code == 105) { //No internet Connection
            Utils.showSimpleMessage(getContext(),getString(R.string.error_title_no_internet),getString(R.string.error_no_internet));
        }
    }

    private void login() {
        if (fieldsValidated()){
            //Call Login API..
            progressDialog.show();
            provider = getString(R.string.value_monin);
            String username= mUserNameEditText.getText().toString();
            String password= mPasswordEditText.getText().toString();
            Map<String, String> params = new HashMap<>();
            params.put(getString(R.string.key_username), username);
            params.put(getString(R.string.key_password), password);
            params.put(getString(R.string.key_grant_type), getString(R.string.key_password));

            Uri endPoint = Uri.parse(getString(R.string.api_login));
            NetworkConnection.with(getActivity()).withListener(this).doRequest(Connection.REQUEST.POST, endPoint, params, null, null);
        }
    }

    private void showErrorBadCredentialsMessage(){
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle(getString(R.string.bad_credentials));
        alert.setMessage(getString(R.string.bad_credentials_message));
        alert.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.create().show();
    }

    public boolean fieldsValidated(){
        boolean validated = true;
        mUserNameEditText.setError(null);
        mPasswordEditText.setError(null);
        if (!(mUserNameEditText.getText().toString().length() > 0)) { //if username field is empty
            mUserNameEditText.setError(getString(R.string.error_field_required));
            mUserNameEditText.requestFocus();
            validated = false;
        }
        if (!(mPasswordEditText.getText().toString().length() > 0)) { //if password field is empty
            mPasswordEditText.setError(getString(R.string.error_field_required));
            mPasswordEditText.requestFocus();
            validated = false;
        }
        if ((mPasswordEditText.getText().toString().length() > 0) && (mPasswordEditText.getText().length() < 6)){
            mPasswordEditText.setError(getString(R.string.error_six_digits));
            mPasswordEditText.requestFocus();
            validated = false;
        }
        return validated;
    }

    private void goHome() {
        Intent intent = new Intent(getActivity(), Home.class);
        startActivity(intent);
        getActivity().finish();
    }



}
