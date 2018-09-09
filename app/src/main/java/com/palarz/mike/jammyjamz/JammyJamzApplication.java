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

    /*
    TODO: There is a problem with this method: we should also remove the ValueEventListener
    within onPause() of either the application as a whole or for each activity. Otherwise, there's
    a chance that the indicator will still be shown if the user locks their device for too long
    and then reopens the app. An immediate idea is to use use a callback method between the
    application and activity, since the application class doesn't have direct access to the views
    contained within the activity.
     */
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
