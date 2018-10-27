package com.palarz.mike.jammyjamz.data;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.palarz.mike.jammyjamz.model.spotify.Album;
import com.palarz.mike.jammyjamz.model.spotify.Artist;
import com.palarz.mike.jammyjamz.model.spotify.PagingAlbums;
import com.palarz.mike.jammyjamz.model.spotify.PagingArtists;
import com.palarz.mike.jammyjamz.model.spotify.PagingTracks;
import com.palarz.mike.jammyjamz.model.spotify.RootJSONResponse;
import com.palarz.mike.jammyjamz.model.spotify.Track;
import com.palarz.mike.jammyjamz.networking.ClientGenerator;
import com.palarz.mike.jammyjamz.networking.SearchClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchService extends IntentService {

    public SearchService(){
        super("SearchService");
    }

    private static final String TAG = "SearchService";
    public static final String ACTION_SEND_RESULTS = "com.palarz.mike.jammyjamz.send_results";

    public static final String EXTRA_SEARCH_TYPE = "com.palarz.mike.jammyjamz.search_type";
    public static final String EXTRA_QUERY = "com.palarz.mike.jammyjamz.query";
    public static final String EXTRA_ACCESS_TOKEN = "com.palarz.mike.jammyjamz.access_token";
    public static final String EXTRA_SEARCH_RESULTS = "com.palarz.mike.jammyjamz.search_results";

    public enum SearchType {

        TRACK(0),
        ALBUM(1),
        ARTIST(2);

        private int searchType;

        SearchType(int searchType){
            this.searchType = searchType;
        }

        @Override
        public String toString() {
            return "Value of search type: " + searchType;
        }
    }

    private LocalBroadcastManager mBroadcastManager;
    private SearchClient mClient;
    private String mQuery;
    private String mAccessToken;
    private SearchType mSearchType;

    @Override
    public void onCreate() {
        super.onCreate();
        mBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null){
            mQuery = intent.getStringExtra(EXTRA_QUERY);
            mAccessToken = intent.getStringExtra(EXTRA_ACCESS_TOKEN);
//            int type = intent.getIntExtra(EXTRA_SEARCH_TYPE, 0);
//            mSearchType = SearchType.valueOf(String.valueOf(type));
            mSearchType = (SearchType) intent.getSerializableExtra(EXTRA_SEARCH_TYPE);
            switch (mSearchType){
                case TRACK:
                    fetchTracks();
                    break;
                case ALBUM:
                    fetchAlbums();
                    break;
                case ARTIST:
                    fetchArtists();
                    break;
                default:
                    Log.w(TAG, "Unidentified search type, all searches will be for tracks");
                    fetchTracks();
            }

        }
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

    }

    /**
     * Searches for tracks based on the user's query. An HTTP request is sent. If a successful
     * response is received, then a local broadcast is sent with the search results.
     *
     */
    private void fetchTracks() {
        // Prepare mClient and mAccessToken for a search
        prepareForSearch();

        // Finally, we create our call object and initiate the HTTP request.
        Call<RootJSONResponse> call = mClient.searchForTrack("Bearer " + mAccessToken, mQuery);

        call.enqueue(new Callback<RootJSONResponse>() {
            @Override
            public void onResponse(Call<RootJSONResponse> call, Response<RootJSONResponse> response) {
                RootJSONResponse rootJSONResponse = null;

                // If the response was successful...
                if (response.isSuccessful()) {
                    Log.d(TAG, "onResponse: The full URL: " + call.request().url());
                    rootJSONResponse = response.body();

                    // We then extract the tracks and add them to the adapter to be displayed
                    PagingTracks pagingTracks = rootJSONResponse.getPagingTracks();
                    ArrayList<Track> tracks = pagingTracks.getTracks();
                    Intent searchResultsIntent = new Intent(ACTION_SEND_RESULTS);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(EXTRA_SEARCH_RESULTS, tracks);
                    searchResultsIntent.putExtras(bundle);

                    mBroadcastManager.sendBroadcast(searchResultsIntent);
                }
            }

            @Override
            public void onFailure(Call<RootJSONResponse> call, Throwable t) {
                Log.d(TAG, "onFailure: The full URL: " + call.request().url());
            }
        });
    }

    /**
     * Searches for albums based on the user's query. An HTTP request is sent. If a successful
     * response is received, then a local broadcast is sent with the search results.
     *
     */
    private void fetchAlbums() {
        // Prepare mClient and mAccessToken for a search
        prepareForSearch();

        // Finally, we create our call object and initiate the HTTP request.
        Call<RootJSONResponse> call = mClient.searchForAlbum("Bearer " + mAccessToken, mQuery);

        call.enqueue(new Callback<RootJSONResponse>() {
            @Override
            public void onResponse(Call<RootJSONResponse> call, Response<RootJSONResponse> response) {
                RootJSONResponse rootJSONResponse = null;

                // If the response was successful...
                if (response.isSuccessful()) {
                    Log.d(TAG, "onResponse: The full URL: " + call.request().url());
                    rootJSONResponse = response.body();

                    // We then extract the tracks and add them to the adapter to be displayed
                    PagingAlbums pagingAlbums = rootJSONResponse.getPagingAlbums();
                    ArrayList<Album> albums = pagingAlbums.getAlbums();
                    Intent searchResultsIntent = new Intent(ACTION_SEND_RESULTS);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(EXTRA_SEARCH_RESULTS, albums);
                    searchResultsIntent.putExtras(bundle);

                    mBroadcastManager.sendBroadcast(searchResultsIntent);

                }
            }

            @Override
            public void onFailure(Call<RootJSONResponse> call, Throwable t) {
                Log.d(TAG, "onFailure: The full URL: " + call.request().url());
            }
        });
    }

    /**
     * Searches for artists based on the user's query. An HTTP request is sent. If a successful
     * response is received, then a local broadcast is sent with the search results.
     *
     */
    private void fetchArtists() {


        // Prepare mClient and mAccessToken for a search
        prepareForSearch();

        // Finally, we create our call object and initiate the HTTP request.
        Call<RootJSONResponse> call = mClient.searchForArtist("Bearer " + mAccessToken, mQuery);

        call.enqueue(new Callback<RootJSONResponse>() {
            @Override
            public void onResponse(Call<RootJSONResponse> call, Response<RootJSONResponse> response) {
                RootJSONResponse rootJSONResponse = null;

                // If the response was successful...
                if (response.isSuccessful()) {
                    Log.d(TAG, "onResponse: The full URL: " + call.request().url());
                    rootJSONResponse = response.body();

                    // We then extract the tracks and add them to the adapter to be displayed
                    PagingArtists pagingArtists = rootJSONResponse.getPagingArtists();
                    ArrayList<Artist> artists = pagingArtists.getArtists();
                    Intent searchResultsIntent = new Intent(ACTION_SEND_RESULTS);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(EXTRA_SEARCH_RESULTS, artists);
                    searchResultsIntent.putExtras(bundle);

                    mBroadcastManager.sendBroadcast(searchResultsIntent);

                }
            }

            @Override
            public void onFailure(Call<RootJSONResponse> call, Throwable t) {
                Log.d(TAG, "onFailure: The full URL: " + call.request().url());
            }
        });
    }
}
