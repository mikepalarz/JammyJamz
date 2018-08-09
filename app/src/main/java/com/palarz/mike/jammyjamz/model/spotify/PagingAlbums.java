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
 * when a search request is made for albums, the "items" key will contain Album objects.
 */

public class PagingAlbums extends Paging {

    @SerializedName("items")
    private List<Album> mAlbums;

    public PagingAlbums() {
        super();
        this.mAlbums = new ArrayList<>();
    }

    public PagingAlbums(String href, int limit, String next, int offset, String previous, int total, ArrayList<Album> albums) {
        super(href, limit, next, offset, previous, total);
        this.mAlbums = albums;
    }

    public List<Album> getAlbums() {
        return mAlbums;
    }

}
