package com.pablito203.plugins.gallerycapacitorplugin.Utils;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.OpenableColumns;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.getcapacitor.Bridge;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class GalleryCapacitor {

    private Bridge bridge;

    public GalleryCapacitor(Bridge bridge) {
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

    public Uri convertHeicToJpg(Uri heicUri) throws IOException {
      InputStream inputStream = null;
      inputStream = bridge.getContext().getContentResolver().openInputStream(heicUri);
      Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
      inputStream.close();

      if (bitmap == null) {
        throw new IOException("Erro ao decodificar imagem HEIC.");
      }

      int orientation = getExifOrientationFromUri(heicUri);
      bitmap = rotateBitmap(bitmap, orientation);

      String fileName = "converted_" + System.currentTimeMillis() + ".jpg";
      File outputDir = bridge.getContext().getCacheDir();
      File outputFile = new File(outputDir, fileName);

      FileOutputStream out = new FileOutputStream(outputFile);
      bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
      out.flush();
      out.close();

      return FileProvider.getUriForFile(
        bridge.getContext(),
        bridge.getContext().getPackageName() + ".fileprovider",
        outputFile
      );
    }

  private int getExifOrientationFromUri(Uri imageUri) throws IOException {
    InputStream inputStream = bridge.getContext().getContentResolver().openInputStream(imageUri);
    if (inputStream == null) {
      throw new IOException("Não foi possível abrir o InputStream da imagem.");
    }

    ExifInterface exif = new ExifInterface(inputStream);
    int orientation = exif.getAttributeInt(
      ExifInterface.TAG_ORIENTATION,
      ExifInterface.ORIENTATION_NORMAL
    );
    inputStream.close();

    return orientation;
  }

  private Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
    Matrix matrix = new Matrix();

    switch (orientation) {
      case ExifInterface.ORIENTATION_ROTATE_90:
        matrix.postRotate(90);
        break;
      case ExifInterface.ORIENTATION_ROTATE_180:
        matrix.postRotate(180);
        break;
      case ExifInterface.ORIENTATION_ROTATE_270:
        matrix.postRotate(270);
        break;
      case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
        matrix.preScale(-1.0f, 1.0f);
        break;
      case ExifInterface.ORIENTATION_FLIP_VERTICAL:
        matrix.preScale(1.0f, -1.0f);
        break;
      default:
        return bitmap;
    }

    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
  }
}
