package com.qwerfghi.gallery;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.firebase.storage.StorageReference;
import com.transitionseverywhere.Slide;
import com.transitionseverywhere.TransitionManager;

import java.io.File;


public class PhotoFragment extends Fragment {
    private static final String IMAGE_POSITION = "image_position";
    private static final String SHOULD_USE_LOCAL = "should_use_local";

    private int mPosition;
    private boolean mShouldUseLocal;
    private boolean mIsShowing;
    private PhotoView mImage;
    private Toolbar mToolBar;
    private ProgressBar mProgressBar;
    private RecyclerView mHorizontalImagesList;

    public static PhotoFragment newInstance(int position, boolean shouldUseLocal) {
        PhotoFragment fragment = new PhotoFragment();
        Bundle args = new Bundle();
        args.putInt(IMAGE_POSITION, position);
        args.putBoolean(SHOULD_USE_LOCAL, shouldUseLocal);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPosition = getArguments().getInt(IMAGE_POSITION, 0);
            mShouldUseLocal = getArguments().getBoolean(SHOULD_USE_LOCAL, true);
        }
        mIsShowing = true;
        mToolBar = ((GalleryActivity) getActivity()).getToolbar();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_photo, container, false);
        mImage = inflate.findViewById(R.id.gallery_image);
        mProgressBar = inflate.findViewById(R.id.progress_bar);
        mHorizontalImagesList = getActivity().findViewById(R.id.images_horizontal_list);
        ApplicationContext context = (ApplicationContext) getActivity().getApplication();

        if (mShouldUseLocal) {
            Glide.with(getActivity())
                    .load(context.getLocalPictures().get(mPosition))
                    .listener(new RequestListener<File, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, File model, Target<GlideDrawable> target, boolean isFirstResource) {
                            mProgressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, File model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            mProgressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(mImage);
        } else {
            Glide.with(getActivity())
                    .using(new FirebaseImageLoader())
                    .load(context.getStoragePictures().get(mPosition))
                    .listener(new RequestListener<StorageReference, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, StorageReference model, Target<GlideDrawable> target, boolean isFirstResource) {
                            mProgressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, StorageReference model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            mProgressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(mImage);
        }


        mImage.setOnClickListener(view -> {
            if (mIsShowing) {
                mIsShowing = false;

                TransitionManager.beginDelayedTransition(mToolBar, new Slide(Gravity.TOP));
                mToolBar.setVisibility(View.GONE);

                TransitionManager.beginDelayedTransition(mHorizontalImagesList, new Slide(Gravity.BOTTOM));
                mHorizontalImagesList.setVisibility(View.GONE);

            } else {
                mIsShowing = true;
                TransitionManager.beginDelayedTransition(mToolBar, new Slide(Gravity.TOP));
                mToolBar.setVisibility(View.VISIBLE);

                TransitionManager.beginDelayedTransition(mHorizontalImagesList, new Slide(Gravity.BOTTOM));
                mHorizontalImagesList.setVisibility(View.VISIBLE);
            }
        });
        return inflate;
    }
}