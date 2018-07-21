package com.palarz.mike.jammyjamz;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * A Retrofit client which includes HTTP request methods that are used in order to perform search
 * requests to the Spotify Web API.
 */

public interface SongClient {

    // The base URL that is used when obtaining an access token for the Spotify Web API
    String BASE_URL_ACCOUNTS = "https://accounts.spotify.com/";
    // The base URL that is used for performing all search requests
    String BASE_URL_SEARCH = "https://api.spotify.com/";

    // HTTP request used to obtain the access token
    @FormUrlEncoded
    @POST("api/token")
    Call<TokenResponse> getAccessToken(@Header("Authorization") String encodedIDAndSecret, @Field("grant_type") String grantType);

    // HTTP request used to search for an individual track
    @GET("v1/search?type=track&market=US")
    Call<RootJSONResponse> searchForTrack(@Header("Authorization") String accessToken, @Query("q") String query);


}
