package com.palarz.mike.jammyjamz;

import android.app.Application;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class JammyJamzApplication extends Application {

    private static JammyJamzApplication mInstance;

    public static synchronized JammyJamzApplication getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        // Enabling persistence for offline capabilities
        firebaseDatabase.setPersistenceEnabled(true);
    }

    public void setupNoInternetIndicator(final TextView connectionIndicator) {
        // Getting a reference to where Firebase stores connection state
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(".info/connected");
        // We show/hide the connection indicator depending on the connection state
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean connected = dataSnapshot.getValue(Boolean.class);
                if (connected) {
                    connectionIndicator.setVisibility(View.GONE);
                } else {
                    connectionIndicator.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
