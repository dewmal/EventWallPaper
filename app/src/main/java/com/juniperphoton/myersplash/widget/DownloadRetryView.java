package com.juniperphoton.myersplash.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.juniperphoton.myersplash.R;
import com.juniperphoton.myersplash.utils.ColorUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DownloadRetryView extends FrameLayout {

    @BindView(R.id.widget_retry_rl)
    RelativeLayout retryRL;

    @BindView(R.id.retry_tv)
    TextView retryTextView;

    @BindView(R.id.widget_retry_btn)
    View retryBtn;

    @BindView(R.id.delete_btn)
    ImageView deleteView;

    @BindView(R.id.delete_btn_root)
    View mDeleteRoot;

    public DownloadRetryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.widget_download_retry_view, this);
        ButterKnife.bind(this);
    }

    public void setOnClickDeleteListener(View.OnClickListener listener) {
        mDeleteRoot.setOnClickListener(listener);
    }

    public void setOnClickRetryListener(View.OnClickListener listener) {
        retryBtn.setOnClickListener(listener);
    }

    public void setThemeBackColor(int color) {
        retryRL.setBackground(new ColorDrawable(color));
        retryTextView.setTextColor(ColorUtil.Companion.isColorLight(color) ? Color.BLACK : Color.WHITE);
        if (ColorUtil.Companion.isColorLight(color)) {
            deleteView.setImageResource(R.drawable.vector_ic_delete_black);
        }
    }
}
