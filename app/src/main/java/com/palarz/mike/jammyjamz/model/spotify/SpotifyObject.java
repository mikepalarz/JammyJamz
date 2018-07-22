package com.palarz.mike.jammyjamz.model.spotify;

import com.google.gson.annotations.SerializedName;

public class SpotifyObject {

    @SerializedName("href")
    private String mHref;

    @SerializedName("id")
    private String mID;

    @SerializedName("type")
    private String mType;

    @SerializedName("uri")
    private String mUri;

    public SpotifyObject() {
        mHref = "";
        mID = "";
        mType = "";
        mUri = "";
    }

    public SpotifyObject(String href, String id, String type, String uri) {
        this.mHref = href;
        this.mID = id;
        this.mType = type;
        this.mUri = uri;
    }

    public String getHref() {
        return mHref;
    }

    public void setHref(String mHref) {
        this.mHref = mHref;
    }

    public String getID() {
        return mID;
    }

    public void setID(String mID) {
        this.mID = mID;
    }

    public String getType() {
        return mType;
    }

    public void setType(String mType) {
        this.mType = mType;
    }

    public String getUri() {
        return mUri;
    }

    public void setUri(String mUri) {
        this.mUri = mUri;
    }


}
