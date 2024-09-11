package com.pablito203.plugins.gallerycapacitorplugin.Views;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

import com.pablito203.plugins.gallerycapacitorplugin.R;

public class RadioCheckView extends AppCompatImageView {
    public RadioCheckView(Context context) {
        super(context);
    }

    public RadioCheckView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setChecked(boolean enable) {
        if (enable) {
            setImageResource(R.drawable.ic_radio_on);
        } else {
            setImageResource(R.drawable.ic_radio_off);
        }
    }
}