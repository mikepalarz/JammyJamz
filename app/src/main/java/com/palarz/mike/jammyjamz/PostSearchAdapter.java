package com.palarz.mike.jammyjamz;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
            default:
                throw new IllegalArgumentException("Invalid adapter type: " + adapterType);

        }
    }


    protected abstract class PostSearchViewHolder extends RecyclerView.ViewHolder {

        protected final ImageView mCover;
        protected final TextView mTitle;
        protected final TextView mArtist;

        protected PostSearchViewHolder(View viewHolder) {
            super(viewHolder);

            mCover = (ImageView) viewHolder.findViewById(R.id.list_item_search_result_cover);
            mTitle = (TextView) viewHolder.findViewById(R.id.list_item_search_result_title);
            mArtist = (TextView) viewHolder.findViewById(R.id.list_item_search_result_artist);
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

    }

}
