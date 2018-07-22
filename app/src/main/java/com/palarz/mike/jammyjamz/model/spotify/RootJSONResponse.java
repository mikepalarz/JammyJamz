package com.palarz.mike.jammyjamz.model.spotify;

import com.google.gson.annotations.SerializedName;

/**
 * A class that was created to contain multiple #Paging objects. Because a search request may
 * contain multiple types of data (such as tracks, artists, albums, etc.), the root of the
 * JSON response will contain a paging object for each type of data that was requested. Even
 * if a single type of data is requested, the root of the JSON response will change depending
 * on what type of data is requested. For example, if tracks are requested, then the root
 * will contain a key called "tracks". However, if artists are searched for, then the root
 * of the JSON will contain a key called "artists". However, it will not contain a "tracks"
 * key since that is not what was searched for. If both tracks and artists were included in
 * the search, then both keys will be present.
 */

public class RootJSONResponse {

    @SerializedName("tracks")
    PagingTracks mPagingTracks;

    @SerializedName("albums")
    PagingAlbums mPagingAlbums;

    @SerializedName("artists")
    PagingArtists mPagingArtists;

    public RootJSONResponse() {
        this.mPagingTracks = new PagingTracks();
        this.mPagingAlbums = new PagingAlbums();
        this.mPagingArtists = new PagingArtists();
    }

    public RootJSONResponse(PagingTracks pagingTracks, PagingAlbums pagingAlbums, PagingArtists pagingArtists) {
        this.mPagingTracks = pagingTracks;
        this.mPagingAlbums = pagingAlbums;
        this.mPagingArtists = pagingArtists;
    }

    public PagingTracks getPagingTracks() {
        return mPagingTracks;
    }

    public PagingAlbums getPagingAlbums() {
        return mPagingAlbums;
    }

    public PagingArtists getPagingArtists() {
        return mPagingArtists;
    }
}
