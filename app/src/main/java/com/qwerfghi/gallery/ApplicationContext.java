package com.qwerfghi.gallery;

import android.app.Application;

import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.util.List;

public class ApplicationContext extends Application {
    private List<File> mPhotoURLs;
    private FirebaseUser mUser;

    public List<File> getPhotoURLs() {
        return mPhotoURLs;
    }

    public void setPhotoURLs(List<File> photoURLs) {
        mPhotoURLs = photoURLs;
    }

    public FirebaseUser getUser() {
        return mUser;
    }

    public void setUser(FirebaseUser user) {
        mUser = user;
    }
}