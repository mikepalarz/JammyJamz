package com.palarz.mike.jammyjamz.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.firebase.ui.auth.AuthUI;
import com.palarz.mike.jammyjamz.R;
import com.palarz.mike.jammyjamz.model.Post;

public class WritePost extends AppCompatActivity {

    private static final String TAG = WritePost.class.getSimpleName();
    public static final String EXTRA_CONTENT = "com.palarz.mike.jammyjamz.activity.WritePost.extra_content";

    // Contains the message that will be added to the post
    private EditText mMessage;
    private ImageButton mSendButton;
    // Post object that was received from PostSearch
    private Post mPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_post);

        Toolbar toolbar = findViewById(R.id.write_post_toolbar);
        setSupportActionBar(toolbar);

        mMessage = (EditText) findViewById(R.id.write_post_post_message);

        Intent receivedIntent = getIntent();
        if (receivedIntent != null && receivedIntent.hasExtra(EXTRA_CONTENT)){
            mPost = receivedIntent.getParcelableExtra(EXTRA_CONTENT);
        }

        mSendButton = (ImageButton) findViewById(R.id.write_post_send_button);
        mSendButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String currentMessage = mMessage.getText().toString();
                mPost.setMessage(currentMessage);

                // Create an intent for Newsfeed with the Post with the added message
                Intent intent = new Intent(WritePost.this, Newsfeed.class);
                intent.putExtra(Newsfeed.EXTRA_NEW_POST, mPost);
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_write_post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){

            case R.id.newsfeed_menu_action_sign_out:
                // TODO: Not the most elegant solution, try to think of something better
                AuthUI.getInstance().signOut(this);
                Intent intent = new Intent(this, Newsfeed.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
