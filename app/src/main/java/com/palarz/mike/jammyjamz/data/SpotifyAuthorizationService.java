package com.palarz.mike.jammyjamz.data;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;

import com.palarz.mike.jammyjamz.Utilities;
import com.palarz.mike.jammyjamz.networking.ClientGenerator;
import com.palarz.mike.jammyjamz.networking.SearchClient;
import com.palarz.mike.jammyjamz.networking.TokenResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SpotifyAuthorizationService extends IntentService {

    private static final String TAG = SpotifyAuthorizationService.class.getSimpleName();

    // TODO: I really, really need to figure out a better way to hide these...
    // See here for some better ideas on how to hide these:
    // https://stackoverflow.com/questions/44396499/android-best-way-to-hide-api-clientid-clientsecret

    // Client ID and secret that are used to obtain the access token to the Spotify Web API
    private static final String CLIENT_ID = "e31c0e021bb24dbcb39717172c68dd98";
    private static final String CLIENT_SECRET = "788b8ae21bb644c9a660c613cc912000";

    public static final String ACTION_UPDATE_ACCESS_TOKEN = "com.palarz.mike.jammyjamz.update_access_token";

    private SearchClient mClient;

    public SpotifyAuthorizationService(){
        super("SpotifyAuthorizationService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent.getAction().equals(ACTION_UPDATE_ACCESS_TOKEN)){
            retrieveAccessToken();
        }
    }

    /**
     * Encodes the client ID and client secret in Base64. According to the Spotify API, the client
     * ID and secret need to be added to the Authorization: header in the following format:
     *
     * Basic <base64 encoded client_id:client_secret>
     *
     * Therefore, the client ID and secret are first encoded and then "Base " is prepended.
     *
     * @return The client ID and secret encoded in Base64, with "Base " prepended. This format is
     *          according to the Spotify API.
     */
    private String encodeClientIDAndSecret(){
        final String BASIC = "Basic ";
        String clientIDAndSecret = CLIENT_ID + ":" + CLIENT_SECRET;
        /*
        I use the NO_WRAP flag so that the encoded String is contained within a single line.
        Otherwise, there will be new line characters in the encoded String and we don't want to
        include those.
         */
        byte [] encodedValue = Base64.encode(clientIDAndSecret.getBytes(), Base64.NO_WRAP);
        String encodedString = new String(encodedValue);

        // The final output needs to have both the encoded String as well as 'Basic ' prepended to it
        return BASIC + encodedString;
    }

    /**
     * Retrieves the access token that is used for subsequent search requests. This is done
     * according to the Client Credentials Flow from the OAuth2 framework.
     */
    private void retrieveAccessToken() {

        // First, we obtain an instance of SearchClient through our ClientGenerator class
        mClient = ClientGenerator.createClient(SearchClient.class);

        // We then obtain the client ID and client secret encoded in Base64.
        String encodedString = encodeClientIDAndSecret();

        // Finally, we initiate the HTTP request and hope to get the access token as a response
        Call<TokenResponse> tokenResponseCall = mClient.getAccessToken(encodedString, "client_credentials");
        tokenResponseCall.enqueue(new Callback<TokenResponse>() {
            @Override
            public void onResponse(Call<TokenResponse> call, Response<TokenResponse> response) {
                Log.d(TAG, "onResponse(): response toString(): " + response.toString());
                TokenResponse tokenResponse = null;
                if (response.isSuccessful()) {
                    tokenResponse = response.body();
                    Log.d(TAG, tokenResponse.toString());
                    Utilities.saveTokenResponse(SpotifyAuthorizationService.this, tokenResponse);
                }
            }

            @Override
            public void onFailure(Call<TokenResponse> call, Throwable t) {
                Log.d(TAG, "onFailure: request toString():" + call.request().toString());
            }
        });
    }

}
