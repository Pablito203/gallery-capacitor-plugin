package com.pablito203.plugins.gallerycapacitorplugin.Utils;

import android.provider.MediaStore;

public class Projections {
    public static final String[] IMAGES = {
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.ORIENTATION
    };

    public static final String[] ALBUMS = {
            MediaStore.Files.FileColumns._ID,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.MIME_TYPE,
            "datetaken"
    };

    public static final String[] ALBUMS_ANDROID_LOWER_TEN = {
            MediaStore.Files.FileColumns._ID,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.MIME_TYPE,
            "COUNT(*) AS count",
            "datetaken"
    };
}
