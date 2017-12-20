package com.qwerfghi.gallery;

import android.content.Context;
import android.content.Intent;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {
    private ViewPager mPhotoPager;
    private RecyclerView mHorizontalImagesList;
    private List<File> mPictureFiles;
    private int mCurrentPhotoIndex;
    private FirebaseStorage mStorage = FirebaseStorage.getInstance();
    private FirebaseUser mUser;

    private static final String CURRENT_PHOTO_INDEX = "current_photo_index";

    public static Intent newIntent(Context packageContext, int photoIndex) {
        Intent intent = new Intent(packageContext, GalleryActivity.class);
        intent.putExtra(CURRENT_PHOTO_INDEX, photoIndex);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mUser = ((ApplicationContext) getApplication()).getUser();
        mPictureFiles = ((ApplicationContext) getApplication()).getPhotoURLs();
        mPhotoPager = findViewById(R.id.photo_pager);
        FragmentManager fragmentManager = getSupportFragmentManager();
        mPhotoPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                return PhotoFragment.newInstance(position);
            }

            @Override
            public int getCount() {
                return mPictureFiles.size();
            }

            @Override
            public int getItemPosition(@NonNull Object object) {
                return POSITION_NONE;
            }
        });
        mCurrentPhotoIndex = getIntent().getIntExtra(CURRENT_PHOTO_INDEX, 0);
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
                File file = mPictureFiles.get(mCurrentPhotoIndex);
                if (file.delete()) {
                    Log.d("asd", "Успех");
                } else {
                    Log.d("asd", "Ошибка");
                }
                mPictureFiles.remove(mCurrentPhotoIndex);
                if (mCurrentPhotoIndex == mPictureFiles.size()) {
                    mCurrentPhotoIndex = mCurrentPhotoIndex - 1;
                }
                mPhotoPager.getAdapter().notifyDataSetChanged();
                mHorizontalImagesList.getAdapter().notifyDataSetChanged();
                return true;
            case R.id.upload_photo:
                File picture = mPictureFiles.get(mCurrentPhotoIndex);
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference(mUser.getUid());
                myRef.push().setValue(picture.getName());
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
            Picasso.with(GalleryActivity.this)
                    .load(((ApplicationContext) getApplication()).getPhotoURLs().get(position))
                    .into(holder.image);
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
            return ((ApplicationContext) getApplication()).getPhotoURLs().size();
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
}