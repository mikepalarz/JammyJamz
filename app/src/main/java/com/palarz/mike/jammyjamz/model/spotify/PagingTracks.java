package com.palarz.mike.jammyjamz.model.spotify;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * A class which represents a Paging object from the Spotify Web API:
 *
 * https://beta.developer.spotify.com/documentation/web-api/reference/object-model/#paging-object
 *
 * It is an offset-based paging object that is a container for the requested data. For my
 * implementation, I've considered this to be a sub-class of the Paging class. This is because
 * when a search request is made for tracks, the "items" key will contain Track objects.
 */


public class PagingTracks extends Paging {

    @SerializedName("items")
    private ArrayList<Track> mTracks;

    public PagingTracks() {
        super();
        this.mTracks = new ArrayList<>();
    }

    public PagingTracks(String href, int limit, String next, int offset, String previous, int total, ArrayList<Track> tracks) {
        super(href, limit, next, offset, previous, total);
        this.mTracks = tracks;
    }

    public ArrayList<Track> getTracks() {
        return mTracks;
    }
}
