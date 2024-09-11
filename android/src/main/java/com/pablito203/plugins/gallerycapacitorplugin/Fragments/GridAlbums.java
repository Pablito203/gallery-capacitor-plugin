package com.pablito203.plugins.gallerycapacitorplugin.Fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

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
import android.widget.TextView;

import com.pablito203.plugins.gallerycapacitorplugin.Activities.GalleryActivity;
import com.pablito203.plugins.gallerycapacitorplugin.R;
import com.pablito203.plugins.gallerycapacitorplugin.Utils.Album;
import com.pablito203.plugins.gallerycapacitorplugin.Utils.Projections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GridAlbums extends Fragment {
    private GridView gridViewAlbums;
    private Cursor albumCursor;
    private int albumIDColumnIndex, albumNameColumnIndex, thumbnailIDColumnIndex, thumbnailRotateColumnIndex;
    private AlbumAdapter albumAdapter;
    private boolean shouldRequestThumb = true;
    private GalleryActivity galleryActivity;
    public Map<Integer, Album> lstAlbums = new HashMap<Integer, Album>();
    public ArrayList<Integer> lstAlbumsID = new ArrayList<Integer>();

    public GridAlbums() {
        // Required empty public constructor
    }

    public static GridAlbums newInstance() {
        GridAlbums fragment = new GridAlbums();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_grid_albums, container, false);
    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState) {
        this.setAlbums();
        this.setGridViewAlbums(view);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof GalleryActivity){
            galleryActivity = (GalleryActivity) context;
        }
    }

    private void setAlbums() {
        albumCursor = galleryActivity.getContentResolver().query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            Projections.ALBUMS,
            null,
            null,
            "DATE_MODIFIED DESC"
        );

        if (albumCursor == null) { return; }

        albumIDColumnIndex = albumCursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID);
        albumNameColumnIndex = albumCursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        thumbnailIDColumnIndex = albumCursor.getColumnIndex(MediaStore.Images.Media._ID);
        thumbnailRotateColumnIndex = albumCursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION);

        while (albumCursor.moveToNext()) {
            int albumID = albumCursor.getInt(albumIDColumnIndex);
            if (lstAlbums.containsKey(albumID)) {
                continue;
            }
            String albumName = albumCursor.getString(albumNameColumnIndex);
            int thumbnailID = albumCursor.getInt(thumbnailIDColumnIndex);
            int thumbnailRotate = albumCursor.getInt(thumbnailRotateColumnIndex);
            Album album = new Album(albumID, albumName, thumbnailID, thumbnailRotate);

            lstAlbums.put(albumID, album);
            lstAlbumsID.add(albumID);
        }
    }

    private void setGridViewAlbums(View view) {
        gridViewAlbums = (GridView) view.findViewById(R.id.gridview_grid_albums);
        this.setOnItemClickGridViewImages();
        this.setScrollListenerGridViewImages();

        albumAdapter = new AlbumAdapter();
        gridViewAlbums.setAdapter(albumAdapter);
    }

    private void setOnItemClickGridViewImages() {
        gridViewAlbums.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            var albumID = lstAlbumsID.get(position);
            if (albumID == null) {
                return;
            }

            Album album = lstAlbums.get(albumID);
            galleryActivity.onAlbumClick(albumID, album.AlbumName);
        });
    }

    private void setScrollListenerGridViewImages() {
        gridViewAlbums.setOnScrollListener(new AbsListView.OnScrollListener() {
            private int lastFirstItem = 0;
            private long timestamp = System.currentTimeMillis();

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    shouldRequestThumb = true;
                    albumAdapter.notifyDataSetChanged();
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

    private class AlbumGridView extends FrameLayout {
        private ImageView thumbnail;
        private TextView albumName;

        public AlbumGridView(Context context) {
            super(context);
            init(context);
        }

        @Override
        public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, widthMeasureSpec);
        }

        private void init(Context context) {
            LayoutInflater.from(context).inflate(R.layout.album_grid, this, true);

            thumbnail = (ImageView) findViewById(R.id.album_thumbnail);
            albumName = (TextView) findViewById(R.id.album_name);
        }
    }

    class AlbumAdapter extends BaseAdapter {
        public int getCount() {
            return lstAlbums.size();
        }

        public Object getItem(int position) {
            int albumID = lstAlbumsID.get(position);
            return lstAlbums.get(albumID);
        }

        public long getItemId(int position) {
            return lstAlbumsID.get(position);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                AlbumGridView temp = new AlbumGridView(galleryActivity);
                temp.thumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
                convertView = temp;
            }

            AlbumGridView albumGridView = (AlbumGridView) convertView;
            albumGridView.thumbnail.setImageBitmap(null);

            int albumID = lstAlbumsID.get(position);
            Album album = lstAlbums.get(albumID);

            assert album != null;
            albumGridView.albumName.setText(album.AlbumName);

            if (shouldRequestThumb) {
                galleryActivity.fetcher.fetch(album.ThumbnailID, albumGridView.thumbnail, galleryActivity.colWidth, album.ThumbnailRotate);
            }

            return albumGridView;
        }
    }
}