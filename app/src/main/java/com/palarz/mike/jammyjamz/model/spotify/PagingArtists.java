package com.palarz.mike.jammyjamz.model.spotify;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

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
