package com.palarz.mike.jammyjamz.model.spotify;

import com.google.gson.annotations.SerializedName;
import com.palarz.mike.jammyjamz.model.Post;

public abstract class SpotifyObject {

    @SerializedName("href")
    private String mHref;

    @SerializedName("id")
    private String mID;

    @SerializedName("type")
    private String mType;

    @SerializedName("uri")
    private String mUri;

    protected SpotifyObject() {
        mHref = "";
        mID = "";
        mType = "";
        mUri = "";
    }

    protected SpotifyObject(String href, String id, String type, String uri) {
        this.mHref = href;
        this.mID = id;
        this.mType = type;
        this.mUri = uri;
    }

    protected String getHref() {
        return mHref;
    }

    protected void setHref(String mHref) {
        this.mHref = mHref;
    }

    protected String getID() {
        return mID;
    }

    protected void setID(String mID) {
        this.mID = mID;
    }

    protected String getType() {
        return mType;
    }

    protected void setType(String mType) {
        this.mType = mType;
    }

    protected String getUri() {
        return mUri;
    }

    protected void setUri(String mUri) {
        this.mUri = mUri;
    }

    public abstract Post createPost();


}
