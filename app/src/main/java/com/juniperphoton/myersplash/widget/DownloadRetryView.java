package com.juniperphoton.myersplash.widget;


import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.juniperphoton.myersplash.R;
import com.juniperphoton.myersplash.interfaces.ISetThemeColor;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DownloadRetryView extends FrameLayout implements ISetThemeColor {

    @Bind(R.id.widget_retry_btn)
    RelativeLayout mRetryBtn;

    @Bind(R.id.widget_retry_rl)
    RelativeLayout RetryRL;

    public DownloadRetryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.widget_download_retry_view, this);
        ButterKnife.bind(this);
    }

    @Override
    public void setThemeBackColor(int color) {
        RetryRL.setBackground(new ColorDrawable(color));
    }

    @Override
    public void setThemeForeColor(int color) {

    }
}
