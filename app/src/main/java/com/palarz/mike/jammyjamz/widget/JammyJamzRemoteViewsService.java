package com.palarz.mike.jammyjamz.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.target.AppWidgetTarget;
import com.bumptech.glide.request.transition.Transition;
import com.palarz.mike.jammyjamz.GlideApp;
import com.palarz.mike.jammyjamz.JammyJamzAppGlideModule;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.palarz.mike.jammyjamz.R;
import com.palarz.mike.jammyjamz.model.Post;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.palarz.mike.jammyjamz.GlideApp.with;

public class JammyJamzRemoteViewsService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new JammyJamzRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class JammyJamzRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory{

    private static final String TAG = JammyJamzRemoteViewsFactory.class.getSimpleName();

    private Context mContext;
    private int mAppWidgetID;
    private DatabaseReference mReference;
    private Query mQuery;
    private ValueEventListener mValueEventListener;
    private List<Post> mPosts;

    public JammyJamzRemoteViewsFactory(Context context, Intent intent){
        mContext = context;
        mAppWidgetID = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {

        mPosts = new ArrayList<>();
        mReference = FirebaseDatabase.getInstance().getReference().child("posts");

        mQuery = mReference.limitToLast(5);

        mValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                    Post currentPost = postSnapshot.getValue(Post.class);
                    mPosts.add(currentPost);
                }
                // Notifying the widget that new data is present
                AppWidgetManager.getInstance(mContext).notifyAppWidgetViewDataChanged(mAppWidgetID, R.id.app_widget_listview);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mQuery.addValueEventListener(mValueEventListener);

    }

    @Override
    public RemoteViews getViewAt(int position) {
        final RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.list_item_app_widget);
        final Post currentPost = mPosts.get(position);
        remoteViews.setTextViewText(R.id.list_item_app_widget_title, currentPost.getTitle());
        remoteViews.setTextViewText(R.id.list_item_app_widget_artist, currentPost.getArtists());

        // Huge thanks to this GitHub issue for figuring out how to load images into RemoteViews:
        // https://github.com/bumptech/glide/issues/1405

        // Loading the image into the RemoteView using Glide
        RequestBuilder<Bitmap> requestBuilder =
                GlideApp.with(mContext)
                        .asBitmap()
                        .load(currentPost.getPhotoUrl())
                        .centerCrop();

        FutureTarget<Bitmap> futureTarget = requestBuilder.submit();
        try {
            remoteViews.setImageViewBitmap(R.id.list_item_app_widget_artwork, futureTarget.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return remoteViews;
    }

    @Override
    public int getCount() {
        if (mPosts == null){
            return 0;
        } else {
            return mPosts.size();
        }
    }

    @Override
    public void onDataSetChanged() {
        // Nothing to do here since we are updating the widget within the ValueEventListener callback
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void onDestroy() {
        mQuery.removeEventListener(mValueEventListener);
        mPosts.clear();
        mQuery = null;
        mValueEventListener = null;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

}
