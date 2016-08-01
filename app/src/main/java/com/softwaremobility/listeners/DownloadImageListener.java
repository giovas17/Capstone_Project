package com.softwaremobility.listeners;


import android.support.annotation.Nullable;

public interface DownloadImageListener {
    void onSuccessfullyDownloadImage(@Nullable byte[] imageRecipe);
    void onSuccessfullyDownloadAllImages(@Nullable byte[] imageRecipe, @Nullable byte[] imageFlag);
}
