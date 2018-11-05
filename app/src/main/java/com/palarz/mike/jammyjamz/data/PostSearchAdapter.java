package com.palarz.mike.jammyjamz.data;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.palarz.mike.jammyjamz.R;
import com.palarz.mike.jammyjamz.Utilities;
import com.palarz.mike.jammyjamz.activity.WritePost;
import com.palarz.mike.jammyjamz.model.Post;
import com.palarz.mike.jammyjamz.model.spotify.SpotifyObject;
import com.palarz.mike.jammyjamz.data.SearchService.SearchType;

import java.util.ArrayList;
import java.util.List;

/**
 * An abstract adapter for PostSearch that is designed to handle Track, Album, and Artist objects.
 * Many thanks to the following SO post for this implementation:
 *
 * https://stackoverflow.com/questions/51460260/recyclerview-adapter-for-multiple-data-types/51460773?noredirect=1#comment89903863_51460773
 *
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

    /**
     *
     * @param context The context of the current activity
     * @param adapterType An integer that determines which subclass of PostSearchadapter will be
     *                    returned. This variable is dependent on the type of search that is to
     *                    be performed, so it is ultimately dependent on <code>mSearchType</code>
     *                    within PostSearch, which subsequently is determined by the type of search
     *                    the user would like to perform (track, album, or artist).
     * @return A new PostSearchAdapter subclass that is appropriate for the value of
     * <code>adapterType</code>.
     */
    public static PostSearchAdapter create(Context context, SearchType adapterType) {
        switch (adapterType){
            case TRACK:
                return new TrackAdapter(context);
            case ALBUM:
                return new AlbumAdapter(context);
            case ARTIST:
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

        /*
        We leave it up to a child class to actually implement this method. This is because a child
        will have different pieces of data from other child classes. Each child class will need to
        bind their data differently to the views within the ViewHolder.
         */
        protected abstract void bind(T data);


        @Override
        public void onClick(View v) {
            T data = mSearchResults.get(getAdapterPosition());
            Post aPost = data.createPost();
            aPost.setUsername(Utilities.getUsername(mContext));
            aPost.setProfilePicture(Utilities.getUserPhoto());

            Intent intent = new Intent(mContext, WritePost.class);
            intent.putExtra(WritePost.EXTRA_CONTENT, aPost);

            if (Utilities.isLollipop()){
                mContext.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation((Activity) mContext).toBundle());
            } else {
                mContext.startActivity(intent);
            }

        }
    }

}
