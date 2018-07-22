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

public class Artist extends SpotifyObject {

    @SerializedName("name")
    String mName;

    public Artist() {
        super();

        this.mName = "";
    }

    public Artist(String href, String id, String type, String uri, String name) {
        super(href, id, type, uri);

        this.mName = name;
    }

    public String getName() {
        return mName;
    }
}
