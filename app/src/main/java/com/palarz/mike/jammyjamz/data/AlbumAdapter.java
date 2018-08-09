package com.palarz.mike.jammyjamz.data;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.palarz.mike.jammyjamz.R;
import com.palarz.mike.jammyjamz.model.spotify.Album;
import com.squareup.picasso.Picasso;

/**
 * A subclass of PostSearchAdapter that is appropriate for Album objects.
 */

public class AlbumAdapter extends PostSearchAdapter<Album> {

    private static final String TAG = AlbumAdapter.class.getSimpleName();

    public AlbumAdapter(Context context) {
        super(context);
    }

    @NonNull
    @Override
    public PostSearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View searchResult = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.list_item_search_result, parent, false);

        return new AlbumViewHolder(searchResult);
    }

    protected class AlbumViewHolder extends PostSearchViewHolder {

        protected AlbumViewHolder(View itemView){
            super(itemView);
        }

        @Override
        protected void bind(Album data) {
            super.mTitle.setText(data.getAlbumTitle());
            super.mArtist.setText(data.getArtistNames());

            if (!TextUtils.isEmpty(data.getLargeAlbumCover())) {
                Picasso.get()
                        .load(Uri.parse(data.getLargeAlbumCover()))
                        .error(R.drawable.ic_error)
                        .into(super.mCover);
            }
        }

    }

}
