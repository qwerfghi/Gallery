package com.qwerfghi.gallery;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ResultCodes;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "directory";
    private static final int RC_SIGN_IN = 1;
    private RecyclerView mRecyclerView;
    private InterstitialAd mInterstitialAd;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firebaseAuth = FirebaseAuth.getInstance();
        ApplicationContext application = (ApplicationContext) getApplication();
        application.setPhotoURLs(getPictures());
        mRecyclerView = findViewById(R.id.image_list_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mRecyclerView.setAdapter(new GridImagesAdapter(application.getPhotoURLs()));
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
                ((ApplicationContext) getApplication()).setUser(FirebaseAuth.getInstance().getCurrentUser());
                Toast.makeText(this, "good", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "bad", Toast.LENGTH_SHORT).show();
            }
        }
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
        firebaseAuth.addAuthStateListener(this::checkAuth);
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        super.onPause();
        firebaseAuth.removeAuthStateListener(this::checkAuth);
    }

    private class GridImagesAdapter extends RecyclerView.Adapter<ImageViewHolder> {
        private List<File> imageURLs;

        private GridImagesAdapter(List<File> imageURLs) {
            this.imageURLs = imageURLs;
        }

        @Override
        public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View inflate = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.square_item_image, null);
            return new ImageViewHolder(inflate);
        }

        @Override
        public void onBindViewHolder(ImageViewHolder holder, final int position) {
            Picasso.with(MainActivity.this)
                    .load(imageURLs.get(position))
                    .placeholder(R.drawable.placeholder)
                    .into(holder.mImage);

            holder.mImage.setOnClickListener(view -> {
                Intent intent = GalleryActivity.
                        newIntent(MainActivity.this, position);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return imageURLs != null ? imageURLs.size() : 0;
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
        }
    }
}