package com.qwerfghi.gallery;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class PhotoFragment extends Fragment {
    private static final String IMAGE_POSITION = "image_position";

    private int mPosition;
    private boolean mIsShowing;
    private ImageView mImage;
    private ActionBar mToolBar;
    private RecyclerView mHorizontalImagesList;

    public PhotoFragment() {
        // Required empty public constructor
    }

    public static PhotoFragment newInstance(int position) {
        PhotoFragment fragment = new PhotoFragment();
        Bundle args = new Bundle();
        args.putInt(IMAGE_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPosition = getArguments().getInt(IMAGE_POSITION, 0);
        }
        mIsShowing = true;
        mToolBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_photo, container, false);
        mImage = inflate.findViewById(R.id.gallery_image);
        mHorizontalImagesList = getActivity().findViewById(R.id.images_horizontal_list);
        ApplicationContext context = (ApplicationContext) getActivity().getApplication();
        Picasso.with(getActivity())
                .load(context.getPhotoURLs().get(mPosition))
                .into(mImage);
        mImage.setOnClickListener(view -> {
            if (mIsShowing) {
                mIsShowing = false;
                mToolBar.hide();
                //mToolBar.animate().translationY(-toolbar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
                mHorizontalImagesList.animate()
                        .translationY(mHorizontalImagesList.getBottom())
                        .setInterpolator(new AccelerateInterpolator())
                        .start();
            } else {
                mIsShowing = true;
                mToolBar.show();
                //toolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).start();
                mHorizontalImagesList.animate()
                        .translationY(0)
                        .setInterpolator(new DecelerateInterpolator())
                        .start();
            }
        });
        return inflate;
    }
}