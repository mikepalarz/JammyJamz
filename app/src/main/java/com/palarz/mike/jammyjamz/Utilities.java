package com.palarz.mike.jammyjamz;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.palarz.mike.jammyjamz.R;


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

}
