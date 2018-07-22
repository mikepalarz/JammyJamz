package com.palarz.mike.jammyjamz.model.spotify;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * A class which represents an Album object from the Spotify Web API:
 *
 * https://beta.developer.spotify.com/documentation/web-api/reference/object-model/#album-object-full
 *
 */

public class Album extends SpotifyObject {

    // The name of the album. In case of an album takedown, the value may be an empty string.
    @SerializedName("name")
    String mAlbumTitle;

    // The cover art for the album in various sizes, widest first.
    @SerializedName("images")
    List<SpotifyImage> mAlbumCovers;

    @SerializedName("artists")
    List<Artist> mArtists;

    public Album() {
        super();

        this.mAlbumTitle = "";
        this.mAlbumCovers = new ArrayList<>();
        this.mArtists = new ArrayList<>();
    }

    public Album(String href, String id, String type, String uri, String albumTitle, ArrayList<SpotifyImage> albumCovers, ArrayList<Artist> artists) {
        super(href, id, type, uri);

        this.mAlbumTitle = albumTitle;
        this.mAlbumCovers = albumCovers;
        this.mArtists = artists;
    }

    public String getAlbumTitle() {
        return mAlbumTitle;
    }

    public List<SpotifyImage> getAlbumCovers() {
        return mAlbumCovers;
    }

    public String getLargeAlbumCover() {
        return SpotifyImage.getLargeImage(mAlbumCovers);
    }

    public List<Artist> getArtists(){
        return mArtists;
    }

    /**
     * Generates a comma-separated String of the artists associated to the track
     *
     * @return A comma-separated String of the artists associated to the track
     */
    public String getArtistNames() {
        return Artist.getArtistNames(mArtists);
    }

}