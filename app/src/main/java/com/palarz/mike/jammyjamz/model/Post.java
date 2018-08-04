package com.palarz.mike.jammyjamz.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.PropertyName;

public class Post implements Parcelable {

    @PropertyName("title")
    private String mTitle;  // This will be either the name of the track or album
    @PropertyName("artists")
    private String mArtists;
    @PropertyName("photoUrl")
    private String mPhotoUrl;
    @PropertyName("userName")
    private String mUsername;

    public Post() {
        this.mTitle = "";
        this.mArtists = "";
        this.mPhotoUrl = "";
        this.mUsername = "";
    }

    public Post(String username, String title, String artist, String photoUrl) {
        this.mTitle = title;
        this.mArtists = artist;
        this.mPhotoUrl = photoUrl;
        this.mUsername = username;
    }

    public Post(Parcel input){
        mTitle = input.readString();
        mArtists = input.readString();
        mPhotoUrl = input.readString();
        mUsername = input.readString();
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

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        mUsername = username;
    }

    @Override
    public String toString(){
        return "Title: " + mTitle + "\tArtists: " + mArtists + "\tPhoto URL: " + mPhotoUrl + "\tUsername: " + mUsername;
    }

    // The following methods are necessary in order to implement the Parcelable interface
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel destination, int flags) {
        destination.writeString(mTitle);
        destination.writeString(mArtists);
        destination.writeString(mPhotoUrl);
        destination.writeString(mUsername);
    }

    public static final Parcelable.Creator<Post> CREATOR = new Parcelable.Creator<Post>(){
        @Override
        public Post createFromParcel(Parcel source) {
            return new Post(source);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

}
