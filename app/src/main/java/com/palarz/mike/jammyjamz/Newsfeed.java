package com.palarz.mike.jammyjamz;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

    private static final int RC_SIGN_IN = 123;

    private RecyclerView mRecyclerView;
    private NewsfeedAdapter mAdapter;

    private List<Post> testPosts = new ArrayList<>();

    // Firebase member variables
    private FirebaseDatabase mFirebaseDatabase;
    // A reference to the "posts" node of the Realtime Database
    private DatabaseReference mPostsReference;
    // Listens to any posts which have been added to the Realtime Database
    private ChildEventListener mPostsListener;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

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
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();

//                Post post = new Post("Sweet Child of Mine", "Guns 'n Roses", "https://images-na.ssl-images-amazon.com/images/I/71H9ZR6EGFL._SL1400_.jpg");
                Post post = getRandomPost();
                mPostsReference.push().setValue(post);
            }
        });

        initializeTestPosts();

        mRecyclerView = (RecyclerView) findViewById(R.id.newsfeed_recyclerview);
        mRecyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);

        mAdapter = new NewsfeedAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        // Initializing the Firebase Database and reference
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mPostsReference = mFirebaseDatabase.getReference().child("posts");

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            // The firebaseAuth parameter is guaranteed to indicate whether the user is authenticated or not
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
            case R.id.action_settings:
                AuthUI.getInstance().signOut(this);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initializeTestPosts() {
        Post post1 = new Post("Sweet Child of Mine", "Guns 'n Roses", "https://images-na.ssl-images-amazon.com/images/I/71H9ZR6EGFL._SL1400_.jpg");
        Post post2 = new Post("Alive", "Pearl Jam", "data:image/jpeg;base64,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");
        Post post3 = new Post("Kickstart My Heart", "Motley Crue", "data:image/jpeg;base64,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");
        Post post4 = new Post("Back in Black", "AC/DC", "data:image/png;base64,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");

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
        // This is a good place to extract the user's display name and include it in the post
        Log.d("Newsfeed", "Display name: " + username);

        attachedPostsReadListener();

    }

    private void onSignedOutCleanUp() {
        // This would be a good place to clear the username, if you have need for one
        mAdapter.clearData();
        detachPostsReadListener();

    }
}
