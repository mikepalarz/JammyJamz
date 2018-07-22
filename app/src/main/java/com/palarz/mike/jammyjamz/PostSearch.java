package com.palarz.mike.jammyjamz;

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
import android.widget.ListView;
import android.support.v7.widget.SearchView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * The main activity of the app which allows the end user to search for a song. Once the search
 * request is made, the results are shown within a ListView.
 */

public class PostSearch extends AppCompatActivity {

    private static final String TAG = PostSearch.class.getSimpleName();

    public static final String EXTRA_POST_TYPE = "post_type";

    // Client ID and secret that are used to obtain the access token to the Spotify Web API
    // TODO: I really, really need to figure out a better way to hide these...
    private static final String CLIENT_ID = "e31c0e021bb24dbcb39717172c68dd98";
    private static final String CLIENT_SECRET = "788b8ae21bb644c9a660c613cc912000";

    private RecyclerView mSeachResults; // Contains the results of the search request
    private PostSearchAdapter mAdapter;
    private SongClient mClient; // Instance of SongClient which is used to perform all HTTP requests
    private ProgressBar mProgressBar;
    private String mAccessToken;    // Stores the access token obtained through retrieveAccessToken()

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_search);

        mSeachResults = (RecyclerView) findViewById(R.id.post_search_recyclerview);
        mSeachResults.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mSeachResults.setLayoutManager(layoutManager);

        mAdapter = PostSearchAdapter.create(this, 0);
        mSeachResults.setAdapter(mAdapter);

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

        // Upon start-up, we obtain the access token so that we're ready for any search requests
        // that the user may have
        retrieveAccessToken();
    }

    /**
     * Retrieves the access token that is used for subsequent search requests
     */
    private void retrieveAccessToken() {

        // First, we obtain an instance of SongClient through our ClientGenerator class
        mClient = ClientGenerator.createClient(SongClient.class);

        // We then obtain the client ID and client secret encoded in Base64.
        String encodedString = encodeClientIDAndSecret();

        // Finally, we initiate the HTTP request and hope to get the access token as a response
        Call<TokenResponse> tokenResponseCall = mClient.getAccessToken(encodedString, "client_credentials");
        tokenResponseCall.enqueue(new Callback<TokenResponse>() {
            @Override
            public void onResponse(Call<TokenResponse> call, Response<TokenResponse> response) {
                Log.d(TAG, "on Response: response toString(): " + response.toString());
                TokenResponse tokenResponse = null;
                if (response.isSuccessful()) {
                    tokenResponse = response.body();
                    Log.d(TAG, "Access token value: " + tokenResponse.getAccessToken());
                    mAccessToken = tokenResponse.getAccessToken();
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
        String basic = "Basic ";
        String clientIDAndSecret = CLIENT_ID + ":" + CLIENT_SECRET;
        /*
        I use the NO_WRAP flag so that the encoded String is contained within a single line.
        Otherwise, there will be new line characters in the encoded String and we don't want to
        include those.
         */
        byte [] encodedValue = Base64.encode(clientIDAndSecret.getBytes(), Base64.NO_WRAP);
        String encodedString = new String(encodedValue);

        // The final output needs to have both the encoded String as well as 'Basic ' prepended to it
        return basic + encodedString;
    }

    /**
     * Actually performs the search request. If the search was successful, then the ListView's
     * adapter is cleared so that previous search results are no longer shown. Then, the
     * new search results are presented to the user.
     *
     * @param query The query that the user entered into the SearchView.
     */
    private void fetchSongs(String query) {
        // We show the ProgressBar to the user so that they're aware that the HTTP request is being made
        mProgressBar.setVisibility(ProgressBar.VISIBLE);

        // We also need to change the base URL of the SongClient since it was previously set to
        // the one that is used for the access token
        ClientGenerator.changeBaseURL(SongClient.BASE_URL_SEARCH);

        // Finally, we obtain a new instance of the SongClient with the appropriate base URL
        mClient = ClientGenerator.createClient(SongClient.class);

        // If we didn't obtain an access token, then we simply stop performing the search
        if (TextUtils.isEmpty(mAccessToken)) {
            // We also want to be sure that we no longer show the ProgressBar
            mProgressBar.setVisibility(ProgressBar.GONE);
            return;
        }

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_post_search, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        // We set an OnQueryTextListener to the SearchView so that fetchSongs() is fired each time
        // a search request is made
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Fetch the remote data
                fetchSongs(query);
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
}
