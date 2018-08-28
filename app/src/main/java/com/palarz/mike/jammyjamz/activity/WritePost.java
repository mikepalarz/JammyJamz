package com.palarz.mike.jammyjamz.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.palarz.mike.jammyjamz.R;
import com.palarz.mike.jammyjamz.model.Post;

public class WritePost extends AppCompatActivity {

    private static final String TAG = WritePost.class.getSimpleName();
    public static final String EXTRA_CONTENT = "com.palarz.mike.jammyjamz.activity.WritePost.extra_content";

    // Contains the message that will be added to the post
    private TextInputLayout mMessageContainer;
    private TextInputEditText mMessage;
    // Post object that was received from PostSearch
    private Post mPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_post);

        Toolbar toolbar = findViewById(R.id.write_post_toolbar);
        setSupportActionBar(toolbar);

        mMessageContainer = findViewById(R.id.write_post_post_message_container);
        mMessage = findViewById(R.id.write_post_post_message);

        Intent receivedIntent = getIntent();
        if (receivedIntent != null && receivedIntent.hasExtra(EXTRA_CONTENT)){
            mPost = receivedIntent.getParcelableExtra(EXTRA_CONTENT);
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
