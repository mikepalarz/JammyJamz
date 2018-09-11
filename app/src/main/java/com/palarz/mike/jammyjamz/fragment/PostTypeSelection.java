package com.palarz.mike.jammyjamz.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.palarz.mike.jammyjamz.R;

public class PostTypeSelection extends DialogFragment {

    public interface PostTypeSelectionListener {
        void onPositiveClick(int postType);
    }

    // Stores what type of post the user has selected to make
    private int mPostType;
    private PostTypeSelectionListener mListener;

    // Checking to make sure that the activity which hosts this fragment has implemented the
    // PostTypeSelectionListener interface
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mListener = (PostTypeSelectionListener) context;
        } catch(ClassCastException exception) {
            throw new ClassCastException(context.getPackageName() + " must implement PostTypeSelectionListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPostType = 0;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder
                .setTitle(getString(R.string.post_type_selection_title))
                .setSingleChoiceItems(R.array.post_types, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mPostType = which;
                    }
                })
                .setPositiveButton(getString(R.string.post_type_selection_button_positive), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // We'll launch the song search activity from this point
                        mListener.onPositiveClick(mPostType);
                    }
                })
                .setNegativeButton(getString(R.string.post_type_selection_button_negative), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        return builder.create();
    }
}
