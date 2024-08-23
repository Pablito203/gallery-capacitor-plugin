package com.pablito203.plugins.gallerycapacitorplugin;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.SparseBooleanArray;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.pablito203.plugins.gallerycapacitorplugin.Utils.Projections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GalleryActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int CURSORLOADER_THUMBS = 0;
    private static final int CURSORLOADER_ALBUNS = 1;
    private static final int CURSORLOADER_THUMBS_ALBUM = 2;

    private static final int ACTIVITY_PREVIEW = 1;


    private ImageAdapter imageAdapter;
    private Cursor imagecursor;
    private int image_column_index, image_column_orientation;
    private int colWidth;
    private final ImageFetcher fetcher = new ImageFetcher();
    private boolean shouldRequestThumb = true;
    private Map<Integer, SelectedFile> selectedFiles = new HashMap<Integer, SelectedFile>();
    private ArrayList<Integer> lstImageID = new ArrayList();

    private SparseBooleanArray checkStatus = new SparseBooleanArray();

    private TextView buttonImages;
    private TextView buttonAlbuns;
    private TextView countSelectedTextView;
    private TextView buttonPreview;
    private LinearLayout buttonApply;

    private GridView gridViewImages;
    int maximumFilesCount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        maximumFilesCount = getIntent().getIntExtra("maximumFilesCount", 15);

        setContentView(R.layout.gallery_activity);
        setupHeader();

        if (maximumFilesCount == 1) {
            findViewById(R.id.bottom_toolbar).setVisibility(View.GONE);
        }

        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();

        colWidth = width / 4;

        countSelectedTextView = (TextView) findViewById(R.id.count_selected);
        buttonPreview = (TextView) findViewById(R.id.button_preview);
        buttonApply = (LinearLayout) findViewById(R.id.button_apply);
        buttonImages = (TextView) findViewById(R.id.images_button);
        buttonAlbuns = (TextView) findViewById(R.id.albuns_button);
        this.setOnClickListener();

        this.setGridViewImages();

        LoaderManager.enableDebugLogging(false);
        LoaderManager.getInstance(this).initLoader(CURSORLOADER_THUMBS, null, this);
    }

    private void setupHeader() {
        getSupportActionBar().hide();
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.BLACK);
    }

    private void setOnClickListener() {
        buttonPreview.setOnClickListener(v -> {
            ArrayList<SelectedFile> lstSelectedFiles = new ArrayList();

            for (int i = 0; i < selectedFiles.size(); i++) {
                lstSelectedFiles.add(selectedFiles.get(lstImageID.get(i)));
            }

            Intent intent = new Intent(GalleryActivity.this, PreviewActivity.class);
            intent.putParcelableArrayListExtra("selectedFiles", lstSelectedFiles);
            startActivityForResult(intent, ACTIVITY_PREVIEW);
        });

        buttonApply.setOnClickListener(v -> applyClicked());

        buttonImages.setOnClickListener(v -> {
            buttonImages.setTextColor(Color.WHITE);
            buttonAlbuns.setTextColor(Color.DKGRAY);
            gridViewImages.setVisibility(View.VISIBLE);
        });

        buttonAlbuns.setOnClickListener(v -> {
            buttonAlbuns.setTextColor(Color.WHITE);
            buttonImages.setTextColor(Color.DKGRAY);
            gridViewImages.setVisibility(View.INVISIBLE);
        });
    }

    private void setGridViewImages() {
        gridViewImages = (GridView) findViewById(R.id.gridview_images);
        this.setOnItemClickGridViewImages();
        this.setScrollListenerGridViewImages();

        imageAdapter = new ImageAdapter();
        gridViewImages.setAdapter(imageAdapter);
    }

    private void setOnItemClickGridViewImages() {
        gridViewImages.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            String name = getImageName(position);
            int rotation = getImageRotation(position);
            int idCursor = imagecursor.getInt(image_column_index);

            if (name == null) {
                return;
            }

            boolean isChecked = !isChecked(idCursor);

            if (isChecked && maximumFilesCount > 1 && selectedFiles.size() >= maximumFilesCount) {
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
                return;
            } else if (isChecked) {
                SelectedFile selectedFile = new SelectedFile(idCursor, rotation, position, name);
                selectedFiles.put(idCursor, selectedFile);
                lstImageID.add(idCursor);

                if (maximumFilesCount == 1) {
                    applyClicked();
                    return;
                } else {
                    ImageGridView imageGridView = (ImageGridView) view;

                    imageGridView.thumbnail.setImageAlpha(128);
                    imageGridView.thumbnail.setBackgroundColor(Color.BLACK);
                    imageGridView.radioCheckView.setChecked(true);

                    buttonPreview.setVisibility(View.VISIBLE);
                }
            } else {
                selectedFiles.remove(idCursor);
                int index = lstImageID.indexOf(idCursor);
                lstImageID.remove(index);


                ImageGridView imageGridView = (ImageGridView) view;

                imageGridView.thumbnail.setImageAlpha(255);
                imageGridView.thumbnail.setBackgroundColor(Color.TRANSPARENT);
                imageGridView.radioCheckView.setChecked(false);

                if (selectedFiles.size() == 0) {
                    buttonPreview.setVisibility(View.GONE);
                }
            }

            checkStatus.put(idCursor, isChecked);

            countSelectedTextView.setText(String.format("(%d)", selectedFiles.size()));
        });
    }

    private void setScrollListenerGridViewImages() {
        gridViewImages.setOnScrollListener(new AbsListView.OnScrollListener() {
            private int lastFirstItem = 0;
            private long timestamp = System.currentTimeMillis();

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    shouldRequestThumb = true;
                    imageAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                float dt = System.currentTimeMillis() - timestamp;
                if (firstVisibleItem != lastFirstItem) {
                    double speed = 1 / dt * 1000;
                    lastFirstItem = firstVisibleItem;
                    timestamp = System.currentTimeMillis();

                    // Limit if we go faster than a page a second
                    shouldRequestThumb = speed < visibleItemCount;
                }
            }
        });
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] projection;
        ArrayList<String> lstProjection = new ArrayList<String>();
        String selection = "";

        switch (id) {
            case CURSORLOADER_THUMBS:
                projection = Projections.IMAGES;
                break;
            case CURSORLOADER_ALBUNS:
                projection = Projections.ALBUMS;
                lstProjection.add(MediaStore.Images.Media._ID);
                lstProjection.add(MediaStore.Images.Media.ORIENTATION);
                break;
            case CURSORLOADER_THUMBS_ALBUM:
                projection = Projections.IMAGES;
                selection = MediaStore.Images.Media.BUCKET_ID + "='" + -1829889111 + "'";
                break;
            default:
                projection = Projections.IMAGES;
        }

        return new CursorLoader(
                this,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                "DATE_MODIFIED DESC"
        );
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null) {
            // NULL cursor. This usually means there's no image database yet....
            return;
        }

        switch (loader.getId()) {
            case CURSORLOADER_THUMBS:
                imagecursor = cursor;
                image_column_index = imagecursor.getColumnIndex(MediaStore.Images.Media._ID);
                image_column_orientation = imagecursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION);
                imageAdapter.notifyDataSetChanged();
                break;
            case CURSORLOADER_THUMBS_ALBUM:
                imagecursor = cursor;
                image_column_index = imagecursor.getColumnIndex(MediaStore.Images.Media._ID);
                image_column_orientation = imagecursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION);
                imageAdapter.notifyDataSetChanged();
                break;
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        switch (loader.getId()) {
            case CURSORLOADER_THUMBS:
                imagecursor = null;
                break;
        }
    }


    private String getImageName(int position) {
        imagecursor.moveToPosition(position);
        String name = null;

        try {
            name = imagecursor.getString(image_column_index);
        } catch (Exception e) {
        }

        return name;
    }

    private int getImageRotation(int position) {
        imagecursor.moveToPosition(position);
        int rotation = 0;

        try {
            rotation = imagecursor.getInt(image_column_orientation);
        } catch (Exception e) {
        }

        return rotation;
    }

    public boolean isChecked(int imageID) {
        return checkStatus.get(imageID);
    }

    private void applyClicked() {
        Intent result = new Intent();
        ArrayList<Integer> lstImageID = new ArrayList<>(selectedFiles.keySet());
        result.putIntegerArrayListExtra("lstImageID", lstImageID);

        setResult(RESULT_OK, result);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }

        if (requestCode == ACTIVITY_PREVIEW) {
            ArrayList<Integer> lstImageIDRemoved = data.getIntegerArrayListExtra("lstImageIDRemoved");

            if (lstImageIDRemoved.size() == 0) {
                return;
            }

            int countSelected = selectedFiles.size() - lstImageIDRemoved.size();
            countSelectedTextView.setText(String.format("(%d)", countSelected));

            for (int i = 0; i < lstImageIDRemoved.size(); i++) {
                int imageID = lstImageIDRemoved.get(i);
                SelectedFile selectedFile = selectedFiles.get(imageID);

                shouldRequestThumb = false;
                ImageGridView imageGridView = (ImageGridView) imageAdapter.getView(selectedFile.gridPosition, null, null);
                shouldRequestThumb = true;

                imageGridView.thumbnail.setImageAlpha(255);
                imageGridView.thumbnail.setBackgroundColor(Color.TRANSPARENT);
                imageGridView.radioCheckView.setChecked(false);

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

    private class ImageGridView extends FrameLayout {
        ImageView thumbnail;
        RadioCheckView radioCheckView;

        public ImageGridView(Context context) {
            super(context);
            init(context);
        }

        @Override
        public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, widthMeasureSpec);
        }

        private void init(Context context) {
            LayoutInflater.from(context).inflate(R.layout.image_grid, this, true);

            thumbnail = (ImageView) findViewById(R.id.media_thumbnail);
            radioCheckView = (RadioCheckView) findViewById(R.id.check_view);
        }
    }

    private class ImageAdapter extends BaseAdapter {

        public int getCount() {
            if (imagecursor != null) {
                return imagecursor.getCount();
            } else {
                return 0;
            }
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {

            return position;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                ImageGridView temp = new ImageGridView(GalleryActivity.this);
                temp.thumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
                convertView = temp;
            }

            ImageGridView imageGridView = (ImageGridView) convertView;
            imageGridView.thumbnail.setImageBitmap(null);

            if (!imagecursor.moveToPosition(position)) {
                return imageGridView;
            }

            if (image_column_index == -1) {
                return imageGridView;
            }

            final int id = imagecursor.getInt(image_column_index);
            final int rotate = imagecursor.getInt(image_column_orientation);

            if (isChecked(id)) {
                imageGridView.thumbnail.setImageAlpha(128);
                imageGridView.setBackgroundColor(Color.BLACK);
                imageGridView.radioCheckView.setChecked(true);
            } else {
                imageGridView.thumbnail.setImageAlpha(255);
                imageGridView.thumbnail.setBackgroundColor(Color.TRANSPARENT);
                imageGridView.radioCheckView.setChecked(false);
            }

            if (shouldRequestThumb) {
                fetcher.fetch(id, imageGridView.thumbnail, colWidth, rotate);
            }

            return imageGridView;
        }
    }
}
