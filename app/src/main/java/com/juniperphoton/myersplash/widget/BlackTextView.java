package com.juniperphoton.myersplash.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.juniperphoton.myersplash.App;
import com.juniperphoton.myersplash.utils.TypefaceUtil;

public class BlackTextView extends AppCompatTextView {
    public BlackTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        setTypeface();
    }

    private void setTypeface() {
        TypefaceUtil.INSTANCE.setTypeface(this, "fonts/seguibl.ttf", App.Companion.getInstance());
    }
}
