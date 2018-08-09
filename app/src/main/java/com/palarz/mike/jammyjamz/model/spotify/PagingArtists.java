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
 * when a search request is made for artists, the "items" key will contain Artist objects.
 */

public class PagingArtists extends Paging {

    @SerializedName("items")
    private List<Artist> mArtists;

    public PagingArtists() {
        super();
        this.mArtists = new ArrayList<>();
    }

    public PagingArtists(String href, int limit, String next, int offset, String previous, int total, ArrayList<Artist> artists) {
        super(href, limit, next, offset, previous, total);
        this.mArtists = artists;
    }

    public List<Artist> getArtists() {
        return mArtists;
    }


}
