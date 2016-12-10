package com.juniperphoton.myersplash.widget;


import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.juniperphoton.myersplash.R;
import com.juniperphoton.myersplash.interfaces.ISetThemeColor;
import com.juniperphoton.myersplash.utils.ColorUtil;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DownloadRetryView extends FrameLayout implements ISetThemeColor {

    @Bind(R.id.widget_retry_btn)
    RelativeLayout retryBtn;

    @Bind(R.id.widget_retry_rl)
    RelativeLayout retryRL;

    @Bind(R.id.retry_tv)
    TextView retryTextView;

    public DownloadRetryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.widget_download_retry_view, this);
        ButterKnife.bind(this);
    }

    @Override
    public void setThemeBackColor(int color) {
        retryRL.setBackground(new ColorDrawable(color));
        retryTextView.setTextColor(ColorUtil.isColorLight(color) ? Color.BLACK : Color.WHITE);
    }
}
