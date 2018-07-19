package com.palarz.mike.jammyjamz;

import android.content.Intent;
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
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Newsfeed extends AppCompatActivity {

    // Request code used within onActivityResult(); this is used to identify the FirebaseUI sign-in
    // activity within onActivityResult(), which is called after the user signs in
    private static final int RC_SIGN_IN = 123;

    private static final String USERNAME_ANONYMOUS = "Anonymous";

    private RecyclerView mRecyclerView;
    private NewsfeedAdapter mAdapter;

    // Only used for very basic testing purposes for now; this should be removed once the search
    // feature is implemented
    private List<Post> testPosts = new ArrayList<>();

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
                Post post = getRandomPost();
                post.setUsername(mUsername);
                mPostsReference.push().setValue(post);
            }
        });
        // Initialized 4 different Post objects, which are strictly used for testing purposes
        initializeTestPosts();

        // We'll set mUsername to anonymous until the user signs in
        mUsername = USERNAME_ANONYMOUS;

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

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // If the activity that we're returning from is the FirebaseUI login-in...
        if (requestCode == RC_SIGN_IN){
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Wassup, Jammer?", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Peace out, homey", Toast.LENGTH_SHORT).show();
                // If the user canceled the sign-in process (either while in the midst of it or by
                // pressing the back button once the sign-in UI was shown), then we'll bring them
                // back to the Newsfeed activity
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }

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

    private void initializeTestPosts() {
        Post post1 = new Post(mUsername, "Sweet Child of Mine", "Guns 'n Roses", "https://images-na.ssl-images-amazon.com/images/I/71H9ZR6EGFL._SL1400_.jpg");
        Post post2 = new Post(mUsername, "Alive", "Pearl Jam", "https://images-na.ssl-images-amazon.com/images/I/813p1x7Vc8L._SY355_.jpg");
        Post post3 = new Post(mUsername, "Kickstart My Heart", "Motley Crue", "https://images-na.ssl-images-amazon.com/images/I/717X-fRStVL._SL1036_.jpg");
        Post post4 = new Post(mUsername, "Back in Black", "AC/DC", "https://images-na.ssl-images-amazon.com/images/I/61sJIfuUSiL._SL1500_.jpg");

        testPosts.add(post1);
        testPosts.add(post2);
        testPosts.add(post3);
        testPosts.add(post4);

    }

    private Post getRandomPost(){
        int index = (int)(Math.random() * testPosts.size());

        return testPosts.get(index);
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
        attachedPostsReadListener();

    }

    private void onSignedOutCleanUp() {
        // This would be a good place to clear the username, if you have need for one
        mAdapter.clearData();
        detachPostsReadListener();

    }
}
