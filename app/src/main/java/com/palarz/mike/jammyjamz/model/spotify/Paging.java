package com.palarz.mike.jammyjamz.model.spotify;

import com.google.gson.annotations.SerializedName;

/**
 * A class which represents a Paging object from the Spotify Web API:
 *
 * https://beta.developer.spotify.com/documentation/web-api/reference/object-model/#paging-object
 *
 * It is an offset-based paging object that is a container for the requested data. For my
 * implementation, I've considered this to be a super-class. For every search request that is
 * made, there can be multiple paging objects which are returned. For example, Spotify
 * search request can search for both artists and tracks:
 *
 * https://api.spotify.com/v1/search?type=track,artist
 *
 * In this case, 2 paging objects will be returned: a paging object under the key "tracks"
 * and another with the key "artists". Each paging object will contain the same key/value
 * pairs: "href", "limit", "next", "previous", "total", and "items". The main difference is what's
 * contained within the "items" key. For the "tracks" paging object, "items" will contain track
 * objects and likewise for the "artists" paging object. For this reason, a PagingTracks subclass
 * has been made so that the "items" key is handled appropriately for track objects.
 *
 * For the time being, search requests are only being made for track objects. However, in
 * the future, artists requests will also be made. In that case, an ArtistsPaging class will
 * also be created.
 *
 * Even if multiple types aren't specified in the query of the HTTP request, the "items" key will
 * contain different data depending on what was specified in the type query.
 */

public class Paging {

    // A link to the Web API endpoint returning the full result of the request.
    @SerializedName("href")
    private String mHref;

    // The maximum number of items in the response (as set in the query or by default).
    @SerializedName("limit")
    private int mLimit;

    // URL to the next page of items. ( null if none)
    @SerializedName("next")
    private String mNext;

    // The offset of the items returned (as set in the query or by default).
    @SerializedName("offset")
    private int mOffset;

    // URL to the previous page of items. ( null if none)
    @SerializedName("previous")
    private String mPrevious;

    // The total number of items available to return.
    @SerializedName("total")
    private int mTotal;

    public Paging() {
        this.mHref = "";
        this.mLimit = 0;
        this.mNext = "";
        this.mOffset = 0;
        this.mPrevious = "";
        this.mTotal = 0;
    }

    public Paging(String href, int limit, String next, int offset, String previous, int total) {
        this.mHref = href;
        this.mLimit = limit;
        this.mNext = next;
        this.mOffset = offset;
        this.mPrevious = previous;
        this.mTotal = total;
    }

    public String getHref() {
        return mHref;
    }

    public int getLimit() {
        return mLimit;
    }

    public String getNext() {
        return mNext;
    }

    public int getOffset() {
        return mOffset;
    }

    public String getPrevious() {
        return mPrevious;
    }

    public int getTotal() {
        return mTotal;
    }


}
