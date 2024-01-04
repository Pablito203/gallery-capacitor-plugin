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
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GalleryActivity extends AppCompatActivity implements OnItemClickListener,
        LoaderManager.LoaderCallbacks<Cursor>,
        View.OnClickListener {
    private ImageAdapter ia;
    private Cursor imagecursor;
    private int image_column_index, image_column_orientation;
    private ArrayList<Integer> lstImageIDSelected = new ArrayList();
    private ArrayList<Integer> lstImageRotateSelected = new ArrayList();
    private int colWidth;
    private static final int CURSORLOADER_THUMBS = 0;
    private final ImageFetcher fetcher = new ImageFetcher();
    private boolean shouldRequestThumb = true;

    private Map<String, Integer> fileNames = new HashMap<String, Integer>();

    private SparseBooleanArray checkStatus = new SparseBooleanArray();
    private TextView selectedTextView;
    private TextView buttonPreview;
    private LinearLayout buttonApply;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.grid);
        setupHeader();

        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();

        colWidth = width / 4;

        selectedTextView = (TextView) findViewById(R.id.count_selected);
        buttonPreview = (TextView) findViewById(R.id.button_preview);
        buttonPreview.setOnClickListener(this);
        buttonApply = (LinearLayout) findViewById(R.id.button_apply);
        buttonApply.setOnClickListener(this);

        GridView gridView = (GridView) findViewById(R.id.gridview);
        gridView.setOnItemClickListener(this);
        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            private int lastFirstItem = 0;
            private long timestamp = System.currentTimeMillis();

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    shouldRequestThumb = true;
                    ia.notifyDataSetChanged();
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

        ia = new ImageAdapter();
        gridView.setAdapter(ia);

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


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String name = getImageName(position);
        int rotation = getImageRotation(position);
        int idCursor = imagecursor.getInt(image_column_index);

        if (name == null) {
            return;
        }

        boolean isChecked = !isChecked(position);

        int maximumFilesCount = 5;

        if (isChecked && fileNames.size() >= maximumFilesCount) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setTitle(String.format("Limite de %d arquivos", maximumFilesCount));
            dialogBuilder.setMessage(String.format("Você pode selecionar até %d arquivos", maximumFilesCount));
            dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            dialogBuilder.create();
            dialogBuilder.show();
            return;
        } else if (isChecked) {
            fileNames.put(name, rotation);
            lstImageIDSelected.add(idCursor);
            lstImageRotateSelected.add(rotation);

            if (3 == 1) {
                //selectClicked();

            } else {
                ImageGridView imageGridView = (ImageGridView) view;

                imageGridView.thumbnail.setImageAlpha(128);
                imageGridView.thumbnail.setBackgroundColor(Color.BLACK);
                imageGridView.radioCheckView.setChecked(true);
            }
        } else {
            fileNames.remove(name);
            int index = lstImageIDSelected.indexOf(idCursor);
            lstImageIDSelected.remove(index);
            lstImageRotateSelected.remove(index);

            ImageGridView imageGridView = (ImageGridView) view;

            imageGridView.thumbnail.setImageAlpha(255);
            imageGridView.thumbnail.setBackgroundColor(Color.TRANSPARENT);
            imageGridView.radioCheckView.setChecked(false);
        }

        checkStatus.put(position, isChecked);

        selectedTextView.setText(String.format("(%d)", fileNames.size()));
        //updateAcceptButton();
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        ArrayList<String> img = new ArrayList<String>();
        switch (id) {
            case CURSORLOADER_THUMBS:
                img.add(MediaStore.Images.Media._ID);
                img.add(MediaStore.Images.Media.ORIENTATION);
                break;
        }

        return new CursorLoader(
                this,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                img.toArray(new String[img.size()]),
                null,
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
                ia.notifyDataSetChanged();
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

    public boolean isChecked(int position) {
        return checkStatus.get(position);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_preview) {
            Intent intent = new Intent(this, PreviewActivity.class);
            intent.putIntegerArrayListExtra("lstImageIDSelected", lstImageIDSelected);
            intent.putIntegerArrayListExtra("lstImageRotateSelected", lstImageRotateSelected);
            startActivityForResult(intent, 23);
        } else if (v.getId() == R.id.button_apply) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setTitle("Botão avançar");
            dialogBuilder.setMessage("Botão avançar pressionado");
            dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            dialogBuilder.create();
            dialogBuilder.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
            return;

        if (requestCode == 23) {
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

            if (isChecked(position)) {
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
