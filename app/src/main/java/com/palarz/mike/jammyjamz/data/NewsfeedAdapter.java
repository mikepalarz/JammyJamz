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
import com.palarz.mike.jammyjamz.model.Post;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

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

        private TextView holderUsername;
        private TextView holderMessage;
        private ImageView holderArtwork;
        private TextView holderTitle;
        private TextView holderArtist;
        private View holderArtworkBackground;
        private View holderUsernameBackground;

        public NewsfeedViewHolder(View viewHolder) {
            super(viewHolder);

            holderUsername = (TextView) viewHolder.findViewById(R.id.list_item_post_username);
            holderMessage = (TextView) viewHolder.findViewById(R.id.list_item_post_message);
            holderArtwork = (ImageView) viewHolder.findViewById(R.id.list_item_post_artwork);
            holderTitle = (TextView) viewHolder.findViewById(R.id.list_item_post_title);
            holderArtist = (TextView) viewHolder.findViewById(R.id.list_item_post_artist);
            holderArtworkBackground = viewHolder.findViewById(R.id.list_item_post_artwork_background);
            holderUsernameBackground = viewHolder.findViewById(R.id.list_item_post_username_background);
        }

        public void bind(Post currentPost){
            if (currentPost.getPhotoUrl() != null) {

                // We will attempt to download the image
                Picasso.get()
                        .load(currentPost.getPhotoUrl())
                        .placeholder(R.drawable.ic_artwork_placeholder)
                        .error(R.drawable.ic_error)
                        .into(holderArtwork, new Callback() {
                            @Override
                            public void onSuccess() {
                                Bitmap bitmap = ((BitmapDrawable) holderArtwork.getDrawable()).getBitmap();

                                Palette.from(bitmap)
                                        .generate(new Palette.PaletteAsyncListener() {
                                            @Override
                                            public void onGenerated(@NonNull Palette palette) {
                                                Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();

                                                if (vibrantSwatch != null){
                                                    holderArtworkBackground.setBackgroundColor(vibrantSwatch.getRgb());
                                                    holderTitle.setTextColor(vibrantSwatch.getTitleTextColor());
                                                    holderArtist.setTextColor(vibrantSwatch.getBodyTextColor());
                                                }

                                            }
                                        });
                            }

                            @Override
                            public void onError(Exception e) {

                            }
                        });
            }

            holderUsername.setText(currentPost.getUsername());
            holderTitle.setText(currentPost.getTitle());
            holderArtist.setText(currentPost.getArtists());
            holderMessage.setText(currentPost.getMessage());


        }

    }
}
