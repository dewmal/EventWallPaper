package com.juniperphoton.myersplash.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.TextView;

import com.juniperphoton.myersplash.base.App;
import com.juniperphoton.myersplash.utils.TypefaceUtil;

public class BlackTextView extends TextView {
    public BlackTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        setTypeface();
    }

    private void setTypeface() {
        TypefaceUtil.setTypeFace(this, "fonts/seguibl.ttf", App.getInstance());
    }
}
