package com.palarz.mike.jammyjamz.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.palarz.mike.jammyjamz.JammyJamzApplication;
import com.palarz.mike.jammyjamz.Utilities;
import com.palarz.mike.jammyjamz.data.NewsfeedAdapter;
import com.palarz.mike.jammyjamz.fragment.PostTypeSelection;
import com.palarz.mike.jammyjamz.R;
import com.palarz.mike.jammyjamz.model.Post;

import junit.framework.Assert;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * An <code>Activity</code> that displays a newsfeed interface to the end user. It uses the
 * Firebase Realtime Database to display all of the current posts from all users. It also provides
 * user authentication via <code>FirebaseUI</code>.
 *
 * @author Mike Palarz
 */
public class Newsfeed extends AppCompatActivity implements PostTypeSelection.PostTypeSelectionListener {

    // Request code used within onActivityResult(); this is used to identify the FirebaseUI sign-in
    // activity within onActivityResult(), which is called after the user signs in
    private static final int RC_SIGN_IN = 1;

    // String which is used to identify when this activity receives an instance of Post within an Intent
    public static final String EXTRA_NEW_POST = "com.palarz.mike.jammyjamz.activity.Newsfeed.new_post";

    // Tag used for log statements
    private static final String TAG = Newsfeed.class.getSimpleName();

    // Our RecyclerView and adapter
    @BindView(R.id.newsfeed_recyclerview) RecyclerView mRecyclerView;
    private NewsfeedAdapter mAdapter;

    @BindView(R.id.newsfeed_toolbar) Toolbar mToolbar;
    @BindView(R.id.fab) FloatingActionButton mFab;

    // A String used to store the current username
    private String mUsername;
    // Used to indicate to the user that they have no Internet connection
    @BindView(R.id.no_internet_indicator) TextView mNoInternet;

