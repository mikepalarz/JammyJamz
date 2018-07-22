package com.palarz.mike.jammyjamz.data;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.palarz.mike.jammyjamz.R;
import com.palarz.mike.jammyjamz.model.spotify.Artist;
import com.squareup.picasso.Picasso;

public class ArtistAdapter extends PostSearchAdapter<Artist> {

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
                        .error(R.drawable.ic_no_cover)
                        .into(super.mCover);
            }
        }

    }

}
