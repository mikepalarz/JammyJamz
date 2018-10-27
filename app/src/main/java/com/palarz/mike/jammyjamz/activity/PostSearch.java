package com.palarz.mike.jammyjamz.activity;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.palarz.mike.jammyjamz.JammyJamzApplication;
import com.palarz.mike.jammyjamz.data.SearchService;
import com.palarz.mike.jammyjamz.fragment.PostTypeSelection;
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

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
TODO: Use RxJava to perform all three searches asynchronously. That way, you can allow the user
to perform a search without having to specify what kind of content they'd like to add beforehand.
You can then filter out the search results based on the search type by perhaps adding a "Filter"
action to the overflow menu.
 */

public class PostSearch extends AppCompatActivity implements PostTypeSelection.PostTypeSelectionListener {

    // Tag used for debugging
    private static final String TAG = PostSearch.class.getSimpleName();

    // Keys used for SharedPreferences in order to save everything related to the access token
    private static final String PREFERENCES_KEY_TOKEN_RESPONSE_ACCESS_TOKEN = "com.palarz.mike.jammyjamz.access_token";
    private static final String PREFERENCES_KEY_TOKEN_RESPONSE_TOKEN_TYPE = "com.palarz.mike.jammyjamz.token_type";
    private static final String PREFERENCES_KEY_TOKEN_RESPONSE_EXPIRATION = "com.palarz.mike.jammyjamz.expiration";
    private static final String PREFERENCES_KEY_TOKEN_RESPONSE_TIME_SAVED = "com.palarz.mike.jammyjamz.time_saved";

    // An extra for the value of mSearchType
    public static final String EXTRA_SEARCH_TYPE = "post_type";
    public static final String EXTRA_LAUNCH_DIALOG = "launch_dialog";

    public static final String ACTION_SEARCH_RESULTS = "search_results";

    // TODO: I really, really need to figure out a better way to hide these...
    // See here for some better ideas on how to hide these:
    // https://stackoverflow.com/questions/44396499/android-best-way-to-hide-api-clientid-clientsecret

    // Client ID and secret that are used to obtain the access token to the Spotify Web API
    private static final String CLIENT_ID = "e31c0e021bb24dbcb39717172c68dd98";
    private static final String CLIENT_SECRET = "788b8ae21bb644c9a660c613cc912000";

    // Contains the results of the search request
    @BindView(R.id.post_search_recyclerview) RecyclerView mSeachResults;
    private PostSearchAdapter mAdapter;
    // Instance of SearchClient which is used to perform all HTTP requests
    private SearchClient mClient;
    @BindView(R.id.post_search_progress_bar) ProgressBar mProgressBar;
    @BindView(R.id.post_search_toolbar) Toolbar mToolbar;
    // Stores the access token obtained through retrieveAccessToken()
    private String mAccessToken;
    // An integer which determines the type of search that will be performed: tracks (= 0), albums (= 1), or artists (= 2)
    private static int mSearchType;
    // The search query entered into the SearchView
    private String mQuery;

    // Indicates to the user when they've lost connection to the Firebase DB
    @BindView(R.id.no_internet_indicator) TextView mNoInternet;

