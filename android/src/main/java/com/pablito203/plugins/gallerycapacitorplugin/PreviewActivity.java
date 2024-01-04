package com.pablito203.plugins.gallerycapacitorplugin;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;

public class PreviewActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView buttonVoltar;
    private final ImageFetcher fetcher = new ImageFetcher();
    private ArrayList<Integer> lstImageIDSelected = new ArrayList();
    private ArrayList<Integer> lstImageRotateSelected = new ArrayList();
    private ViewPager2 viewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        lstImageIDSelected = getIntent().getIntegerArrayListExtra("lstImageIDSelected");
        lstImageRotateSelected = getIntent().getIntegerArrayListExtra("lstImageRotateSelected");

        setContentView(R.layout.preview);
        setupHeader();

        ArrayList<ViewPagerItem> viewPagerItemArrayList = new ArrayList<>();

        for (int i = 0; i < lstImageIDSelected.size(); i++) {
            ViewPagerItem viewPagerItem = new ViewPagerItem(lstImageIDSelected.get(i), lstImageRotateSelected.get(i));
            viewPagerItemArrayList.add(viewPagerItem);
        }
        viewPager = findViewById(R.id.pager);
        ViewPageAdapter pagerAdapter = new ViewPageAdapter(viewPagerItemArrayList);
        viewPager.setAdapter(pagerAdapter);

        buttonVoltar = findViewById(R.id.button_back);
        buttonVoltar.setOnClickListener(this);
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
            holder.checked = viewPagerItem.checked;
            fetcher.fetch(holder.imageID, holder.thumbnail, 0, holder.imageRotate);
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
