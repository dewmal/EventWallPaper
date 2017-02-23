package com.juniperphoton.myersplash.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.juniperphoton.myersplash.R;
import com.juniperphoton.myersplash.interfaces.SetThemeColor;
import com.juniperphoton.myersplash.utils.ColorUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DownloadingView extends FrameLayout implements SetThemeColor {
    @BindView(R.id.downloading_progress_pv)
    ProgressView progressView;

    @BindView(R.id.downloading_progress_tv)
    TextView progressTV;

    @BindView(R.id.downloading_root_rl)
    RelativeLayout rootRL;

    @BindView(R.id.downloading_cancel_rl)
    RelativeLayout cancelRL;

    @BindView(R.id.cancel_ic)
    ImageView cancelImageView;

    private OnClickListener mListener;

    public DownloadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.widget_downloading_view, this);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.downloading_cancel_rl)
    void onCancel() {
        if (mListener != null) {
            mListener.onClick(cancelRL);
        }
    }

    public void setClickCancelListener(OnClickListener listener) {
        mListener = listener;
    }

    public void setThemeBackColor(int color) {
        rootRL.setBackground(new ColorDrawable(color));
        this.progressView.setThemeColor(color);
        if (ColorUtil.isColorLight(color)) {
            progressTV.setTextColor(Color.BLACK);
            cancelImageView.setImageResource(R.drawable.vector_ic_clear_black);
        } else {
            progressTV.setTextColor(Color.WHITE);
        }
    }

    public void setProgress(int progress) {
        this.progressView.setProgress(progress);
        this.progressTV.setText(String.valueOf(progress) + "%");
    }
}
