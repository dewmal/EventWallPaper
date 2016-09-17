package com.juniperphoton.myersplash.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import butterknife.ButterKnife;

public class DetailView extends FrameLayout {



    public DetailView(Context context, AttributeSet attrs) {
        super(context, attrs);

        ButterKnife.bind(this);
    }
}
