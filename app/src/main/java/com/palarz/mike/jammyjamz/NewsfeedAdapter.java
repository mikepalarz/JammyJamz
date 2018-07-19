package com.palarz.mike.jammyjamz;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class NewsfeedAdapter extends RecyclerView.Adapter<NewsfeedAdapter.NewsfeedViewHolder> {

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

        if (currentPost.getPhotoUrl() != null) {
            // We will attempt to download the image
            // TODO: Add a placeholder and an error image
            Picasso.get()
                    .load(currentPost.getPhotoUrl())
                    .into(viewHolder.holderArtwork);
        }

        viewHolder.holderUsername.setText(currentPost.getUsername());
        viewHolder.holderTitle.setText(currentPost.getTitle());
        viewHolder.holderArtist.setText(currentPost.getArtists());

    }

    public void addData(Post addedPost) {
        mPosts.add(addedPost);
        notifyDataSetChanged();

    }

    public void clearData() {
        mPosts.clear();
    }

    public class NewsfeedViewHolder extends RecyclerView.ViewHolder {

        TextView holderUsername;
        ImageView holderArtwork;
        TextView holderTitle;
        TextView holderArtist;

        public NewsfeedViewHolder(View viewHolder) {
            super(viewHolder);

            holderUsername = (TextView) viewHolder.findViewById(R.id.list_item_post_username);
            holderArtwork = (ImageView) viewHolder.findViewById(R.id.list_item_post_artwork);
            holderTitle = (TextView) viewHolder.findViewById(R.id.list_item_post_title);
            holderArtist = (TextView) viewHolder.findViewById(R.id.list_item_post_artist);
        }
    }
}
