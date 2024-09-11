package com.pablito203.plugins.gallerycapacitorplugin.Fragments;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;

import com.pablito203.plugins.gallerycapacitorplugin.Activities.GalleryActivity;
import com.pablito203.plugins.gallerycapacitorplugin.R;
import com.pablito203.plugins.gallerycapacitorplugin.Views.RadioCheckView;
import com.pablito203.plugins.gallerycapacitorplugin.Utils.Projections;

public class GridImages extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private GridView gridViewImages;
    private Cursor imageCursor;
    private int imageColumnIndex, imageColumnOrientation;
    private ImageAdapter imageAdapter;
    private boolean shouldRequestThumb = true;
    private GalleryActivity galleryActivity;
    private int albumID = 0;


    public GridImages() {
        // Required empty public constructor
    }

    public static GridImages newInstance(int albumID) {
        GridImages fragment = new GridImages();

        Bundle args = new Bundle();
        args.putInt("albumID", albumID);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args == null) return;

        albumID = getArguments().getInt("albumID", 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_grid_images, container, false);
    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState) {
        this.setGridViewImages(view);
        LoaderManager.enableDebugLogging(false);
        LoaderManager.getInstance(this).initLoader(0, null, this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof GalleryActivity){
            galleryActivity = (GalleryActivity) context;
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        if (albumID != 0) {
            return CreateLoaderByAlbum();
        }

        return CreateLoaderAllImages();
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null) {
            return;
        }

        imageCursor = cursor;
        imageColumnIndex = imageCursor.getColumnIndex(MediaStore.Images.Media._ID);
        imageColumnOrientation = imageCursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION);
        imageAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        imageCursor = null;
    }

    private CursorLoader CreateLoaderAllImages() {
        return new CursorLoader(
                galleryActivity,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                Projections.IMAGES,
                null,
                null,
                "DATE_MODIFIED DESC"
        );
    }

    private CursorLoader CreateLoaderByAlbum() {
        return new CursorLoader(
                galleryActivity,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                Projections.IMAGES,
                MediaStore.Images.Media.BUCKET_ID + "=?",
                new String[] {Integer.toString(albumID)},
                "DATE_MODIFIED DESC"
        );
    }

    private void setGridViewImages(View view) {
        gridViewImages = (GridView) view.findViewById(R.id.gridview_grid_images);
        this.setOnItemClickGridViewImages();
        this.setScrollListenerGridViewImages();

        imageAdapter = new ImageAdapter();
        gridViewImages.setAdapter(imageAdapter);
    }

    private void setOnItemClickGridViewImages() {
        gridViewImages.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            String name = getImageName(position);
            if (name == null) {
                return;
            }

            int imageID = imageCursor.getInt(imageColumnIndex);
            ImageGridView imageGridView = (ImageGridView) view;
            boolean isChecked = !galleryActivity.isChecked(imageID);

            imageGridView.setChecked(isChecked);
            galleryActivity.onItemClick(imageID, isChecked);
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

    private String getImageName(int position) {
        imageCursor.moveToPosition(position);
        String name = null;

        try {
            name = imageCursor.getString(imageColumnIndex);
        } catch (Exception e) {
        }

        return name;
    }

    private class ImageGridView extends FrameLayout {
        private ImageView thumbnail;
        private RadioCheckView radioCheckView;

        public ImageGridView(Context context) {
            super(context);
            init(context);
        }

        public void setChecked(boolean isChecked) {
            if (isChecked) {
                thumbnail.setImageAlpha(128);
                thumbnail.setBackgroundColor(Color.BLACK);
                radioCheckView.setChecked(true);
                return;
            }

            thumbnail.setImageAlpha(255);
            thumbnail.setBackgroundColor(Color.TRANSPARENT);
            radioCheckView.setChecked(false);
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

    class ImageAdapter extends BaseAdapter {

        public int getCount() {
            if (imageCursor != null) {
                return imageCursor.getCount();
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

        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                ImageGridView temp = new ImageGridView(galleryActivity);
                temp.thumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
                convertView = temp;
            }

            ImageGridView imageGridView = (ImageGridView) convertView;
            imageGridView.thumbnail.setImageBitmap(null);

            if (!imageCursor.moveToPosition(position)) {
                return imageGridView;
            }

            if (imageColumnIndex == -1) {
                return imageGridView;
            }

            final int id = imageCursor.getInt(imageColumnIndex);
            final int rotate = imageCursor.getInt(imageColumnOrientation);

            if (galleryActivity.isChecked(id)) {
                imageGridView.thumbnail.setImageAlpha(128);
                imageGridView.thumbnail.setBackgroundColor(Color.BLACK);
                imageGridView.radioCheckView.setChecked(true);
            } else {
                imageGridView.thumbnail.setImageAlpha(255);
                imageGridView.thumbnail.setBackgroundColor(Color.TRANSPARENT);
                imageGridView.radioCheckView.setChecked(false);
            }

            if (shouldRequestThumb) {
                galleryActivity.fetcher.fetch(id, imageGridView.thumbnail, galleryActivity.colWidth, rotate);
            }

            return imageGridView;
        }
    }

}