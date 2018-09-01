package com.palarz.mike.jammyjamz.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.palarz.mike.jammyjamz.R;
import com.palarz.mike.jammyjamz.Utilities;
import com.palarz.mike.jammyjamz.model.Post;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class WritePost extends AppCompatActivity {

    private static final String TAG = WritePost.class.getSimpleName();
    public static final String EXTRA_CONTENT = "com.palarz.mike.jammyjamz.activity.WritePost.extra_content";

    // Contains the message that will be added to the post
    private TextInputLayout mMessageContainer;
    private TextInputEditText mMessage;
    private ImageView mProfilePic;
    private TextView mUsername;
    private View mArtworkBackground;
    private ImageView mArtwork;
    private TextView mTitle;
    private TextView mArtist;
    // Post object that was received from PostSearch
    private Post mPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_post);

        Toolbar toolbar = findViewById(R.id.write_post_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mMessageContainer = findViewById(R.id.write_post_post_message_container);
        mMessage = findViewById(R.id.write_post_post_message);
        mProfilePic = findViewById(R.id.write_post_profile_pic);

        // Setting up the username TextView
        mUsername = findViewById(R.id.write_post_username);
        mUsername.setText(Utilities.getUsername(this));

        mArtworkBackground = findViewById(R.id.write_post_artwork_background);
        mArtwork = findViewById(R.id.write_post_artwork);
        mTitle = findViewById(R.id.write_post_title);
        mArtist = findViewById(R.id.write_post_artist);

        Intent receivedIntent = getIntent();
        if (receivedIntent != null && receivedIntent.hasExtra(EXTRA_CONTENT)){
            mPost = receivedIntent.getParcelableExtra(EXTRA_CONTENT);

            // Setting up the views that correspond to the post
            mTitle.setText(mPost.getTitle());
            mArtist.setText(mPost.getArtists());

            if (mPost.getPhotoUrl() != null){
                Utilities.setupArtwork(mPost, mArtwork, mArtworkBackground, mTitle, mArtist);
            }
            if (!mPost.getProfilePicture().isEmpty()) {
                Utilities.setupProfilePicture(mPost, mProfilePic);
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_write_post, menu);

        // onClick handler for action view tied to "Jam!" button
        final MenuItem addPost = menu.findItem(R.id.write_post_menu_action_add_post);
        addPost.getActionView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(addPost);
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){

            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;

            case R.id.write_post_menu_action_add_post:
                String currentMessage = mMessage.getText().toString();
                mPost.setMessage(currentMessage);

                // Create an intent for Newsfeed with the Post with the added message
                Intent intent = new Intent(WritePost.this, Newsfeed.class);
                intent.putExtra(Newsfeed.EXTRA_NEW_POST, mPost);
                startActivity(intent);
                return true;


            case R.id.write_post_menu_action_sign_out:
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                startActivity(new Intent(WritePost.this, Newsfeed.class));
                                finish();
                            }
                        });
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
