package com.pablito203.plugins.gallerycapacitorplugin;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

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