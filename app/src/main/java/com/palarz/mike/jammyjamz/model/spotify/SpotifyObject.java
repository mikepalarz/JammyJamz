package com.palarz.mike.jammyjamz.model.spotify;

import com.google.gson.annotations.SerializedName;
import com.palarz.mike.jammyjamz.model.Post;

/**
 * An abstract class that represents very basic information shared between {@link Track},
 * {@link Album}, and {@link Artist}.
 */

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

    /**
     * Creates a {@link Post} from an instance of a {@link SpotifyObject}. This method is necessary
     * since we are allowing users to create posts for track, album, or artist data. Therefore, a
     * {@link Track},{@link Album}, or {@link Artist} should be able to create a {@link Post}.
     *
     * @return A {@link Post} object created from the data of a {@link SpotifyObject}.
     */
    public abstract Post createPost();


}
