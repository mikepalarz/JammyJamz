package com.palarz.mike.jammyjamz;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.palarz.mike.jammyjamz.R;
import com.palarz.mike.jammyjamz.model.Post;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import okhttp3.internal.Util;


public class Utilities {

    private static final String TAG = Utilities.class.getSimpleName();

    // A default username in case the user isn't logged in somehow
    public static final String USERNAME_ANONYMOUS = "Anonymous";

    /**
     * Provides the current username. This is a helper method which provides the current username.
     * If the username has been previously saved to <code>SharedPreferneces</code>, then
     * the <code>String</code> is extracted from <code>SharedPreferences</code> and returned.
     * Otherwise, the returned <code>String</code> is just <code>USERNAME_ANONYMOUS</code>.
     *
     * @return The current username.
     */
    public static String getUsername(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String savedUsername = preferences.getString(context.getString(R.string.shared_preferences_key_username), "");
        if (!savedUsername.isEmpty()){
            return savedUsername;
        } else {
            return USERNAME_ANONYMOUS;
        }
    }

    public static void saveUsername(Context context, String username){
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(context.getString(R.string.shared_preferences_key_username), username);
        editor.commit();
    }

    public static void setupArtwork(Post post, final ImageView artwork, final View background,
                                    final TextView title, final TextView artist){
        Picasso.get()
                .load(post.getPhotoUrl())
                .placeholder(R.drawable.ic_artwork_placeholder)
                .error(R.drawable.ic_error)
                .into(artwork, new Callback() {
                    @Override
                    public void onSuccess() {
                        Bitmap bitmap = ((BitmapDrawable) artwork.getDrawable()).getBitmap();

                        Palette.from(bitmap)
                                .generate(new Palette.PaletteAsyncListener() {
                                    @Override
                                    public void onGenerated(@NonNull Palette palette) {
                                        Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();

                                        if (vibrantSwatch != null){
                                            background.setBackgroundColor(vibrantSwatch.getRgb());
                                            title.setTextColor(vibrantSwatch.getTitleTextColor());
                                            artist.setTextColor(vibrantSwatch.getBodyTextColor());
                                        }

                                    }
                                });
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
    }

    public static String getUserPhoto(){
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null){

            Uri photoUri = currentUser.getPhotoUrl();
            if (photoUri != null){
                return photoUri.toString();
            } else {
                Log.d(TAG, "Profile picture URI does not exist");
                return "";
            }

        } else {
            Log.d(TAG, "Current user is not signed in");
            return "";
        }
    }

    public static void setupProfilePicture(Post post, ImageView imageView){
        String profilePicUri = post.getProfilePicture();

        if (!profilePicUri.isEmpty()){
            Picasso.get()
                    .load(profilePicUri)
                    .placeholder(R.drawable.ic_profile_pic_placeholder)
                    .into(imageView);
        }
    }
}


