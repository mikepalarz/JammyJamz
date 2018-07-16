package com.palarz.mike.jammyjamz;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Newsfeed extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private NewsfeedAdapter mAdapter;

    // Firebase member variables
    private FirebaseDatabase mFirebaseDatabase;
    // A reference to the "posts" node of the Realtime Database
    private DatabaseReference mPostsReference;
    // Listens to any posts which have been added to the Realtime Database
    private ChildEventListener mPostsListener;

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

                Post post = new Post("Sweet Child of Mine", "Guns 'n Roses", "https://images-na.ssl-images-amazon.com/images/I/71H9ZR6EGFL._SL1400_.jpg");
                mPostsReference.push().setValue(post);
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.newsfeed_recyclerview);
        mRecyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);

        mAdapter = new NewsfeedAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        // Initializing the Firebase Database and reference
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mPostsReference = mFirebaseDatabase.getReference().child("posts");

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_newsfeed, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
