package com.palarz.mike.jammyjamz.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.palarz.mike.jammyjamz.R;
import com.palarz.mike.jammyjamz.Utilities;
import com.palarz.mike.jammyjamz.model.Post;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * The adapter for the Newsfeed activity. This adapter displays all of the current Post objects
 * that are stored within the Realtime Database.
 */

public class NewsfeedAdapter extends RecyclerView.Adapter<NewsfeedAdapter.NewsfeedViewHolder> {

    private static final String TAG = "NewsfeedAdapter";

    private List<Post> mPosts;
    private Context mContext;

    public NewsfeedAdapter(Context context) {
        this.mContext = context;
        mPosts = new ArrayList<>();
    }

    @Override
    public int getItemCount() {
        if(mPosts == null) {
          return 0;
        }
        else {
            return mPosts.size();
        }
    }

    @NonNull
    @Override
    public NewsfeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.list_item_post, parent, false);

        return new NewsfeedViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(NewsfeedViewHolder viewHolder, int position) {
        Post currentPost = mPosts.get(position);
        viewHolder.bind(currentPost);

    }

    public void addData(Post addedPost) {
        mPosts.add(addedPost);
        notifyDataSetChanged();
    }

    public void clearData() {
        mPosts.clear();
    }

    public class NewsfeedViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.list_item_post_username) TextView holderUsername;
        @BindView(R.id.list_item_post_message) TextView holderMessage;
        @BindView(R.id.list_item_post_artwork) ImageView holderArtwork;
        @BindView(R.id.list_item_post_title) TextView holderTitle;
        @BindView(R.id.list_item_post_artist) TextView holderArtist;
        @BindView(R.id.list_item_post_artwork_background) View holderArtworkBackground;
        @BindView(R.id.list_item_post_profile_pic) ImageView holderProfilePic;

        public NewsfeedViewHolder(View viewHolder) {
            super(viewHolder);

            ButterKnife.bind(this, viewHolder);
        }

        public void bind(Post currentPost){
            if (currentPost.getPhotoUrl() != null) {
                Utilities.setupArtwork(currentPost, holderArtwork, holderArtworkBackground, holderTitle, holderArtist);
            }

            holderUsername.setText(currentPost.getUsername());
            holderTitle.setText(currentPost.getTitle());
            holderArtist.setText(currentPost.getArtists());
            holderMessage.setText(currentPost.getMessage());
            if (!currentPost.getProfilePicture().isEmpty()){
                Utilities.setupProfilePicture(currentPost, holderProfilePic);
            }

        }

    }
}
