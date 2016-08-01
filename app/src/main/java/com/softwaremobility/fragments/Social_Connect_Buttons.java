package com.softwaremobility.fragments;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.softwaremobility.monin.R;
import com.softwaremobility.network.Connection;
import com.softwaremobility.network.NetworkConnection;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Social_Connect_Buttons extends Fragment implements FacebookCallback<LoginResult>, View.OnClickListener,
        GoogleApiClient.OnConnectionFailedListener, LocationListener{

    private static final String TAG = Social_Connect_Buttons.class.getSimpleName();
    private static final int RC_GOOGLE_LOGIN = 1000;

    private CallbackManager mCallbackManager;
    private GoogleApiClient mGoogleApiClient = null;

    private GoogleSignInOptions mGoogleSignInOptions = null;
    private ISocialLoginListener mISocialLoginListener = null;
    private LoginButton loginButton;
    public static AuthInfo authInfo = null;
    public static SsoHandler ssoHandler = null;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        validateServerClientID();
        initGoogleSignInOptions();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_social_buttons, container, false);

        loginButton = (LoginButton) view.findViewById(R.id.facebook_login_button);
        loginButton.setReadPermissions(Arrays.asList(getString(R.string.facebook_permission_public_profile),
                getString(R.string.facebook_permission_email)));
        mCallbackManager = CallbackManager.Factory.create();
        loginButton.registerCallback(mCallbackManager, this);

        SignInButton googleSignInButton = (SignInButton) view.findViewById(R.id.google_login_button);
        googleSignInButton.setSize(SignInButton.SIZE_STANDARD);
        googleSignInButton.setScopes(mGoogleSignInOptions.getScopeArray());
        googleSignInButton.setOnClickListener(this);

        Button google = (Button) view.findViewById(R.id.google_button);
        google.setOnClickListener(this);

        Button facebook = (Button) view.findViewById(R.id.facebook_button);
        facebook.setOnClickListener(this);

        Button weibo = (Button) view.findViewById(R.id.weibo_button);
        weibo.setOnClickListener(this);

        return view;
    }

    /**
     * This methos is call when facebook account login property
     * @param loginResult
     */
    @Override
    public void onSuccess(LoginResult loginResult) {
        final AccessToken accessToken = loginResult.getAccessToken();
        final String token = accessToken.getToken();
        final long tokenExpiryTime = accessToken.getExpires().getTime();
        Log.d(TAG, "onSuccess called ,token:" + token + ",Expirytime:" + tokenExpiryTime);
        getDataAccount(accessToken);
    }

    private void getDataAccount(final AccessToken token) {
        GraphRequest request = GraphRequest.newMeRequest(token, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {

                try {
                    String email = object.has("email") ? object.getString("email") : "";
                    String name = object.getString("name");
                    String provider = getString(R.string.value_facebook);
                    sendSuccessAnswer(token.getToken(), name, email, provider);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "name,email");
        request.setParameters(parameters);
        request.executeAsync();

    }

    private void sendSuccessAnswer(String token, String name, String email, String provider){
        try {
            JSONObject object = new JSONObject();
            object.put(getString(R.string.key_token),token);
            object.put(getString(R.string.key_name), name);
            object.put(getString(R.string.key_email), email);
            object.put(getString(R.string.key_provider), provider);
            mISocialLoginListener.onLoginSuccess(object,provider);
        }catch (JSONException e){}
    }

    @Override
    public void onCancel() {
        mISocialLoginListener.onLoginCancelled();
    }

    @Override
    public void onError(FacebookException error) {
        mISocialLoginListener.onLoginError(105, error.getMessage());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        /*
        * Google SOO Login Success
        * */
        Log.d(TAG, "onActivityResult:requestCode:" + requestCode + ",resultCode" + resultCode + ",data:" + data);
        if (requestCode == RC_GOOGLE_LOGIN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            Log.d(TAG, "onActivityResult:GET_TOKEN:success:" + result);

            if (result.isSuccess()) {
                //mISocialLoginListener.onLoginSuccess();
                GoogleSignInAccount googleSignInAccount = result.getSignInAccount();
                String idToken = googleSignInAccount.getIdToken();
                Log.d(TAG, "idToken: " + idToken);
                String name = googleSignInAccount.getDisplayName();
                String email = googleSignInAccount.getEmail();
                sendSuccessAnswer(idToken, name, email, getString(R.string.value_google));
            }
        }else {
            if (ssoHandler != null) {
                ssoHandler.authorizeCallBack(requestCode, resultCode, data);
            }
        }

    }


    private void validateServerClientID() {
        String serverClientId = getString(R.string.server_client_id);
        String suffix = getString(R.string.server_client_id_suffix);
        if (!serverClientId.trim().endsWith(suffix)) {
            String message = "Invalid server client ID in strings.xml, must end with " + suffix;
            Log.w(TAG, message);
        }
    }


    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.google_button || v.getId() == R.id.google_login_button){
            mISocialLoginListener.onLoginProgress();
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            getActivity().startActivityForResult(signInIntent, RC_GOOGLE_LOGIN);
        }else if (v.getId() == R.id.facebook_button){
            LoginManager.getInstance().logOut();
            loginButton.performClick();
        }else if (v.getId() == R.id.weibo_button){
            ConnectWeibo();
        }
    }

    private void ConnectWeibo() {
        authInfo = new AuthInfo(getContext(),getString(R.string.weibo_app_id),getString(R.string.weibo_redirect_url),"");
        ssoHandler = new SsoHandler((Activity) getContext(),authInfo);
        ssoHandler.authorize(new WeiboAuthListener() {
            @Override
            public void onComplete(Bundle bundle) {
                Oauth2AccessToken token = Oauth2AccessToken.parseAccessToken(bundle);
                final String tokenStr = token.getToken();
                Log.d(TAG,"OnComplete, token: " + tokenStr);
                Uri uri = Uri.parse("show.json");
                Map<String,String> params = new HashMap<>();
                params.put("uid",token.getUid());
                params.put("access_token",token.getToken());
                Map<String,String> headers = new HashMap<>();
                headers.put(getString(R.string.key_content_type), getString(R.string.header_json));
                NetworkConnection.productionPath("https://api.weibo.com/2/users");
                NetworkConnection.testPath("https://api.weibo.com/2/users");
                NetworkConnection.with(getActivity()).withListener(new NetworkConnection.ResponseListener() {
                    @Override
                    public void onSuccessfullyResponse(String response) {
                        String res = response;
                        NetworkConnection.testPath(getString(R.string.base_path_requests));
                        NetworkConnection.productionPath(getString(R.string.base_path_production));
                        try {
                            JSONObject object = new JSONObject(response);
                            String name = object.getString("name");
                            String photo = object.getString("profile_image_url");
                            sendSuccessAnswer(tokenStr,name,"","Weibo");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onErrorResponse(String error, String message, int code) {
                        Log.e("Error",message + ", " + error);

                    }
                }).doRequest(Connection.REQUEST.GET,uri,params,null,null);
            }

            @Override
            public void onWeiboException(WeiboException e) {
                Log.e(TAG,"Exception" + e.getMessage());
            }

            @Override
            public void onCancel() {
                Log.e(TAG,"OnCancel");
            }
        });
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed" + connectionResult.getErrorMessage());
        mISocialLoginListener.onLoginError(105, getString(R.string.connection_failed));
    }

    private void initGoogleSignInOptions() {
        // Request only the user's ID token, which can be used to identify the
        // user securely to your backend. This will contain the user's basic
        // profile (name, profile picture URL, etc) so you should not need to
        // make an additional call to personalize your application.
        try {
            if (mGoogleSignInOptions == null) {
                mGoogleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.server_auth_code))
                        .requestEmail()
                        .requestProfile()
                        .build();
            }

            // Build GoogleAPIClient with the Google Sign-In API and the above options.
            if (mGoogleApiClient == null) {
                mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                        .enableAutoManage(getActivity(), this)
                        .addApi(Auth.GOOGLE_SIGN_IN_API, mGoogleSignInOptions)
                        .build();
            }
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    public void setSocialLoginListener(ISocialLoginListener listener){
        mISocialLoginListener = listener;
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    public interface ISocialLoginListener {
        public void onLoginSuccess(JSONObject data, String provider);

        public void onLoginError(final int errCode, final String errorMessage);

        public void onLoginCancelled();

        public void onLoginProgress();
    }
}
