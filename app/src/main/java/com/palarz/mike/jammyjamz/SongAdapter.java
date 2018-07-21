package com.palarz.mike.jammyjamz;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * An adapter for the ListView within PostSearchResultsPostSearchResults which displays the results of the
 * search request.
 */

public class SongAdapter extends ArrayAdapter<Track> {

    private static class ViewHolder {
        public ImageView cover;
        public TextView title;
        public TextView author;
    }

    public SongAdapter(Context context, ArrayList<Track> tracks) {
        super(context, 0, tracks);
    }

    @Override
    public View getView(int position, @Nullable View convertView, ViewGroup parent) {
        final Track currentTrack = getItem(position);
        ViewHolder viewHolder;
        // We'll first check if an existing view is being reused. If not, then we will inflate the view
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_book, parent, false);
            viewHolder.cover = (ImageView) convertView.findViewById(R.id.list_item_cover);
            viewHolder.title = (TextView) convertView.findViewById(R.id.list_item_title);
            viewHolder.author = (TextView) convertView.findViewById(R.id.list_item_author);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Finally, we populate the data into the list item using our Book object
        viewHolder.title.setText(currentTrack.getTitle());
        viewHolder.author.setText(currentTrack.getArtistNames());
        Picasso.get()
                .load(Uri.parse(currentTrack.getAlbum().getLargeAlbumCover()))
                .error(R.drawable.ic_no_cover)
                .into(viewHolder.cover);

        return convertView;

    }
}
