package com.palarz.mike.jammyjamz.model.spotify;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * A class which represents an image object from the Spotify Web API:
 *
 * https://beta.developer.spotify.com/documentation/web-api/reference/object-model/#image-object
 *
 * This class is used in order to provide an album cover when the search results are displayed
 * to the user.
 */

class SpotifyImage {

    // The image height in pixels. If unknown: null or not returned.
    @SerializedName("height")
    private int mHeight;

    // The image width in pixels. If unknown: null or not returned.
    @SerializedName("width")
    private int mWidth;

    // The source URL of the image.
    @SerializedName("url")
    private String mURL;

    public SpotifyImage() {
        this.mHeight = 0;
        this.mWidth = 0;
        this.mURL = "";
    }

    public SpotifyImage(int height, int width, String url) {
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

    public static String getLargeImage(List<SpotifyImage> images){
        if ( (images == null) || (images.size() == 0)){
            return "";
        } else{
            return images.get(0).getURL();
        }
    }

}
