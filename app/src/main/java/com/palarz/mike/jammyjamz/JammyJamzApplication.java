package com.palarz.mike.jammyjamz;

import android.app.Application;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.palarz.mike.jammyjamz.data.SpotifyAuthorizationService;

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

        // If a new access token is needed, then we'll retrieve it right away when the app is launched
        if (Utilities.isAccessTokenExpired(this)){
            Intent accessTokenIntent = new Intent(this, SpotifyAuthorizationService.class);
            accessTokenIntent.setAction(SpotifyAuthorizationService.ACTION_UPDATE_ACCESS_TOKEN);
            startService(accessTokenIntent);
        }
    }

}