    // Broadcast receiver stuff
    BroadcastReceiver mReceiver;
    LocalBroadcastManager mBroadcastManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_search);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mSeachResults.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mSeachResults.setLayoutManager(layoutManager);

        // We get the access token. If it's expired, then we will retrieve a new access token.
        mAccessToken = getAccessToken();

        // If the access token is expired, then we will attempt to retrieve a new one
        if (accessTokenExpired()) {
            retrieveAccessToken();
        }

        // Setup of data contained within the received intent
        Intent receivedIntent = getIntent();
        if (receivedIntent != null){
            if (receivedIntent.hasExtra(EXTRA_SEARCH_TYPE)){
                mSearchType = receivedIntent.getIntExtra(EXTRA_SEARCH_TYPE, 0);
            }
            else {
                mSearchType = 0;
                Log.e(TAG, "No search type attached to received intent. Adapter will be set to " +
                        "handle tracks, which can lead to unexpected results.");
            }
            /*
            This extra is used to determine if the PostTypeSelection dialog should be shown or not.
            The dialog is shown only if the user clicks on the button within the widget. Otherwise,
            it won't be shown since the user already chose what type of search they'd like to
            perform within Newsfeed.
             */
            if (receivedIntent.hasExtra(EXTRA_LAUNCH_DIALOG)){
                boolean launchDialog = receivedIntent.getBooleanExtra(EXTRA_LAUNCH_DIALOG, false);
                if (launchDialog){
                    PostTypeSelection dialog = new PostTypeSelection();
                    dialog.show(getSupportFragmentManager(), "dialog");
                }

            } else {
                Log.d(TAG, "Launch dialog not specified within intent");
            }
        } else {
            mSearchType = 0;
            Log.e(TAG, "Did not receive an intent. Adapter will be set to " +
                    "handle tracks, which can lead to unexpected results.");
        }

        mAdapter = PostSearchAdapter.create(this, mSearchType);
        mSeachResults.setAdapter(mAdapter);

        mBroadcastManager = LocalBroadcastManager.getInstance(this);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(SearchService.ACTION_SEND_RESULTS)){
                    Bundle bundle = intent.getExtras();
                    ArrayList<Track> tracks = (ArrayList<Track>) bundle.getSerializable(SearchService.SEARCH_RESULTS);
                    if (mSearchType == 0){
                        mAdapter.clearData();
                        mAdapter.addData(tracks);
                        mProgressBar.setVisibility(View.GONE);
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter(SearchService.ACTION_SEND_RESULTS);
        mBroadcastManager.registerReceiver(mReceiver, filter);

        // Handling the search query
        handleSearchQueryIntent(receivedIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        JammyJamzApplication.getInstance().setupNoInternetIndicator(mNoInternet);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mBroadcastManager.unregisterReceiver(mReceiver);
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
        TODO: Maybe adjust this so that the token is instead saved to SharedPreferences. This way,
        you can get the access token within SearchService by creating a helper method within
        Utilities.
         */

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

    private void prepareAccessToken(){

        // Make sure that mAccessToken is using the most recent token that we have
        mAccessToken = getAccessToken();
        boolean hasExpired = accessTokenExpired();
        if (mAccessToken.isEmpty() || hasExpired){
            retrieveAccessToken();
        }
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
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleSearchQueryIntent(intent);
    }

    private void handleSearchQueryIntent(Intent receivedIntent){
        if (receivedIntent != null && receivedIntent.getAction() != null){
            if (receivedIntent.getAction().equals(Intent.ACTION_SEARCH)){
                mQuery = receivedIntent.getStringExtra(SearchManager.QUERY);
                performSearch();
            }
        }
    }

    private void performSearch(){
        // Depending on the value of mSearchType, a different type of search will be performed
        mProgressBar.setVisibility(ProgressBar.VISIBLE);
        prepareAccessToken();
        switch (mSearchType){
            case 0:
//                fetchTracks(mQuery);
                Intent tracksIntent = new Intent(this, SearchService.class);
                tracksIntent.putExtra(SearchService.EXTRA_ACCESS_TOKEN, mAccessToken);
                tracksIntent.putExtra(SearchService.EXTRA_SEARCH_TYPE, mSearchType);
                tracksIntent.putExtra(SearchService.EXTRA_QUERY, mQuery);
                startService(tracksIntent);
                break;
            case 1:
                fetchAlbums(mQuery);
                break;
            case 2:
                fetchArtists(mQuery);
                break;
            default:
                Log.e(TAG, "Search could not be performed, unknown search type");
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_post_search, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.post_search_menu_action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;

            case R.id.post_search_menu_action_sign_out:
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                startActivity(new Intent(PostSearch.this, Newsfeed.class));
                                finish();
                            }
                        });
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Callback for the positive button within the dialog
    @Override
    public void onPositiveClick(int postType) {
        mSearchType = postType;
        mAdapter = PostSearchAdapter.create(this, mSearchType);
        mSeachResults.setAdapter(mAdapter);
    }
}
