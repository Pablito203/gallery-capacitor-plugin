package com.pablito203.plugins.gallerycapacitorplugin;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;

public class PreviewActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView buttonBack;
    private LinearLayout buttonApply;
    private TextView countSelectedTextView;
    private final ImageFetcher fetcher = new ImageFetcher();
    private ArrayList<SelectedFile> selectedFiles = new ArrayList();
    private ArrayList<Integer> lstImageIDRemoved = new ArrayList();
    private ViewPager2 viewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = this.getIntent();

        selectedFiles = intent.getParcelableArrayListExtra("selectedFiles");

        setContentView(R.layout.preview);
        setupHeader();

        ArrayList<ViewPagerItem> viewPagerItemArrayList = new ArrayList<>();

        for (int i = 0; i < selectedFiles.size(); i++) {
            ViewPagerItem viewPagerItem = new ViewPagerItem(selectedFiles.get(i).imageID, selectedFiles.get(i).imageRotate);
            viewPagerItemArrayList.add(viewPagerItem);
        }
        viewPager = findViewById(R.id.pager);
        ViewPageAdapter pagerAdapter = new ViewPageAdapter(viewPagerItemArrayList);
        viewPager.setAdapter(pagerAdapter);

        buttonBack = findViewById(R.id.button_back);
        buttonBack.setOnClickListener(this);

        buttonApply = findViewById(R.id.button_apply_preview);
        buttonApply.setOnClickListener(this);

        countSelectedTextView = findViewById(R.id.count_selected_preview);
        countSelectedTextView.setText(String.format("(%d)", selectedFiles.size()));
    }

    private void setupHeader() {
        getSupportActionBar().hide();
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.BLACK);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_back) {
            setResult(RESULT_CANCELED);
            finish();
        } else if (v.getId() == R.id.button_apply_preview) {
            Intent result = new Intent();
            result.putIntegerArrayListExtra("lstImageIDRemoved", lstImageIDRemoved);
            setResult(RESULT_OK, result);
            finish();
        }
    }

    private class ViewPageAdapter extends RecyclerView.Adapter<ViewPageAdapter.ViewHolder> {
        ArrayList<ViewPagerItem> viewPagerItemArrayList;

        public ViewPageAdapter(ArrayList<ViewPagerItem> viewPagerItemArrayList) {
            this.viewPagerItemArrayList = viewPagerItemArrayList;
        }
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_preview, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ViewPagerItem viewPagerItem = viewPagerItemArrayList.get(position);

            holder.imageID = viewPagerItem.imageID;
            holder.imageRotate = viewPagerItem.imageRotate;
            if (holder.checked != viewPagerItem.checked) {
                holder.checked = viewPagerItem.checked;
                holder.radioCheckView.setChecked(holder.checked);
            }

            String stringr = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString() + String.format("/%d", holder.imageID);

            Uri imgUri=Uri.parse(stringr);
            holder.thumbnail.setImageURI(imgUri);
        }

        @Override
        public int getItemCount() {
            return viewPagerItemArrayList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private RadioCheckView radioCheckView;
            private AppCompatImageView thumbnail;
            private boolean checked = true;
            private int imageID = 0;
            private int imageRotate = 0;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);

                radioCheckView = itemView.findViewById(R.id.check_view_preview);
                radioCheckView.setOnClickListener(this);
                thumbnail = itemView.findViewById(R.id.media_thumbnail_preview);
            }

            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.check_view_preview) {
                    checked = !checked;
                    this.radioCheckView.setChecked(checked);

                    if (checked) {
                        int index = lstImageIDRemoved.indexOf(imageID);
                        lstImageIDRemoved.remove(index);
                    } else {
                        lstImageIDRemoved.add(imageID);
                    }

                    int countSelected = selectedFiles.size() - lstImageIDRemoved.size();
                    countSelectedTextView.setText(String.format("(%d)", countSelected));
                }
            }
        }
    }

    public class ViewPagerItem {
        private boolean checked = true;
        private int imageID = 0;
        private int imageRotate = 0;

        public ViewPagerItem(int imageID, int imageRotate) {
            this.imageID = imageID;
            this.imageRotate = imageRotate;
        }
    }
}
