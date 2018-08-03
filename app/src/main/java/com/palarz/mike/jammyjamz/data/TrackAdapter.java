package com.palarz.mike.jammyjamz.data;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.palarz.mike.jammyjamz.R;
import com.palarz.mike.jammyjamz.activity.WritePost;
import com.palarz.mike.jammyjamz.model.spotify.Track;
import com.squareup.picasso.Picasso;

public class TrackAdapter extends PostSearchAdapter<Track> {

    private static final String TAG = TrackAdapter.class.getSimpleName();

    public TrackAdapter(Context context){
        super(context);
    }

    @NonNull
    @Override
    public PostSearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View searchResult = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.list_item_search_result, parent, false);

        return new TrackViewHolder(searchResult);
    }

    protected class TrackViewHolder extends PostSearchViewHolder {

        protected TrackViewHolder(View itemView){
            super(itemView);
        }

        @Override
        protected void bind(Track data) {
            super.mTitle.setText(data.getTitle());
            super.mArtist.setText(data.getArtistNames());

            if (!TextUtils.isEmpty(data.getAlbum().getLargeAlbumCover())) {
                Picasso.get()
                        .load(Uri.parse(data.getAlbum().getLargeAlbumCover()))
                        .error(R.drawable.ic_no_cover)
                        .into(super.mCover);
            }
        }

        @Override
        protected void handleOnClick(Track data, Context context){
            Log.i(TAG, "Here is the track: " + data.toString());

            Intent intent = new Intent(context, WritePost.class);
            context.startActivity(intent);
        }
    }

}
