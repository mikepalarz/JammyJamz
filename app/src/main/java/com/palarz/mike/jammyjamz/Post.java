package com.palarz.mike.jammyjamz;

import com.google.firebase.database.PropertyName;

public class Post {

    @PropertyName("title")
    private String mTitle;  // This will be either the name of the track or album
    @PropertyName("artists")
    private String mArtists;
    @PropertyName("photoUrl")
    private String mPhotoUrl;

    public Post() {
        this.mTitle = "";
        this.mArtists = "";
        this.mPhotoUrl = "";
    }

    public Post(String title, String artist, String photoUrl) {
        this.mTitle = title;
        this.mArtists = artist;
        this.mPhotoUrl = photoUrl;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getArtists() {
        return mArtists;
    }

    public void setArtists(String mArtist) {
        this.mArtists = mArtist;
    }

    public String getPhotoUrl() {
        return mPhotoUrl;
    }

    public void setPhotoUrl(String mPhotoUrl) {
        this.mPhotoUrl = mPhotoUrl;
    }


}
