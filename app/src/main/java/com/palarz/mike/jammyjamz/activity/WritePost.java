package com.palarz.mike.jammyjamz.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import com.palarz.mike.jammyjamz.R;
import com.palarz.mike.jammyjamz.model.Post;

public class WritePost extends AppCompatActivity {

    private static final String TAG = WritePost.class.getSimpleName();
    public static final String EXTRA_CONTENT = "com.palarz.mike.jammyjamz.activity.WritePost.extra_content";

    private EditText mMessage;
    private Post mPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_post);

        mMessage = (EditText) findViewById(R.id.write_post_post_message);

        Intent receivedIntent = getIntent();
        if (receivedIntent != null && receivedIntent.hasExtra(EXTRA_CONTENT)){
            mPost = receivedIntent.getParcelableExtra(EXTRA_CONTENT);
            Log.i(TAG, "Received Post:\n" + mPost.toString());
        }

    }
}
