package com.pablito203.plugins.gallerycapacitorplugin;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResult;
import androidx.annotation.Nullable;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;
import com.pablito203.plugins.gallerycapacitorplugin.Activities.GalleryActivity;
import com.pablito203.plugins.gallerycapacitorplugin.Utils.GalleryCapacitor;

import java.util.ArrayList;
import java.util.List;


@CapacitorPlugin(
    name = "GalleryCapacitor",
    permissions = {
            @Permission(strings = {Manifest.permission.READ_MEDIA_IMAGES}, alias = GalleryCapacitorPlugin.TIRAMISU_GALLERY_PERMISSION),
            @Permission(strings = {Manifest.permission.READ_EXTERNAL_STORAGE}, alias = GalleryCapacitorPlugin.GALLERY_PERMISSION)
    }
)
public class GalleryCapacitorPlugin extends Plugin {
    public static final String ERROR_PICK_FILE_FAILED = "pickFiles failed.";
    public static final String ERROR_PICK_FILE_CANCELED = "pickFiles canceled.";
    public static final String TIRAMISU_GALLERY_PERMISSION = "tiramisuGallery";
    public static final String GALLERY_PERMISSION = "gallery";
    public static final String PERMISSION_NAME = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) ? TIRAMISU_GALLERY_PERMISSION : GALLERY_PERMISSION;
    private GalleryCapacitor implementation;
    public void load() {
        implementation = new GalleryCapacitor(this.getBridge());
    }

    @PluginMethod
    public void pickFiles(PluginCall call) {
        try {
            int maximumFilesCount = call.getInt("maximumFilesCount", 15);

            Intent intent = new Intent(this.getActivity(), GalleryActivity.class);
            intent.putExtra("maximumFilesCount", maximumFilesCount);

            startActivityForResult(call, intent, "pickFilesResult");
        } catch (Exception ex) {
            String message = ex.getMessage();
            call.reject(message);
        }
    }

    @PluginMethod
    @PermissionCallback
    public void checkPermissions(PluginCall call) {
        if (isPermissionGranted()) {
            JSObject permissionsResultJSON = new JSObject();
            permissionsResultJSON.put(PERMISSION_NAME, "granted");
            call.resolve(permissionsResultJSON);
        } else {
            super.checkPermissions(call);
        }
    }

    @PluginMethod
    public void requestPermissions(PluginCall call) {
        if (isPermissionGranted()) {
            JSObject permissionsResultJSON = new JSObject();
            permissionsResultJSON.put(PERMISSION_NAME, "granted");
            call.resolve(permissionsResultJSON);
        } else {
            super.requestPermissionForAlias(PERMISSION_NAME, call, "checkPermissions");
        }
    }

    @ActivityCallback
    private void pickFilesResult(PluginCall call, ActivityResult result) {
        try {
            if (call == null) {
                return;
            }

            int resultCode = result.getResultCode();
            switch (resultCode) {
                case Activity.RESULT_OK:
                    JSObject callResult = createPickFilesResult(result.getData());
                    call.resolve(callResult);
                    break;
                case Activity.RESULT_CANCELED:
                    call.reject(ERROR_PICK_FILE_CANCELED);
                    break;
                default:
                    call.reject(ERROR_PICK_FILE_FAILED);
            }
        } catch (Exception ex) {
            String message = ex.getMessage();
            call.reject(message);
        }
    }

    private JSObject createPickFilesResult(@Nullable Intent data) {
        JSObject callResult = new JSObject();
        List<JSObject> filesResultList = new ArrayList();
        if (data == null) {
            callResult.put("files", JSArray.from(filesResultList));
            return callResult;
        }

        ArrayList<Integer> lstImageID = data.getIntegerArrayListExtra("lstImageID");

        for (int i = 0; i < lstImageID.size(); i++) {
            String stringr = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString() + String.format("/%d", lstImageID.get(i));

            Uri uri = Uri.parse(stringr);
            JSObject fileResult = new JSObject();

            fileResult.put("mimeType", implementation.getMimeTypeFromUri(uri));
            fileResult.put("name", implementation.getNameFromUri(uri));
            fileResult.put("path", implementation.getPathFromUri(uri));
            fileResult.put("size", implementation.getSizeFromUri(uri));
            filesResultList.add(fileResult);
        }
        callResult.put("files", JSArray.from(filesResultList.toArray()));
        return callResult;
    }

    private boolean isPermissionGranted() {
        return getPermissionState(PERMISSION_NAME) == PermissionState.GRANTED;
    }
}
