package com.palarz.mike.jammyjamz;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

public class TrackAdapter extends PostSearchAdapter<Track> {

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
            super.getTitle().setText(data.getTitle());
            super.getArtist().setText(data.getArtistNames());

            if (!TextUtils.isEmpty(data.getAlbum().getLargeAlbumCover())) {
                Picasso.get()
                        .load(Uri.parse(data.getAlbum().getLargeAlbumCover()))
                        .error(R.drawable.ic_no_cover)
                        .into(super.getCover());
            }
        }
    }

}
