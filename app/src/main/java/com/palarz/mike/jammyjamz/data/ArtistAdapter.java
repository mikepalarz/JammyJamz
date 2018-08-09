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
import com.palarz.mike.jammyjamz.model.spotify.Artist;
import com.squareup.picasso.Picasso;

/**
 * A subclass of PostSearchAdapter that is appropriate for Artist objects.
 */

public class ArtistAdapter extends PostSearchAdapter<Artist> {

    private static final String TAG = ArtistAdapter.class.getSimpleName();

    public ArtistAdapter(Context context){
        super(context);
    }

    @NonNull
    @Override
    public PostSearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View searchResult = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.list_item_search_result, parent, false);

        return new ArtistViewHolder(searchResult);
    }


    protected class ArtistViewHolder extends PostSearchViewHolder {

        protected ArtistViewHolder(View view){
            super(view);
        }

        @Override
        protected void bind(Artist data){
            super.mTitle.setText(data.getName());

            if (!TextUtils.isEmpty(data.getLargeImage())){
                Picasso.get()
                        .load(Uri.parse(data.getLargeImage()))
                        .error(R.drawable.ic_error)
                        .into(super.mCover);
            }
        }

    }

}
