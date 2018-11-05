package com.palarz.mike.jammyjamz.activity;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
import com.palarz.mike.jammyjamz.Utilities;
import com.palarz.mike.jammyjamz.data.SearchService;
import com.palarz.mike.jammyjamz.data.SearchService.SearchType;
import com.palarz.mike.jammyjamz.fragment.PostTypeSelection;
import com.palarz.mike.jammyjamz.model.spotify.Album;
import com.palarz.mike.jammyjamz.model.spotify.Artist;
import com.palarz.mike.jammyjamz.data.PostSearchAdapter;
import com.palarz.mike.jammyjamz.R;
import com.palarz.mike.jammyjamz.networking.SearchClient;
import com.palarz.mike.jammyjamz.model.spotify.Track;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


public class PostSearch extends AppCompatActivity implements PostTypeSelection.PostTypeSelectionListener {

    // Tag used for debugging
    private static final String TAG = PostSearch.class.getSimpleName();

    // An extra for the value of mSearchType
    public static final String EXTRA_SEARCH_TYPE = "post_type";
    public static final String EXTRA_LAUNCH_DIALOG = "launch_dialog";

    // Contains the results of the search request
    @BindView(R.id.post_search_recyclerview) RecyclerView mSeachResults;
    private PostSearchAdapter mAdapter;
    @BindView(R.id.post_search_progress_bar) ProgressBar mProgressBar;
    @BindView(R.id.post_search_toolbar) Toolbar mToolbar;
    // Indicates to the user when they've lost connection to the Firebase DB
    @BindView(R.id.no_internet_indicator) TextView mNoInternet;

    private static SearchType mSearchType;
    private String mQuery;
    // Broadcast receiver stuff
    BroadcastReceiver mReceiver;
    LocalBroadcastManager mBroadcastManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utilities.setTransitions(this);

        setContentView(R.layout.activity_post_search);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mSeachResults.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mSeachResults.setLayoutManager(layoutManager);

        // Setup of data contained within the received intent
        Intent receivedIntent = getIntent();
        if (receivedIntent != null){
            if (receivedIntent.hasExtra(EXTRA_SEARCH_TYPE)){
                mSearchType = (SearchType) receivedIntent.getSerializableExtra(EXTRA_SEARCH_TYPE);
            }
            else {
                mSearchType = SearchType.TRACK;
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
            mSearchType = SearchType.TRACK;
            Log.e(TAG, "Did not receive an intent. Adapter will be set to " +
                    "handle tracks, which can lead to unexpected results.");
        }

        mAdapter = PostSearchAdapter.create(this, mSearchType);
        mSeachResults.setAdapter(mAdapter);

        setupReceiver();

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

        Intent searchIntent = new Intent(this, SearchService.class);
        searchIntent.putExtra(SearchService.EXTRA_QUERY, mQuery);
        searchIntent.putExtra(SearchService.EXTRA_SEARCH_TYPE, mSearchType);
        startService(searchIntent);
    }

    private void setupReceiver(){
        mBroadcastManager = LocalBroadcastManager.getInstance(this);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(SearchService.ACTION_SEND_RESULTS)){
                    Bundle bundle = intent.getExtras();
                    switch (mSearchType){
                        case TRACK:
                            ArrayList<Track> tracks = (ArrayList<Track>) bundle.getSerializable(SearchService.EXTRA_SEARCH_RESULTS);
                            mAdapter.clearData();
                            mAdapter.addData(tracks);
                            mProgressBar.setVisibility(View.GONE);
                            break;

                        case ALBUM:
                            ArrayList<Album> albums = (ArrayList<Album>) bundle.getSerializable(SearchService.EXTRA_SEARCH_RESULTS);
                            mAdapter.clearData();
                            mAdapter.addData(albums);
                            mProgressBar.setVisibility(View.GONE);
                            break;

                        case ARTIST:
                            ArrayList<Artist> artists = (ArrayList<Artist>) bundle.getSerializable(SearchService.EXTRA_SEARCH_RESULTS);
                            mAdapter.clearData();
                            mAdapter.addData(artists);
                            mProgressBar.setVisibility(View.GONE);
                            break;
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter(SearchService.ACTION_SEND_RESULTS);
        mBroadcastManager.registerReceiver(mReceiver, filter);
    }


    // Callback for the positive button within the dialog
    @Override
    public void onPositiveClick(SearchType searchType) {
        mSearchType = searchType;
        mAdapter = PostSearchAdapter.create(this, mSearchType);
        mSeachResults.setAdapter(mAdapter);
    }
}
