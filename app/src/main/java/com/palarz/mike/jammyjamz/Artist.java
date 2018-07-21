package com.palarz.mike.jammyjamz;

import com.google.gson.annotations.SerializedName;

/**
 * A class which represents an Artist object from the Spotify Web API:
 *
 * https://beta.developer.spotify.com/documentation/web-api/reference/object-model/#artist-object-full
 *
 * This class is used in order to provide a String of artists for each track when the search
 * results are displayed to the user.
 */

public class Artist {

    @SerializedName("name")
    String mName;

    public Artist() {
        this.mName = "";
    }

    public Artist(String name) {
        this.mName = name;
    }

    public String getName() {
        return mName;
    }
}
