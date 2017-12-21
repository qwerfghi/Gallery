package com.qwerfghi.gallery;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ResultCodes;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "directory";
    private static final int RC_SIGN_IN = 1;
    private static final int MENU_UPLOAD = 10;
    private static final int MENU_DELETE = 20;
    private RecyclerView mRecyclerView;
    private InterstitialAd mInterstitialAd;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private FirebaseStorage mStorage = FirebaseStorage.getInstance();
    private boolean mShouldUseLocal = true;
    private GridImagesAdapter mGridImagesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        ApplicationContext application = (ApplicationContext) getApplication();
        application.setLocalPictures(getPictures());
        mDatabase = FirebaseDatabase.getInstance();

        mRecyclerView = findViewById(R.id.image_list_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mGridImagesAdapter = new GridImagesAdapter();
        mRecyclerView.setAdapter(mGridImagesAdapter);
        MobileAds.initialize(getApplicationContext(), getString(R.string.ad_mob_id));
        //  init banner
        AdView mAdView = findViewById(R.id.bottom_ad);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.ad_mob_interstitial));

        AdRequest interstitialAdRequest = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(interstitialAdRequest);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == ResultCodes.OK) {
                ApplicationContext application = (ApplicationContext) getApplication();
                FirebaseUser user = mAuth.getCurrentUser();
                application.setUser(user);
                loadStorageList(application, user);

                Toast.makeText(this, "good", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "bad", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadStorageList(ApplicationContext application, FirebaseUser user) {
        DatabaseReference myRef = mDatabase.getReference(user.getUid());
        StorageReference userStorage = mStorage.getReference(user.getUid());
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<StorageReference> storagePictures = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String result = snapshot.getValue(String.class);
                    storagePictures.add(userStorage.child(result));
                }
                application.setStoragePictures(storagePictures);
                mGridImagesAdapter.setStoragePictures(storagePictures);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gallery_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out:
                AuthUI.getInstance().signOut(this);
                return true;
            case R.id.change_source:
                mShouldUseLocal = !mShouldUseLocal;
                mRecyclerView.getAdapter().notifyDataSetChanged();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(0, MENU_UPLOAD, 0, "Upload");
        menu.add(0, MENU_DELETE, 0, "Delete");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_UPLOAD:
                /*if (mShouldUseLocal) {
                    item
                File file = mLocalPictures.get(item.);
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
                StorageReference storageReference = mStoragePictures.get(mCurrentPhotoIndex);
                if (storageReference.delete().isSuccessful()) {
                    Log.d("asd", "Успех");
                } else {
                    Log.d("asd", storageReference.toString());
                }
                mStoragePictures.remove(mCurrentPhotoIndex);
                if (mCurrentPhotoIndex == mStoragePictures.size()) {
                    mCurrentPhotoIndex = mCurrentPhotoIndex - 1;
                }
            }

            mPhotoPager.getAdapter().notifyDataSetChanged();
            mHorizontalImagesList.getAdapter().notifyDataSetChanged();*/
                break;
            case MENU_DELETE:
                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
        mAuth.addAuthStateListener(this::checkAuth);
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAuth.removeAuthStateListener(this::checkAuth);
    }

    private class GridImagesAdapter extends RecyclerView.Adapter<ImageViewHolder> {
        private List<File> localPictures;
        private List<StorageReference> storagePictures;

        private GridImagesAdapter() {
            this.localPictures = ((ApplicationContext) getApplication()).getLocalPictures();
        }

        @Override
        public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View inflate = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.square_item_image, null);
            return new ImageViewHolder(inflate);
        }

        @Override
        public void onBindViewHolder(ImageViewHolder holder, final int position) {
            if (mShouldUseLocal) {
                Glide.with(MainActivity.this)
                        .load(localPictures.get(position))
                        .into(holder.mImage);
            } else {
                Glide.with(MainActivity.this)
                        .using(new FirebaseImageLoader())
                        .load(storagePictures.get(position))
                        .into(holder.mImage);
            }

            registerForContextMenu(holder.mImage);
            holder.mImage.setOnClickListener(view -> {
                Intent intent = GalleryActivity.
                        newIntent(MainActivity.this, position, mShouldUseLocal);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            if (mShouldUseLocal) {
                return localPictures != null ? localPictures.size() : 0;
            } else {
                return storagePictures != null ? storagePictures.size() : 0;
            }
        }

        public void setStoragePictures(List<StorageReference> storagePictures) {
            this.storagePictures = storagePictures;
        }
    }

    private class ImageViewHolder extends RecyclerView.ViewHolder {
        private ImageView mImage;

        private ImageViewHolder(View itemView) {
            super(itemView);
            mImage = itemView.findViewById(R.id.square_item_image);
        }
    }

    private List<File> getPictures() {
        File photosDir = new File(getPhotosStorageDir().getPath());
        File picturesDir = new File(getPicturesStorageDir().getPath());
        List<File> result = new ArrayList<>();
        Queue<File> fileTree = new PriorityQueue<>();

        Collections.addAll(fileTree, photosDir.listFiles());
        Collections.addAll(fileTree, picturesDir.listFiles());

        while (!fileTree.isEmpty()) {
            File currentFile = fileTree.remove();
            if (currentFile.isDirectory()) {
                Collections.addAll(fileTree, currentFile.listFiles());
            } else {
                if (currentFile.getName().endsWith(".jpg") ||
                        currentFile.getName().endsWith(".png") ||
                        currentFile.getName().endsWith(".jpeg"))
                    result.add(currentFile);
            }
        }
        return result;
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public File getPhotosStorageDir() {
        // Get the directory for the user's public pictures directory.
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        if (!file.mkdirs()) {
            Log.e(LOG_TAG, "Directory not created");
        }
        return file;
    }

    public File getPicturesStorageDir() {
        // Get the directory for the user's public pictures directory.
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (!file.mkdirs()) {
            Log.e(LOG_TAG, "Directory not created");
        }
        return file;
    }

    private void checkAuth(FirebaseAuth firebaseAuth) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            AuthUI.IdpConfig googleAuthUI = new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build();
            List<AuthUI.IdpConfig> providers = Collections.singletonList(googleAuthUI);
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setIsSmartLockEnabled(false)
                            .setAvailableProviders(providers)
                            //.setTheme(R.style.LoginTheme)
                            //.setLogo(R.mipmap.logo)
                            .build(), RC_SIGN_IN);
        } else {
            ApplicationContext applicationContext = (ApplicationContext) getApplication();
            applicationContext.setUser(user);
            loadStorageList(applicationContext, user);
        }
    }
}