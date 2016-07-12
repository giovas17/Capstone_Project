package com.softwaremobility.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.softwaremobility.json.JSONUtils;
import com.softwaremobility.monin.R;
import com.softwaremobility.network.Connection;
import com.softwaremobility.network.NetworkConnection;
import com.softwaremobility.objects.CheckBoxImageView;
import com.softwaremobility.utilities.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class AccountCreate extends Fragment implements View.OnClickListener, NetworkConnection.ResponseListener {

    private static final String LOG_TAG = AccountCreate.class.getSimpleName();

    private EditText mPasswordEditText;
    private EditText mCompleteNameEditText;
    private EditText mEmailEditText;
    private EditText mUserNameEditText;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.create_account:
                try {
                    long current = System.currentTimeMillis();
                    JSONObject params = new JSONObject();
                    params.put(getString(R.string.key_email), mEmailEditText.getText().toString());
                    params.put(getString(R.string.key_name), mCompleteNameEditText.getText().toString());
                    params.put(getString(R.string.key_username), mUserNameEditText.getText().toString());
                    params.put(getString(R.string.key_password), mPasswordEditText.getText().toString());
                    params.put(getString(R.string.key_latitude), "99.324234234");
                    params.put(getString(R.string.key_longitude), "24.23432434");
                    params.put(getString(R.string.key_createddatetime), current);

                    Map<String,String> headers = new HashMap<>();
                    headers.put(getString(R.string.key_content_type), getString(R.string.header_json));
                    final Uri endPoint = Uri.parse(getString(R.string.api_account_register));
                    NetworkConnection.with(getActivity()).withListener(this).doRequest(Connection.REQUEST.POST,endPoint,null,headers,params);
                }catch (JSONException e){
                    e.printStackTrace();
                }
                break;
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account_create, container, false);
        mCompleteNameEditText = (EditText) view.findViewById(R.id.complete_name);
        mEmailEditText = (EditText) view.findViewById(R.id.email);
        mUserNameEditText = (EditText) view.findViewById(R.id.username);
        mPasswordEditText = (EditText) view.findViewById(R.id.account_create_password);

        CheckBoxImageView checkBoxImageView = (CheckBoxImageView) view.findViewById(R.id.checkBox);
        mPasswordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
        checkBoxImageView.setOnCheckedChangeListener(new CheckBoxImageView.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View buttonView, boolean isChecked) {
                if (isChecked) {
                    mPasswordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    mPasswordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

        Button createAccountButton = (Button) view.findViewById(R.id.create_account);
        createAccountButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onSuccessfullyResponse(String response) {
        Log.i(LOG_TAG, "onSuccessfullyResponse:" + response);
        Map<String,String> params = new HashMap<>();
        params.put(getString(R.string.key_username),mUserNameEditText.getText().toString());
        params.put(getString(R.string.key_password), mPasswordEditText.getText().toString());
        params.put(getString(R.string.key_grant_type), getString(R.string.key_password));
        Map<String,String> headers = new HashMap<>();
        headers.put(getString(R.string.key_content_type), getString(R.string.header_content_urlencoded));
        NetworkConnection.with(getActivity()).withListener(new NetworkConnection.ResponseListener() {
            @Override
            public void onSuccessfullyResponse(String response) {
                Log.i(LOG_TAG, "onSuccessfullyResponse:" + response);
                try {
                    JSONUtils.with(getActivity()).updateToken(new JSONObject(response),getString(R.string.value_monin));
                    Intent intent = new Intent(getActivity(), Home.class);
                    startActivity(intent);
                    getActivity().finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onErrorResponse(String error, String message, int code) {
                Log.i(LOG_TAG, "onErrorResponse() errCode:" + code + ",errorMessage:" + message);
                String errorMessage = JSONUtils.with(getActivity()).getErrorMessage(message);
                Utils.showSimpleMessage(getContext(), getString(R.string.error), errorMessage);
            }
        }).doRequest(Connection.REQUEST.POST, Uri.parse(getString(R.string.api_login)), params, headers, null);
    }

    @Override
    public void onErrorResponse(String error, String message, int code) {
        Log.i(LOG_TAG, "onErrorResponse() errCode:" + code + ",errorMessage:" + message);
        String errorMessage = JSONUtils.with(getActivity()).getErrorMessage(message);
        Utils.showSimpleMessage(getContext(),getString(R.string.error),errorMessage);
    }

}
