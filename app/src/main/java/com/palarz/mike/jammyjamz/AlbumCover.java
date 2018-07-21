package com.palarz.mike.jammyjamz;

import com.google.gson.annotations.SerializedName;

/**
 * A class which represents an image object from the Spotify Web API:
 *
 * https://beta.developer.spotify.com/documentation/web-api/reference/object-model/#image-object
 *
 * This class is used in order to provide an album cover when the search results are displayed
 * to the user.
 */

class AlbumCover {

    // The image height in pixels. If unknown: null or not returned.
    @SerializedName("height")
    int mHeight;

    // The image width in pixels. If unknown: null or not returned.
    @SerializedName("width")
    int mWidth;

    // The source URL of the image.
    @SerializedName("url")
    String mURL;

    public AlbumCover() {
        this.mHeight = 0;
        this.mWidth = 0;
        this.mURL = "";
    }

    public AlbumCover(int height, int width, String url) {
        this.mHeight = height;
        this.mWidth = width;
        this.mURL = url;
    }

    public int getHeight() {
        return mHeight;
    }

    public int getWidth() {
        return mWidth;
    }

    public String getURL() {
        return mURL;
    }

}
