package com.qwerfghi.gallery;

import android.content.Context;
import android.content.Intent;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {
    private ViewPager mPhotoPager;
    private RecyclerView mHorizontalImagesList;
    private List<File> mLocalPictures;
    private List<StorageReference> mStoragePictures;
    private int mCurrentPhotoIndex;
    private FirebaseStorage mStorage = FirebaseStorage.getInstance();
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private FirebaseUser mUser;
    private Toolbar mToolbar;

    private boolean mShouldUseLocal;

    private static final String CURRENT_PHOTO_INDEX = "current_photo_index";
    private static final String SHOULD_USE_LOCAL = "should_use_local";

    public static Intent newIntent(Context packageContext, int photoIndex, boolean shouldUseLocal) {
        Intent intent = new Intent(packageContext, GalleryActivity.class);
        intent.putExtra(CURRENT_PHOTO_INDEX, photoIndex);
        intent.putExtra(SHOULD_USE_LOCAL, shouldUseLocal);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        mCurrentPhotoIndex = getIntent().getIntExtra(CURRENT_PHOTO_INDEX, 0);
        mShouldUseLocal = getIntent().getBooleanExtra(SHOULD_USE_LOCAL, true);
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mUser = ((ApplicationContext) getApplication()).getUser();
        mLocalPictures = ((ApplicationContext) getApplication()).getLocalPictures();
        mStoragePictures = ((ApplicationContext) getApplication()).getStoragePictures();
        mPhotoPager = findViewById(R.id.photo_pager);
        FragmentManager fragmentManager = getSupportFragmentManager();
        mPhotoPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                return PhotoFragment.newInstance(position, mShouldUseLocal);
            }

            @Override
            public int getCount() {
                if (mShouldUseLocal) {
                    return mLocalPictures.size();
                } else {
                    return mStoragePictures.size();
                }
            }

            @Override
            public int getItemPosition(@NonNull Object object) {
                return POSITION_NONE;
            }
        });
        mHorizontalImagesList = findViewById(R.id.images_horizontal_list);
        HorizontalListAdapter listAdapter = new HorizontalListAdapter();
        mHorizontalImagesList.setAdapter(listAdapter);
        mHorizontalImagesList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mPhotoPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mCurrentPhotoIndex = position;
                mHorizontalImagesList.smoothScrollToPosition(position);
                listAdapter.setSelectedItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        listAdapter.setSelectedItem(mCurrentPhotoIndex);
        mPhotoPager.setCurrentItem(mCurrentPhotoIndex);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.photo_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_photo:
                if (mShouldUseLocal) {
                    File file = mLocalPictures.get(mCurrentPhotoIndex);
                    if (file.delete()) {
                        Log.d("asd", "Успех");
                    } else {
                        Log.d("asd", "Ошибка");
                    }
                    mLocalPictures.remove(mCurrentPhotoIndex);
                    if (mCurrentPhotoIndex == mLocalPictures.size()) {
                        mCurrentPhotoIndex = mCurrentPhotoIndex - 1;
                    }
                } else {
                    StorageReference reference = mStoragePictures.get(mCurrentPhotoIndex);
                    String reference2 = reference.getName().split("\\.")[0];
                    mDatabase.getReference(mUser.getUid()).child(reference2).removeValue();
                    mStorage.getReferenceFromUrl(mStoragePictures.get(mCurrentPhotoIndex).toString()).delete();

                    mStoragePictures.remove(mCurrentPhotoIndex);
                    if (mCurrentPhotoIndex == mStoragePictures.size()) {
                        mCurrentPhotoIndex = mCurrentPhotoIndex - 1;
                    }
                }

                mPhotoPager.getAdapter().notifyDataSetChanged();
                mHorizontalImagesList.getAdapter().notifyDataSetChanged();
                return true;
            case R.id.upload_photo:
                File picture = mLocalPictures.get(mCurrentPhotoIndex);
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference(mUser.getUid());
                String key = picture.getName().split("\\.")[0];
                myRef.child(key).setValue(picture.getName());
                UploadTask putFile = mStorage.getReference(mUser.getUid())
                        .child(picture.getName())
                        .putFile(Uri.fromFile(picture));
                putFile.addOnSuccessListener(taskSnapshot -> {
                    Toast.makeText(this, "success", Toast.LENGTH_SHORT).show();
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public class HorizontalListAdapter extends RecyclerView.Adapter<HorizontalImageViewHolder> {
        int selectedItem = -1;

        @Override
        public HorizontalImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View inflate = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_image_horizontal, null);
            return new HorizontalImageViewHolder(inflate);
        }

        @Override
        public void onBindViewHolder(HorizontalImageViewHolder holder, int position) {
            if (mShouldUseLocal) {
                Glide.with(GalleryActivity.this)
                        .load(((ApplicationContext) getApplication()).getLocalPictures().get(position))
                        .into(holder.image);
            } else {
                Glide.with(GalleryActivity.this)
                        .using(new FirebaseImageLoader())
                        .load(((ApplicationContext) getApplication()).getStoragePictures().get(position))
                        .into(holder.image);
            }

            ColorMatrix matrix = new ColorMatrix();
            if (selectedItem != position) {
                matrix.setSaturation(0);

                ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
                holder.image.setColorFilter(filter);
                holder.image.setAlpha(0.5f);
            } else {
                matrix.setSaturation(1);

                ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
                holder.image.setColorFilter(filter);
                holder.image.setAlpha(1f);
            }


            holder.image.setOnClickListener(view -> {
                mCurrentPhotoIndex = position;
                mPhotoPager.setCurrentItem(position, true);
            });
        }

        @Override
        public int getItemCount() {
            if (mShouldUseLocal) {
                return ((ApplicationContext) getApplication()).getLocalPictures().size();
            } else {
                return ((ApplicationContext) getApplication()).getStoragePictures().size();
            }
        }

        public void setSelectedItem(int position) {
            selectedItem = position;
            notifyDataSetChanged();
        }
    }

    private class HorizontalImageViewHolder extends RecyclerView.ViewHolder {
        private ImageView image;

        private HorizontalImageViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image_horizontal);
        }
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }
}