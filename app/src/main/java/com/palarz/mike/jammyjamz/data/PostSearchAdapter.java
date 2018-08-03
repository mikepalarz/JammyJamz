package com.palarz.mike.jammyjamz.data;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.palarz.mike.jammyjamz.R;
import com.palarz.mike.jammyjamz.model.spotify.SpotifyObject;

import java.util.ArrayList;
import java.util.List;

/**
 * An adapter for the ListView within PostSearch which displays the results of the
 * search request.
 */

public abstract class PostSearchAdapter<T extends SpotifyObject> extends RecyclerView.Adapter<PostSearchAdapter.PostSearchViewHolder> {

    protected final Context mContext;
    protected final ArrayList<T> mSearchResults = new ArrayList<>();

    public PostSearchAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public void onBindViewHolder(@NonNull PostSearchAdapter.PostSearchViewHolder holder, int position) {
        holder.bind(mSearchResults.get(position));
    }

    @Override
    public int getItemCount() {

        return mSearchResults.size();

    }

    public void addData(List<T> newData){

        for (T data : newData){
            mSearchResults.add(data);
        }
        notifyDataSetChanged();
    }

    public void clearData(){
        mSearchResults.clear();
    }

    public static PostSearchAdapter create(Context context, int adapterType) {
        switch (adapterType){
            case 0:
                return new TrackAdapter(context);
            case 1:
                return new AlbumAdapter(context);
            case 2:
                return new ArtistAdapter(context);
            default:
                throw new IllegalArgumentException("Invalid adapter type: " + adapterType);

        }
    }


    protected abstract class PostSearchViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        protected final ImageView mCover;
        protected final TextView mTitle;
        protected final TextView mArtist;

        protected PostSearchViewHolder(View viewHolder) {
            super(viewHolder);

            mCover = (ImageView) viewHolder.findViewById(R.id.list_item_search_result_cover);
            mTitle = (TextView) viewHolder.findViewById(R.id.list_item_search_result_title);
            mArtist = (TextView) viewHolder.findViewById(R.id.list_item_search_result_artist);

            viewHolder.setOnClickListener(this);
        }

        protected ImageView getCover(){
            return mCover;
        }

        protected TextView getTitle(){
            return mTitle;
        }

        protected TextView getArtist(){
            return mArtist;
        }

        protected abstract void bind(T data);

        protected abstract void handleOnClick(T data, Context context);

        @Override
        public void onClick(View v) {
            T data = mSearchResults.get(getAdapterPosition());
            handleOnClick(data, mContext);
        }
    }

}