    /*
                                    Firebase member variables
     */
    // A reference to our Realtime Database
    private FirebaseDatabase mFirebaseDatabase;
    // A reference to the "posts" node of the Realtime Database
    private DatabaseReference mPostsReference;
    // A reference to the connectivity between the Android device and the Firebase Realtime DB server
    private DatabaseReference mConnectionState;
    // Listens to any posts which have been added to the Realtime Database
    private ChildEventListener mPostsListener;
    // Our FirebaseAuth instance
    private FirebaseAuth mFirebaseAuth;
    // Listens to any changes in authentication state; i.e., this is a listener which detects when
    // the user is either signed-in or signed-out
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private static final String BUNDLE_KEY_STATE = "recyclerview_state";
    private Parcelable mState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newsfeed);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        Assert.assertNotNull("mNoInternet is null", mNoInternet);

        if (savedInstanceState != null){
            mState = savedInstanceState.getParcelable(BUNDLE_KEY_STATE);
        }

        // Initialize the FAB and set an OnClickListener
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                When the user clicks on the FAB, they are shown a dialog to choose what type of
                post they'd like to create.
                 */
                PostTypeSelection dialog = new PostTypeSelection();
                dialog.show(getSupportFragmentManager(), "dialog");
            }
        });

        // We'll set mUsername to the current username, or anonymous if the user isn't yet signed in
        mUsername = Utilities.getUsername(this);

        // Initial setup of the RecyclerView
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        /* Very nifty way to show reverse order in a RecyclerView */
        // Reverses the order in which the ViewHolderss are displayed, similar to layout changes for RTL
        layoutManager.setReverseLayout(true);
        // Sets the RecyclerView to snap to the end of the data contents of the adapter
        layoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(layoutManager);

        // Initial setup of the adapter
        mAdapter = new NewsfeedAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        // Initializing the Firebase Database, posts reference, and authentication
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        mFirebaseAuth = FirebaseAuth.getInstance();
        mPostsReference = mFirebaseDatabase.getReference().child("posts");

        // The auth state listener is also initialized to handle user sign-in/sign-out
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            // The FirebaseAuth parameter is guaranteed to indicate whether the user is authenticated or not
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();

                // We determine if the user is authenticated or not by checking if currentUser is null

                // The user is signed in
                if (currentUser != null) {
                    /*
                    Do initial setup for when the user signs-in, such as saving the username and
                    initializing the ChildEventsListener for our Realtime Database
                     */
                    onSignedInInitialize(currentUser.getDisplayName());

                }
                // The user is signed out
                else {
                    /*
                    We perform necessary clean-up when the user signs out, such as removing all
                    data within the adapter and detaching the ChildEventsListener from the
                    Realtime Database.
                     */
                    onSignedOutCleanUp();
                    // Once the clean-up is finished, when then prompt the user to sign back in.
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.GoogleBuilder().build(),
                                            new AuthUI.IdpConfig.EmailBuilder().build()))
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };

        /*
        One last thing to do here: this activity can be launched from another activity if a new
        Post is created. For now, WritePost is the only one to do that. If a new Post comes in,
        then we push it up to the Realtime Database. It will also appear within the RecyclerView
        since our ChildEventsListener has already been initialized.
         */
        Intent receivedIntent = getIntent();
        if (receivedIntent != null && receivedIntent.hasExtra(EXTRA_NEW_POST)){
            Post newPost = receivedIntent.getParcelableExtra(EXTRA_NEW_POST);
            mPostsReference.push().setValue(newPost);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // If the activity that we're returning from is the FirebaseUI login-in...
        if (requestCode == RC_SIGN_IN){
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                Log.i(TAG, "User has signed in successfully.");
            } else if (resultCode == RESULT_CANCELED) {
                // If the user canceled the sign-in process (either while in the midst of it or by
                // pressing the back button once the sign-in UI was shown), then we'll bring them
                // back to the Newsfeed activity
                Log.i(TAG, "Sign-in process was canceled.");
                finish();
            } else {
                /*
                Sign in failed. If response is null the user canceled the sign-in flow using the
                back button, which we'll handle by exiting the newsfeed.
                 */
                if (response == null) {
                    finish();
                } else {
                    /*
                    Otherwise, there was an error during the sign-in process. For now, we'll log
                    the error code, but we should really be handling each error code individually.
                     */
                    Log.e(TAG, "Error occurred during sign-in. Error code: " + response.getError().getErrorCode());
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // We attach our auth state listener here since we want to do so only once the app is
        // visible, which is what occurs once onResume() is called
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);

        JammyJamzApplication.getInstance().setupNoInternetIndicator(mNoInternet);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // We first check if mAuthStateListener is null, meaning that is hasn't been previously removed
        if (mAuthStateListener != null) {
            /*
            We then remove the listener in onPause(). We do that here since onPause() is called
            when the app is no longer visible. We don't want to do this each time the user signs out
            and only within onPause() because the app is still potentially visible when the user
            signs out.
             */
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }

        // We then detach the child events listener and clear any data that may be in our adapter
        detachPostsReadListener();
        mAdapter.clearData();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(BUNDLE_KEY_STATE, mRecyclerView.getLayoutManager().onSaveInstanceState());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Nothing overly fancy going on here
        getMenuInflater().inflate(R.menu.menu_newsfeed, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.write_post_menu_action_sign_out:
                // If the user clicks on the sign-out button within the menu, then we will start
                // the sign-out process
                AuthUI.getInstance().signOut(this);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Attaches our <code>ChildEventListener</code> to our <code>DatabaseReference</code> when necessary.
     */
    private void attachedPostsReadListener() {
        /*
        We first check if mPostsListener is null in order to determine if it was previously
        attached to a database reference. If it hasn't yet been attached to a DB reference (that
        is, it is null), then we will instantiate mPostsListener.
         */
        if (mPostsListener == null) {
            mPostsListener = new ChildEventListener() {
                /*
                Any post that is added to the Realtime Database should also be displayed within the
                UI via the RecyclerView. In order for that to happen, we read from the database via
                a ChildEventListener.

                onChildAdded() is called when the app is initially launched as well as  each time a
                new child is added to the database.
                */
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    // Here, we are simply getting the post and adding it to the adapter
                    Post addedPost = dataSnapshot.getValue(Post.class);
                    mAdapter.addData(addedPost);

                    if (mState != null){
                        ((LinearLayoutManager) mRecyclerView.getLayoutManager()).onRestoreInstanceState(mState);
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            // Finally, we add the ChildEventListener to mPostsReference
            mPostsReference.addChildEventListener(mPostsListener);
        }
    }

    /**
     * Detaches our <code>ChildEventListener</code> to our <code>DatabaseReference</code> when necessary.
     */
    private void detachPostsReadListener() {
        // We first check if mPostsListener is not null in order to ensure that it is already
        // attached to a listener
        if (mPostsListener != null) {
            mPostsReference.removeEventListener(mPostsListener);
            mPostsListener = null;
        }
    }

    /**
     * Performs necessary setup when the user signs in. This includes saving the username and
     * calling <code>attachedPostsReadListener()</code> if necessary.
     */
    private void onSignedInInitialize(String username) {
        // We extract the username after the user has signed-in to the newsfeed
        mUsername = username;

        /*
        We also save the username to SharedPreferences so that it can later be retrieved. We don't
        just want to set the username when the user first signs in. The user can also come back to
        Newsfeed once they enter other activities within the app. Within onCreate() we set the
        username to anonymous until the user signs in. However, if the user is already signed-in
        and they come back to Newsfeed, then their username will be set to anonymous. In order to
        have the username properly set, we will also save it to SharedPreferences and then extract
        it when appropriate.
         */
        Utilities.saveUsername(this, username);

        attachedPostsReadListener();
    }

    /**
     * Performs necessary clean-up when the user signs out. This includes clearing out the
     * adapter for our RecyclerView and calling <code>detachPostsReadListener()</code> if
     * necessary.
     */
    private void onSignedOutCleanUp() {
        // This would be a good place to clear the username, if you have need for one
        mAdapter.clearData();
        detachPostsReadListener();
    }

    /**
     * An overriden method from <code>PostTypeSelection</code> which launches the
     * <code>PostSearch</code> activity when the user clicks on the OK button within the dialog.
     * This is a calback method within <code>PostTypeSelection</code>, which allows any activity
     * to specify what happens when the user clicks on the OK button.
     *
     * @param postType An integer which indicates what type of post the user has selected.
     */
    @Override
    public void onPositiveClick(int postType) {
        // We'll start the song search activity here
        Log.i(TAG, "Ok button clicked within dialog");

        Intent intent = new Intent(this, PostSearch.class);
        intent.putExtra(PostSearch.EXTRA_SEARCH_TYPE, postType);
        intent.putExtra(PostSearch.EXTRA_LAUNCH_DIALOG, false);

        startActivity(intent);
    }
}
