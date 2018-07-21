package com.palarz.mike.jammyjamz;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * A class which represents an Album object from the Spotify Web API:
 *
 * https://beta.developer.spotify.com/documentation/web-api/reference/object-model/#album-object-full
 *
 */

public class Album {

    // The name of the album. In case of an album takedown, the value may be an empty string.
    @SerializedName("name")
    String mAlbumTitle;

    // The cover art for the album in various sizes, widest first.
    @SerializedName("images")
    List<AlbumCover> mAlbumCovers;

    public Album() {
        this.mAlbumTitle = "";
        this.mAlbumCovers = new ArrayList<>();
    }

    public Album(String albumTitle, ArrayList<AlbumCover> albumCovers) {
        this.mAlbumTitle = albumTitle;
        this.mAlbumCovers = albumCovers;
    }

    public String getAlbumTitle() {
        return mAlbumTitle;
    }

    public List<AlbumCover> getAlbumCovers() {
        return mAlbumCovers;
    }

    public String getLargeAlbumCover() {
        return mAlbumCovers.get(0).getURL();
    }

}
