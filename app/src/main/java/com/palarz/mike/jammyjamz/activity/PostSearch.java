package com.palarz.mike.jammyjamz.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.SearchView;
import android.widget.ProgressBar;

import com.palarz.mike.jammyjamz.networking.ClientGenerator;
import com.palarz.mike.jammyjamz.data.PostSearchAdapter;
import com.palarz.mike.jammyjamz.R;
import com.palarz.mike.jammyjamz.networking.SearchClient;
import com.palarz.mike.jammyjamz.networking.TokenResponse;
import com.palarz.mike.jammyjamz.model.spotify.Album;
import com.palarz.mike.jammyjamz.model.spotify.Artist;
import com.palarz.mike.jammyjamz.model.spotify.PagingAlbums;
import com.palarz.mike.jammyjamz.model.spotify.PagingArtists;
import com.palarz.mike.jammyjamz.model.spotify.PagingTracks;
import com.palarz.mike.jammyjamz.model.spotify.RootJSONResponse;
import com.palarz.mike.jammyjamz.model.spotify.Track;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostSearch extends AppCompatActivity {

    // Tag used for debugging
    private static final String TAG = PostSearch.class.getSimpleName();

    // Keys used for SharedPreferences in order to save everything related to the access token
    private static final String PREFERENCES_KEY_TOKEN_RESPONSE_ACCESS_TOKEN = "com.palarz.mike.jammyjamz.access_token";
    private static final String PREFERENCES_KEY_TOKEN_RESPONSE_TOKEN_TYPE = "com.palarz.mike.jammyjamz.token_type";
    private static final String PREFERENCES_KEY_TOKEN_RESPONSE_EXPIRATION = "com.palarz.mike.jammyjamz.expiration";
    private static final String PREFERENCES_KEY_TOKEN_RESPONSE_TIME_SAVED = "com.palarz.mike.jammyjamz.time_saved";

    // An extra for the value of mSearchType
    public static final String EXTRA_SEARCH_TYPE = "post_type";

    // TODO: I really, really need to figure out a better way to hide these...
    // See here for some better ideas on how to hide these:
    // https://stackoverflow.com/questions/44396499/android-best-way-to-hide-api-clientid-clientsecret

    // Client ID and secret that are used to obtain the access token to the Spotify Web API
    private static final String CLIENT_ID = "e31c0e021bb24dbcb39717172c68dd98";
    private static final String CLIENT_SECRET = "788b8ae21bb644c9a660c613cc912000";

    // Contains the results of the search request
    private RecyclerView mSeachResults;
    private PostSearchAdapter mAdapter;
    // Instance of SearchClient which is used to perform all HTTP requests
    private SearchClient mClient;
    private ProgressBar mProgressBar;
    // Stores the access token obtained through retrieveAccessToken()
    private String mAccessToken;
    // An integer which determines the type of search that will be performed: tracks (= 0), albums (= 1), or artists (= 2)
    private int mSearchType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_search);

        /*
        We attempt to extract mSearchType from the received intent
         */
        Intent receivedIntent = getIntent();
        if (receivedIntent != null && receivedIntent.hasExtra(EXTRA_SEARCH_TYPE)){
            mSearchType = receivedIntent.getIntExtra(EXTRA_SEARCH_TYPE, 0);
        } else {
            mSearchType = 0;
            Log.e(TAG, "No extra attached to received intent, all search requests will be for tracks");
        }


        mSeachResults = (RecyclerView) findViewById(R.id.post_search_recyclerview);
        mSeachResults.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mSeachResults.setLayoutManager(layoutManager);

        mAdapter = PostSearchAdapter.create(this, mSearchType);
        mSeachResults.setAdapter(mAdapter);

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

        // We get the access token. If it's expired, then we will retrieve a new access token.
        mAccessToken = getAccessToken();

        // If the access token is expired, then we will attempt to retrieve a new one
        if (accessTokenExpired()) {
            retrieveAccessToken();
        }
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
                    mAccessToken = tokenResponse.getAccessToken();
                    saveTokenResponse(tokenResponse);
                }
            }

            @Override
            public void onFailure(Call<TokenResponse> call, Throwable t) {
                Log.d(TAG, "onFailure: request toString():" + call.request().toString());
                mAccessToken = "";
            }
        });
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
     * Saves all useful information from a <code>TokenResponse</code> into SharedPreferences.
     * Parts of the <code>TokenResponse</code> that are saved include the access token and the
     * expiration of the access token.
     *
     * @param tokenResponse A <code>TokenResponse</code> that is ideally retrieved on a successful
     *                      HTTP request for the access token.
     */
    private void saveTokenResponse(TokenResponse tokenResponse){
        /*
        We're using getPreferences() here instead of getSharedPreferences() since getPreferences()
        provides us with the default SharedPreferences for the current activity. If we used
        getSharedPreferences(), then other activities could potentially access the same
        SharedPreferences file. We only want PostSearch to be able to save the access token
        for now, so getPreferences() works fine for our purposes.
        */

        SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
        editor.putString(PREFERENCES_KEY_TOKEN_RESPONSE_ACCESS_TOKEN, tokenResponse.getAccessToken());
        editor.putString(PREFERENCES_KEY_TOKEN_RESPONSE_TOKEN_TYPE, tokenResponse.getTokenType());
        editor.putLong(PREFERENCES_KEY_TOKEN_RESPONSE_EXPIRATION, tokenResponse.getExpiration());
        editor.putLong(PREFERENCES_KEY_TOKEN_RESPONSE_TIME_SAVED, System.currentTimeMillis()/1000);
        editor.commit();

    }

    /**
     * Determines if the access token is expired or not.
     *
     * @return <code>true</code> if <code>mAccessToken</code> is expired, <code>false</code>
     * otherwise.
     */
    private boolean accessTokenExpired() {
        // If mAccessToken hasn't yet been initialized, that means that we need to try to retrieve
        // an access token. In this case, we will return true;
        if (mAccessToken == null) {
            return true;
        }

        // Otherwise, we will read from SharedPreferences to determine if the access token is expired or not
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        long timeSaved = preferences.getLong(PREFERENCES_KEY_TOKEN_RESPONSE_TIME_SAVED, 0L);
        long expiration = preferences.getLong(PREFERENCES_KEY_TOKEN_RESPONSE_EXPIRATION, 0L);

        // Determining how much time has passed since we saved the access token
        long now = System.currentTimeMillis()/1000;
        long timePassed = Math.abs(now - timeSaved);

        if (timePassed >= expiration) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * A helper method to get the value of the access token from SharedPreferences.
     *
     * @return The current value of the access token that was saved to SharedPreferences.
     */
    private String getAccessToken() {
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        return preferences.getString(PREFERENCES_KEY_TOKEN_RESPONSE_ACCESS_TOKEN, "");
    }

    /**
     * Searches for tracks based on the user's query. An HTTP request is sent. If a successful
     * response is received, then the current contents of <code>mSearchResults</code> is cleared
     * and the new search results are added to the adapter.
     *
     * @param query The query that the user entered into the SearchView.
     */
    private void fetchTracks(String query) {
        // We show the ProgressBar to the user so that they're aware that the HTTP request is being made
        mProgressBar.setVisibility(ProgressBar.VISIBLE);

        // Prepare mClient and mAccessToken for a search
        prepareForSearch();

        // Finally, we create our call object and initiate the HTTP request.
        Call<RootJSONResponse> call = mClient.searchForTrack("Bearer " + mAccessToken, query);

        call.enqueue(new Callback<RootJSONResponse>() {
            @Override
            public void onResponse(Call<RootJSONResponse> call, Response<RootJSONResponse> response) {
                RootJSONResponse rootJSONResponse = null;

                // If the response was successful...
                if (response.isSuccessful()) {
                    Log.d(TAG, "onResponse: The full URL: " + call.request().url());

                    // ...we clear the adapter and populate the RootJSONResponse object
                    mAdapter.clearData();
                    rootJSONResponse = response.body();

                    // We then extract the tracks and add them to the adapter to be displayed
                    PagingTracks pagingTracks = rootJSONResponse.getPagingTracks();
                    List<Track> tracks = pagingTracks.getTracks();
                    mAdapter.addData(tracks);

                    mProgressBar.setVisibility(ProgressBar.GONE);
                }
            }

            @Override
            public void onFailure(Call<RootJSONResponse> call, Throwable t) {
                Log.d(TAG, "onFailure: The full URL: " + call.request().url());
                mProgressBar.setVisibility(ProgressBar.GONE);
            }
        });
    }

    /**
     * Searches for albums based on the user's query. An HTTP request is sent. If a successful
     * response is received, then the current contents of <code>mSearchResults</code> is cleared
     * and the new search results are added to the adapter.
     *
     * @param query The query that the user entered into the SearchView.
     */
    private void fetchAlbums(String query) {
        // We show the ProgressBar to the user so that they're aware that the HTTP request is being made
        mProgressBar.setVisibility(ProgressBar.VISIBLE);

        // Prepare mClient and mAccessToken for a search
        prepareForSearch();

        // Finally, we create our call object and initiate the HTTP request.
        Call<RootJSONResponse> call = mClient.searchForAlbum("Bearer " + mAccessToken, query);

        call.enqueue(new Callback<RootJSONResponse>() {
            @Override
            public void onResponse(Call<RootJSONResponse> call, Response<RootJSONResponse> response) {
                RootJSONResponse rootJSONResponse = null;

                // If the response was successful...
                if (response.isSuccessful()) {
                    Log.d(TAG, "onResponse: The full URL: " + call.request().url());

                    // ...we clear the adapter and populate the RootJSONResponse object
                    mAdapter.clearData();
                    rootJSONResponse = response.body();

                    // We then extract the tracks and add them to the adapter to be displayed
                    PagingAlbums pagingAlbums = rootJSONResponse.getPagingAlbums();
                    List<Album> albums = pagingAlbums.getAlbums();
                    mAdapter.addData(albums);

                    mProgressBar.setVisibility(ProgressBar.GONE);
                }
            }

            @Override
            public void onFailure(Call<RootJSONResponse> call, Throwable t) {
                Log.d(TAG, "onFailure: The full URL: " + call.request().url());
                mProgressBar.setVisibility(ProgressBar.GONE);
            }
        });
    }

    /**
     * Searches for artists based on the user's query. An HTTP request is sent. If a successful
     * response is received, then the current contents of <code>mSearchResults</code> is cleared
     * and the new search results are added to the adapter.
     *
     * @param query The query that the user entered into the SearchView.
     */
    private void fetchArtists(String query) {
        // We show the ProgressBar to the user so that they're aware that the HTTP request is being made
        mProgressBar.setVisibility(ProgressBar.VISIBLE);

        // Prepare mClient and mAccessToken for a search
        prepareForSearch();

        // Finally, we create our call object and initiate the HTTP request.
        Call<RootJSONResponse> call = mClient.searchForArtist("Bearer " + mAccessToken, query);

        call.enqueue(new Callback<RootJSONResponse>() {
            @Override
            public void onResponse(Call<RootJSONResponse> call, Response<RootJSONResponse> response) {
                RootJSONResponse rootJSONResponse = null;

                // If the response was successful...
                if (response.isSuccessful()) {
                    Log.d(TAG, "onResponse: The full URL: " + call.request().url());

                    // ...we clear the adapter and populate the RootJSONResponse object
                    mAdapter.clearData();
                    rootJSONResponse = response.body();

                    // We then extract the tracks and add them to the adapter to be displayed
                    PagingArtists pagingArtists = rootJSONResponse.getPagingArtists();
                    List<Artist> artists = pagingArtists.getArtists();
                    mAdapter.addData(artists);

                    mProgressBar.setVisibility(ProgressBar.GONE);
                }
            }

            @Override
            public void onFailure(Call<RootJSONResponse> call, Throwable t) {
                Log.d(TAG, "onFailure: The full URL: " + call.request().url());
                mProgressBar.setVisibility(ProgressBar.GONE);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_post_search, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        // TODO: Update all of this to the latest SearchView best practices
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        // We set an OnQueryTextListener to the SearchView
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Depending on the value of mSearchType, a different type of search will be performed
                switch (mSearchType){
                    case 0:
                        fetchTracks(query);
                        break;
                    case 1:
                        fetchAlbums(query);
                        break;
                    case 2:
                        fetchArtists(query);
                        break;
                    default:
                        break;
                }
                // Reset the SearchView
                searchView.clearFocus();
                searchView.setQuery("", false);
                searchView.setIconified(true);
                searchItem.collapseActionView();
                // We'll also set the title of the activity to the current search query
                PostSearch.this.setTitle(query);

                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        return true;
    }

    /**
     * Prepares several member variables for a search to be performed. In particular, it
     * changes the base URL of our search client to the URL that should be used for searches. In
     * addition, it checks if the access token is expired. If it is, then a new access token will
     * be retrieved.
     */
    private void prepareForSearch(){

        // Set the base URL to the search URL
        ClientGenerator.changeBaseURL(SearchClient.BASE_URL_SEARCH);

        // We obtain a new instance of the SearchClient with the appropriate base URL
        mClient = ClientGenerator.createClient(SearchClient.class);

        // Make sure that mAccessToken is using the most recent token that we have
        mAccessToken = getAccessToken();
        boolean hasExpired = accessTokenExpired();
        if (mAccessToken.isEmpty() || hasExpired){
            retrieveAccessToken();
        }
    }

}
