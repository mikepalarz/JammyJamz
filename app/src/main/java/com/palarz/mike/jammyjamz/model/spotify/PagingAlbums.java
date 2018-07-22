package com.palarz.mike.jammyjamz.model.spotify;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

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
