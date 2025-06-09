package com.pablito203.plugins.gallerycapacitorplugin.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.pablito203.plugins.gallerycapacitorplugin.Fragments.GridAlbums;
import com.pablito203.plugins.gallerycapacitorplugin.Fragments.GridImages;
import com.pablito203.plugins.gallerycapacitorplugin.R;
import com.pablito203.plugins.gallerycapacitorplugin.Utils.SelectedFile;
import com.pablito203.plugins.gallerycapacitorplugin.Utils.ImageFetcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GalleryActivity extends AppCompatActivity {
    private static final int ACTIVITY_PREVIEW = 1;

    private Fragment currentFragment;
    private Fragment imagesFragment;
    private Fragment albumsFragment;
    private FragmentManager fragmentManager = getSupportFragmentManager();
    private String fragmentTag = "";
    private enum FragmentTags {
        IMAGES { public String getValue() {return "IMAGES";} },
        ALBUMS { public String getValue() {return "ALBUMS";} };

        public String getValue() {
            return "";
        }
    }
    private AppCompatImageView closeButton;
    private TextView buttonImages;
    private TextView buttonAlbuns;
    private TextView countSelectedTextView;
    private TextView buttonPreview;
    private LinearLayout buttonApply;
    private AppCompatImageView backButtonAlbum;
    private TextView albumNameToolbar;
    private LinearLayout toolbarMain;
    private LinearLayout toolbarAlbum;

    public SparseBooleanArray checkStatus = new SparseBooleanArray();
    public final ImageFetcher fetcher = new ImageFetcher();
    public Map<Integer, SelectedFile> selectedFiles = new HashMap<Integer, SelectedFile>();
    public int colWidth;
    public int maximumFilesCount;
    public ArrayList<Integer> lstImageID = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        maximumFilesCount = getIntent().getIntExtra("maximumFilesCount", 15);

        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        colWidth = width / 4;

        setContentView(R.layout.gallery_activity);
        setupHeader();
        setViewVariables();
        setOnClickListener();
        addFragment(FragmentTags.IMAGES.getValue());
        setBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) { return; }

        if (requestCode == ACTIVITY_PREVIEW) {
            ArrayList<Integer> lstImageIDRemoved = data.getIntegerArrayListExtra("lstImageIDRemoved");

            if (lstImageIDRemoved.size() == 0) { return; }

            int countSelected = selectedFiles.size() - lstImageIDRemoved.size();
            countSelectedTextView.setText(String.format("(%d)", countSelected));

            for (int i = 0; i < lstImageIDRemoved.size(); i++) {
                int imageID = lstImageIDRemoved.get(i);
                SelectedFile selectedFile = selectedFiles.get(imageID);

                checkStatus.put(selectedFile.imageID, false);

                selectedFiles.remove(imageID);
                int indexImageID = lstImageID.indexOf(imageID);
                lstImageID.remove(indexImageID);
            }

            if (selectedFiles.size() == 0) {
                buttonPreview.setVisibility(View.GONE);
            }
        }
    }

    private void setupHeader() {
        getSupportActionBar().hide();
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.BLACK);

        if (maximumFilesCount == 1) {
            findViewById(R.id.bottom_toolbar).setVisibility(View.GONE);
        }
    }

    private void setOnClickListener() {
        buttonPreview.setOnClickListener(v -> onButtonPreviewClick());

        buttonApply.setOnClickListener(v -> applyClicked());

        closeButton.setOnClickListener(v -> closeClicked());

        buttonImages.setOnClickListener(v -> addFragment(FragmentTags.IMAGES.getValue()));

        buttonAlbuns.setOnClickListener(v -> addFragment(FragmentTags.ALBUMS.getValue()));

        backButtonAlbum.setOnClickListener(v -> onBackButtonAlbumClick());
    }

    private void onButtonPreviewClick() {
        ArrayList<SelectedFile> lstSelectedFiles = new ArrayList();

        for (int i = 0; i < selectedFiles.size(); i++) {
            lstSelectedFiles.add(selectedFiles.get(lstImageID.get(i)));
        }

        Intent intent = new Intent(GalleryActivity.this, PreviewActivity.class);
        intent.putParcelableArrayListExtra("selectedFiles", lstSelectedFiles);
        startActivityForResult(intent, ACTIVITY_PREVIEW);
    }

    private void addFragment(String tag) {
        if (Objects.equals(tag, fragmentTag)) { return; }

        Fragment newFragment;
        boolean addFragment = false;

        if (Objects.equals(tag, FragmentTags.IMAGES.getValue())) {
            buttonImages.setTextColor(Color.WHITE);
            buttonAlbuns.setTextColor(Color.DKGRAY);

            if (imagesFragment == null) {
                imagesFragment = new GridImages();
                addFragment = true;
            }
            newFragment = imagesFragment;
        } else {
            buttonAlbuns.setTextColor(Color.WHITE);
            buttonImages.setTextColor(Color.DKGRAY);

            if (albumsFragment == null) {
                albumsFragment = new GridAlbums();
                addFragment = true;
            }

            newFragment = albumsFragment;
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (currentFragment != null) {
            transaction.detach(currentFragment);
        }

        if (addFragment) {
            transaction.add(R.id.fragment_container, newFragment, tag);
        } else {
            transaction.attach(newFragment);
        }

        transaction.commit();
        currentFragment = newFragment;
        fragmentTag = tag;
    }

    private void onBackButtonAlbumClick() {
        fragmentManager.popBackStackImmediate();
        toolbarAlbum.setVisibility(View.INVISIBLE);
        toolbarMain.setVisibility(View.VISIBLE);
    }

    public void closeClicked() {
      setResult(RESULT_CANCELED);
      finish();
    }

    public void applyClicked() {
        Intent result = new Intent();
        ArrayList<Integer> lstImageID = new ArrayList<>(selectedFiles.keySet());
        result.putIntegerArrayListExtra("lstImageID", lstImageID);

        setResult(RESULT_OK, result);
        finish();
    }

    private void setCountSelectedImages() {
        countSelectedTextView.setText(String.format("(%d)", selectedFiles.size()));
    }

    private void showMaxFilesSelectedDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(String.format("Limite de %d arquivos", maximumFilesCount));
        dialogBuilder.setMessage(String.format("Você pode selecionar até %d arquivos", maximumFilesCount));
        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.parseColor("#E39730"));
    }

    private void setBackPressed() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (fragmentManager.getBackStackEntryCount() > 0) {
                    onBackButtonAlbumClick();
                    return;
                }

                closeClicked();
            }
        });
    }

    private void setViewVariables() {
        countSelectedTextView = (TextView) findViewById(R.id.count_selected);
        buttonPreview = (TextView) findViewById(R.id.button_preview);
        buttonApply = (LinearLayout) findViewById(R.id.button_apply);
        closeButton = (AppCompatImageView) findViewById(R.id.close_button);
        buttonImages = (TextView) findViewById(R.id.images_button);
        buttonAlbuns = (TextView) findViewById(R.id.albuns_button);
        backButtonAlbum = (AppCompatImageView) findViewById(R.id.back_button_album);
        albumNameToolbar = (TextView) findViewById(R.id.album_name_toolbar);
        toolbarMain = (LinearLayout) findViewById(R.id.toolbar_main);
        toolbarAlbum = (LinearLayout) findViewById(R.id.toolbar_album);
    }

    public void onItemClick(int imageID, boolean isChecked) {
        if (isChecked) {
            SelectedFile selectedFile = new SelectedFile(imageID);
            selectedFiles.put(imageID, selectedFile);
            lstImageID.add(imageID);

            if (maximumFilesCount == 1) {
                applyClicked();
                return;
            }

            buttonPreview.setVisibility(View.VISIBLE);
        } else {
            selectedFiles.remove(imageID);
            int index = lstImageID.indexOf(imageID);
            lstImageID.remove(index);

            if (selectedFiles.size() == 0) {
                buttonPreview.setVisibility(View.GONE);
            }
        }

        checkStatus.put(imageID, isChecked);

        setCountSelectedImages();
    }

    public boolean checkMaximumFiles(boolean isChecked) {
        if (isChecked && maximumFilesCount > 1 && selectedFiles.size() >= maximumFilesCount) {
            showMaxFilesSelectedDialog();
            return true;
        }

        return false;
    }

    public void onAlbumClick(int albumID, String albumName) {
        GridImages gridImagesAlbum = GridImages.newInstance(albumID);
        String tag = "album-" + albumID;
        albumNameToolbar.setText(albumName);
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, gridImagesAlbum, tag)
                .setReorderingAllowed(true)
                .addToBackStack(tag)
                .commit();

        toolbarMain.setVisibility(View.INVISIBLE);
        toolbarAlbum.setVisibility(View.VISIBLE);
    }

    public boolean isChecked(int imageID) {
        return checkStatus.get(imageID);
    }
}
