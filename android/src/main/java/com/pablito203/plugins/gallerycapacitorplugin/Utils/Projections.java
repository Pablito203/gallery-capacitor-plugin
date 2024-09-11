package com.pablito203.plugins.gallerycapacitorplugin.Utils;

import android.provider.MediaStore;

public class Projections {
    public static final String[] IMAGES = {
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.ORIENTATION
    };

    public static final String[] ALBUMS = {
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.ORIENTATION,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
    };
}
