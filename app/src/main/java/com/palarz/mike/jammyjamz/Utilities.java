package com.palarz.mike.jammyjamz;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.transition.Explode;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.palarz.mike.jammyjamz.model.Post;
import com.palarz.mike.jammyjamz.networking.TokenResponse;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;


public class Utilities {

    private static final String TAG = Utilities.class.getSimpleName();

    // A default username in case the user isn't logged in somehow
    public static final String USERNAME_ANONYMOUS = "Anonymous";

    public static boolean isLollipop(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static void setTransitions(AppCompatActivity activity){
        // Check if we're running on Android 5.0 or higher
        if (isLollipop()) {
            // Apply activity transition
            activity.getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            activity.getWindow().setEnterTransition(new Explode());
            activity.getWindow().setExitTransition(new Explode());
        }
    }

    public static boolean isAccessTokenExpired(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.shared_preferences_file_name_default), Context.MODE_PRIVATE);
        String accessToken = sharedPreferences.getString(context.getString(R.string.shared_preferences_key_access_token), "");

        // If the access token has it's default value, then that means it hasn't yet been initialized.
        if (accessToken.isEmpty()) {
            return true;
        }

        // Otherwise, we will read from SharedPreferences to determine if the access token is expired or not
        long timeSaved = sharedPreferences.getLong(context.getString(R.string.shared_preferences_key_access_token_time_saved), 0L);
        long expiration = sharedPreferences.getLong(context.getString(R.string.shared_preferences_key_access_token_expiration), 0L);

        // Determining how much time has passed since we saved the access token
        long now = System.currentTimeMillis()/1000;
        long timePassed = Math.abs(now - timeSaved);

        if (timePassed >= expiration) {
            return true;
        } else {
            return false;
        }
    }

    public static void saveTokenResponse(Context context, TokenResponse tokenResponse){
        SharedPreferences.Editor editor = context.getSharedPreferences(context.getString(R.string.shared_preferences_file_name_default), Context.MODE_PRIVATE).edit();

        editor.putString(context.getString(R.string.shared_preferences_key_access_token), tokenResponse.getAccessToken());
        editor.putString(context.getString(R.string.shared_preferences_key_token_type), tokenResponse.getTokenType());
        editor.putLong(context.getString(R.string.shared_preferences_key_access_token_expiration), tokenResponse.getExpiration());
        editor.putLong(context.getString(R.string.shared_preferences_key_access_token_time_saved), System.currentTimeMillis()/1000);
        editor.commit();
    }

    public static String getAccessToken(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.shared_preferences_file_name_default), Context.MODE_PRIVATE);
        return sharedPreferences.getString(context.getString(R.string.shared_preferences_key_access_token), "");
    }

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


