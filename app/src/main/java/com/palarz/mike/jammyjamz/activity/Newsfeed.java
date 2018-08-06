package com.palarz.mike.jammyjamz.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.palarz.mike.jammyjamz.data.NewsfeedAdapter;
import com.palarz.mike.jammyjamz.fragment.PostTypeSelection;
import com.palarz.mike.jammyjamz.R;
import com.palarz.mike.jammyjamz.model.Post;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Newsfeed extends AppCompatActivity implements PostTypeSelection.PostTypeSelectionListener {

    // Request code used within onActivityResult(); this is used to identify the FirebaseUI sign-in
    // activity within onActivityResult(), which is called after the user signs in
    private static final int RC_SIGN_IN = 1;

    // String which is used to identify when this activity receives an instance of Post within an Intent
    public static final String EXTRA_NEW_POST = "com.palarz.mike.jammyjamz.activity.Newsfeed.new_post";

    private static final String TAG = Newsfeed.class.getSimpleName();

    private static final String USERNAME_ANONYMOUS = "Anonymous";

    private static final String PREFERENCES_KEY_USERNAME = "com.palarz.mike.jammyjamz.activity.Newsfeed.username";

    private RecyclerView mRecyclerView;
    private NewsfeedAdapter mAdapter;


    /*
                                    Firebase member variables
     */
    // A reference to our Realtime Database
    private FirebaseDatabase mFirebaseDatabase;
    // A reference to the "posts" node of the Realtime Database
    private DatabaseReference mPostsReference;
    // Listens to any posts which have been added to the Realtime Database
    private ChildEventListener mPostsListener;
    private FirebaseAuth mFirebaseAuth;
    // Listens to any changes in authentication state; i.e., this is a listener which detects when
    // the user is either signed-in or signed-out
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    // A String used to store the current username
    private String mUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newsfeed);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PostTypeSelection dialog = new PostTypeSelection();
                dialog.show(getSupportFragmentManager(), "dialog");
            }
        });

        // We'll set mUsername to the current username, or anonymous if the user isn't yet signed in
        mUsername = getUsername();

        mRecyclerView = (RecyclerView) findViewById(R.id.newsfeed_recyclerview);
        mRecyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);

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
                if (currentUser != null) {
                    // The user is signed in
                    onSignedInInitialize(currentUser.getDisplayName());

                } else {
                    // The user is signed out
                    onSignedOutCleanUp();
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

        Intent receivedIntent = getIntent();
        if (receivedIntent != null && receivedIntent.hasExtra(EXTRA_NEW_POST)){
            Post newPost = receivedIntent.getParcelableExtra(EXTRA_NEW_POST);
            newPost.setUsername(mUsername);
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
                Toast.makeText(this, "Wassup, Jammer?", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Peace out, homey", Toast.LENGTH_SHORT).show();
                // If the user canceled the sign-in process (either while in the midst of it or by
                // pressing the back button once the sign-in UI was shown), then we'll bring them
                // back to the Newsfeed activity
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
    }

    @Override
    protected void onPause() {
        super.onPause();

        // We first check if mAuthStateListener is null, meaning that is hasn't been previously removed
        if (mAuthStateListener != null) {
            // We then remove the listener in onPause(). We do that here since onPause() is called
            // when the app is no longer visible
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }

        // We then detach the child events listener and clear any data that may be in our adapter
        detachPostsReadListener();
        mAdapter.clearData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_newsfeed, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action_sign_out:
                // If the user clicks on the sign-out button within the menu, then we will start the sign-out process
                AuthUI.getInstance().signOut(this);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void attachedPostsReadListener() {
        // We first check if mPostsListener is null in order to determine if it was previously
        // attached to a database reference
        if (mPostsListener == null) {
            mPostsListener = new ChildEventListener() {
                /*
                Any post that is added to the Realtime Database should also be displayed within the
                UI via the RecyclerView. In order for that to happen, we read from the database via
                a ChildEventListener.

                onChildAdded() is called when the app is initially launched as well as when each time a
                new child is added to the database.
                */
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    Post addedPost = dataSnapshot.getValue(Post.class);
                    mAdapter.addData(addedPost);
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

            mPostsReference.addChildEventListener(mPostsListener);
        }
    }

    private void detachPostsReadListener() {
        // We first check if mPostsListener is not null in order to ensure that it is already
        // attached to a listener
        if (mPostsListener != null) {
            mPostsReference.removeEventListener(mPostsListener);
            mPostsListener = null;
        }
    }

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
        SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
        editor.putString(PREFERENCES_KEY_USERNAME, username);
        editor.commit();

        attachedPostsReadListener();
    }

    private void onSignedOutCleanUp() {
        // This would be a good place to clear the username, if you have need for one
        mAdapter.clearData();
        detachPostsReadListener();
    }

    @Override
    public void onPositiveClick(int postType) {
        // We'll start the song search activity here
        Log.i(TAG, "Ok button clicked within dialog");

        Intent intent = new Intent(this, PostSearch.class);
        intent.putExtra(PostSearch.EXTRA_POST_TYPE, postType);

        startActivity(intent);
    }

    private String getUsername(){
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        String savedUsername = preferences.getString(PREFERENCES_KEY_USERNAME, "");
        if (!savedUsername.isEmpty()){
            return savedUsername;
        } else {
            return USERNAME_ANONYMOUS;
        }
    }

}
