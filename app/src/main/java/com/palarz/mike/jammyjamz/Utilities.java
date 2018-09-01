package com.palarz.mike.jammyjamz;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.graphics.Palette;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.palarz.mike.jammyjamz.R;
import com.palarz.mike.jammyjamz.model.Post;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;


public class Utilities {

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
}


