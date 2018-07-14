package com.palarz.mike.jammyjamz;

public class Post {

    private String mTitle;  // This will be either the name of the track or album
    private String mArtist;
    private String mPhotoUrl;

    public Post() {
        this.mTitle = "";
        this.mArtist = "";
        this.mPhotoUrl = "";
    }

    public Post(String title, String artist, String photoUrl) {
        this.mTitle = title;
        this.mArtist = artist;
        this.mPhotoUrl = photoUrl;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getArtist() {
        return mArtist;
    }

    public void setArtist(String mArtist) {
        this.mArtist = mArtist;
    }

    public String getPhotoUrl() {
        return mPhotoUrl;
    }

    public void setPhotoUrl(String mPhotoUrl) {
        this.mPhotoUrl = mPhotoUrl;
    }


}
