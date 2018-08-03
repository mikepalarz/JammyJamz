package com.palarz.mike.jammyjamz.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;

import com.palarz.mike.jammyjamz.R;

public class WritePost extends AppCompatActivity {

    private EditText mPostMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_post);

        mPostMessage = (EditText) findViewById(R.id.write_post_post_message);

    }
}
