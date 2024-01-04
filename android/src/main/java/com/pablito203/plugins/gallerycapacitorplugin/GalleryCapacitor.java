package com.pablito203.plugins.gallerycapacitorplugin;

import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.getcapacitor.Bridge;
import com.getcapacitor.Logger;

public class GalleryCapacitor {

    private Bridge bridge;

    GalleryCapacitor(Bridge bridge) {
        this.bridge = bridge;
    }

    public String getPathFromUri(@NonNull Uri uri) {
        return uri.toString();
    }

    public String getNameFromUri(@NonNull Uri uri) {
        String displayName = "";
        String[] projection = { OpenableColumns.DISPLAY_NAME };
        Cursor cursor = bridge.getContext().getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIdx = cursor.getColumnIndex(projection[0]);
            displayName = cursor.getString(columnIdx);
            cursor.close();
        }
        if (displayName == null || displayName.length() < 1) {
            displayName = uri.getLastPathSegment();
        }
        return displayName;
    }

    @Nullable
    public String getMimeTypeFromUri(@NonNull Uri uri) {
        return bridge.getContext().getContentResolver().getType(uri);
    }

    public long getSizeFromUri(@NonNull Uri uri) {
        long size = 0;
        String[] projection = { OpenableColumns.SIZE };
        Cursor cursor = bridge.getContext().getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIdx = cursor.getColumnIndex(projection[0]);
            size = cursor.getLong(columnIdx);
            cursor.close();
        }
        return size;
    }
}
