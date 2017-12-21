package com.qwerfghi.gallery;

import android.app.Application;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.List;

public class ApplicationContext extends Application {
    private List<File> mLocalPictures;
    private List<StorageReference> mStoragePictures;
    private FirebaseUser mUser;

    public List<File> getLocalPictures() {
        return mLocalPictures;
    }

    public void setLocalPictures(List<File> photoURLs) {
        mLocalPictures = photoURLs;
    }

    public FirebaseUser getUser() {
        return mUser;
    }

    public void setUser(FirebaseUser user) {
        mUser = user;
    }

    public List<StorageReference> getStoragePictures() {
        return mStoragePictures;
    }

    public void setStoragePictures(List<StorageReference> mStoragePhotos) {
        this.mStoragePictures = mStoragePhotos;
    }
}