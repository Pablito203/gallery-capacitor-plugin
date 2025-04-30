package com.pablito203.plugins.gallerycapacitorplugin.Activities;

import static android.provider.MediaStore.Images;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.pablito203.plugins.gallerycapacitorplugin.R;
import com.pablito203.plugins.gallerycapacitorplugin.Views.RadioCheckView;
import com.pablito203.plugins.gallerycapacitorplugin.Utils.SelectedFile;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;

public class PreviewActivity extends AppCompatActivity {
    private ArrayList<SelectedFile> selectedFiles = new ArrayList<>();
    private ArrayList<Integer> lstImageIDRemoved = new ArrayList<>();
    private ViewPager2 viewPager;
    ViewPageAdapter pagerAdapter;

    private RadioCheckView radioCheckView;
    private AppCompatImageView thumbnail;
    private FrameLayout topToolbar;
    private AppCompatImageView backButton;

    private TextView countSelectedTextView;
    private LinearLayout buttonApply;
    private FrameLayout bottomToolbar;
    private boolean toolbarVisible = true;

    ArrayList<ViewPagerItem> viewPagerItemArrayList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = this.getIntent();
        selectedFiles = intent.getParcelableArrayListExtra("selectedFiles");

        setContentView(R.layout.preview_activity);
        setupHeader();
        setViewVariables();
        setViewPager();
        setOnClickListener();
    }

    private void setupHeader() {
        Window window = this.getWindow();
        int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        window.getDecorView().setSystemUiVisibility(flags);

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.BLACK);
    }

    private void setViewVariables() {
        viewPager = findViewById(R.id.pager);
        buttonApply = findViewById(R.id.button_apply_preview);
        bottomToolbar = findViewById(R.id.bottom_toolbar_preview);
        topToolbar = findViewById(R.id.top_toolbar_preview);
        radioCheckView = findViewById(R.id.check_view_preview);
        thumbnail = findViewById(R.id.media_thumbnail_preview);
        topToolbar = findViewById(R.id.top_toolbar_preview);
        backButton = findViewById(R.id.back_button_preview);
        countSelectedTextView = findViewById(R.id.count_selected_preview);
        countSelectedTextView.setText(String.format("(%d)", selectedFiles.size()));
    }

    private void setViewPager() {
        ArrayList<ViewPagerItem> viewPagerItemArrayList = new ArrayList<>();

        for (int i = 0; i < selectedFiles.size(); i++) {
            SelectedFile selectedFile = selectedFiles.get(i);
            ViewPagerItem viewPagerItem = new ViewPagerItem(selectedFile.imageID);
            viewPagerItemArrayList.add(viewPagerItem);
        }

        viewPager.setOffscreenPageLimit(15);
        pagerAdapter = new ViewPageAdapter(viewPagerItemArrayList);
        viewPager.setAdapter(pagerAdapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                ViewPagerItem viewPagerItem = viewPagerItemArrayList.get(position);
                radioCheckView.setChecked(viewPagerItem.checked);
            }
        });

    }

    private void setOnClickListener() {
        buttonApply.setOnClickListener(v -> onApplyClick());
        backButton.setOnClickListener(v -> onBackClick());
        radioCheckView.setOnClickListener(v -> onCheckViewClick());
    }

    private void onCheckViewClick() {
        int currentItemPosition = viewPager.getCurrentItem();
        ViewPagerItem viewPagerItem = viewPagerItemArrayList.get(currentItemPosition);
        viewPagerItem.checked = !viewPagerItem.checked;

        radioCheckView.setChecked(viewPagerItem.checked);

        if (viewPagerItem.checked) {
            int index = lstImageIDRemoved.indexOf(viewPagerItem.imageID);
            lstImageIDRemoved.remove(index);
        } else {
            lstImageIDRemoved.add(viewPagerItem.imageID);
        }

        int countSelected = selectedFiles.size() - lstImageIDRemoved.size();
        countSelectedTextView.setText(String.format("(%d)", countSelected));
    }

    private void onBackClick() {
        setResult(RESULT_CANCELED);
        finish();
    }

    private void onApplyClick() {
        Intent result = new Intent();
        result.putIntegerArrayListExtra("lstImageIDRemoved", lstImageIDRemoved);
        setResult(RESULT_OK, result);
        finish();
    }

    private class ViewPageAdapter extends RecyclerView.Adapter<ViewPageAdapter.ViewHolder> {
        ArrayList<ViewHolder> lstViewHolder = new ArrayList<>();

        public ViewPageAdapter(ArrayList<ViewPagerItem> viewPagerItemList) {
            viewPagerItemArrayList = viewPagerItemList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_preview, parent, false);
            ViewHolder holder = new ViewHolder(view);
            lstViewHolder.add(holder);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ViewPagerItem viewPagerItem = viewPagerItemArrayList.get(position);

            Target mTarget = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    if (bitmap != null) {
                        holder.thumbnail.setImageBitmap(bitmap);
                    }
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            };
            holder.itemView.setTag(mTarget);

            Picasso.get()
                    .load(viewPagerItem.imagePath)
                    .resize(1920, 0)
                    .rotate(viewPagerItem.exifOrientation)
                    .priority(Picasso.Priority.NORMAL)
                    .into(mTarget);
        }

        @Override
        public int getItemCount() {
            return viewPagerItemArrayList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private AppCompatImageView thumbnail;
            private boolean changeToolbarVisibility = false;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);

                setViewVariables();
                setOnClickListener();
            }

            private void setViewVariables() {
                thumbnail = itemView.findViewById(R.id.media_thumbnail_preview);
            }

            private void setOnClickListener() {
                this.thumbnail.setOnClickListener(v -> onThumbnailClick());
            }

            private void onThumbnailClick() {
                if(changeToolbarVisibility) {return;}
                changeToolbarVisibility = true;

                int visibility = (toolbarVisible) ? View.INVISIBLE : View.VISIBLE;
                bottomToolbar.setVisibility(visibility);
                topToolbar.setVisibility(visibility);

                toolbarVisible = !toolbarVisible;
                changeToolbarVisibility = false;
            }

        }
    }

    public class ViewPagerItem {
        private boolean checked = true;
        private int imageID;
        private String imagePath;
        private int exifOrientation;

        public ViewPagerItem(int imageID) {
            this.imageID = imageID;
            this.imagePath = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString() + "/" + imageID;
            this.exifOrientation = this.getExifOrientation();
        }

        public int getExifOrientation() {
            Uri uri = Uri.parse(this.imagePath);
            String[] projection = { Images.ImageColumns.ORIENTATION };
            Cursor cursor = PreviewActivity.this.getContentResolver().query(uri, projection, null, null, null);
            if (cursor == null || !cursor.moveToFirst()) {
                return 0;
            }
            return cursor.getInt(0);
        }
    }
}
