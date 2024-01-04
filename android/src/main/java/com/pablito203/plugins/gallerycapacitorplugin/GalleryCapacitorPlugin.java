package com.pablito203.plugins.gallerycapacitorplugin;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.annotation.Nullable;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Logger;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;
import java.util.ArrayList;
import java.util.List;


@CapacitorPlugin(name = "GalleryCapacitor")
public class GalleryCapacitorPlugin extends Plugin {
    public static final String ERROR_PICK_FILE_FAILED = "pickFiles failed.";
    public static final String ERROR_PICK_FILE_CANCELED = "pickFiles canceled.";
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
        List<JSObject> filesResultList = new ArrayList<>();
        if (data == null) {
            callResult.put("files", JSArray.from(filesResultList));
            return callResult;
        }
        List<Uri> uris = new ArrayList<>();
        if (data.getClipData() == null) {
            Uri uri = data.getData();
            uris.add(uri);
        } else {
            for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                Uri uri = data.getClipData().getItemAt(i).getUri();
                uris.add(uri);
            }
        }
        for (int i = 0; i < uris.size(); i++) {
            Uri uri = uris.get(i);
            if (uri == null) {
                continue;
            }
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
}
