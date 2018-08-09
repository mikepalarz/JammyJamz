package com.palarz.mike.jammyjamz.model.spotify;

import com.google.gson.annotations.SerializedName;
import com.palarz.mike.jammyjamz.model.Post;

import java.util.ArrayList;
import java.util.List;

/**
 * A class which represents an Artist object from the Spotify Web API:
 *
 * https://beta.developer.spotify.com/documentation/web-api/reference/object-model/#artist-object-full
 *
 */

public class Artist extends SpotifyObject {

    @SerializedName("name")
    String mName;

    @SerializedName("images")
    List<SpotifyImage> mImages;

    public Artist() {
        super();

        this.mName = "";
        mImages = new ArrayList<>();
    }

    public Artist(String href, String id, String type, String uri, String name, List<SpotifyImage> images) {
        super(href, id, type, uri);

        this.mName = name;
        this.mImages = images;
    }

    public String getName() {
        return mName;
    }

    public List<SpotifyImage> getImages() {
        return mImages;
    }

    public String getLargeImage(){
        return SpotifyImage.getLargeImage(mImages);
    }

    /**
     * Creates a comma-separated String of artist names.
     *
     * @param artists A <code>List</code> of Artist objects.
     * @return A comma-separated String of artist names.
     */
    public static String getArtistNames(List<Artist> artists){
        if (artists == null || artists.size() == 0) {
            return "";
        }

        String artistNames = artists.get(0).getName();

        for (int i = 1; i < artists.size(); i++) {
            artistNames += ", " + artists.get(i).getName();
        }

        return artistNames;
    }

    @Override
    public String toString() {
        return "Name: " + getName();
    }

    /**
     * Creates a Post from an Artist.
     *
     * @return A Post which is representative of the Artist object.
     */
    @Override
    public Post createPost() {
        Post aPost = new Post();
        aPost.setTitle(mName);
        aPost.setPhotoUrl(getLargeImage());

        return aPost;
    }

}
