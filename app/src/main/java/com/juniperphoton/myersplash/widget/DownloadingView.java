package com.juniperphoton.myersplash.widget;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.juniperphoton.myersplash.R;
import com.juniperphoton.myersplash.interfaces.ISetThemeColor;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DownloadingView extends FrameLayout implements ISetThemeColor {

    @Bind(R.id.downloading_progress_pv)
    ProgressView ProgressView;

    @Bind(R.id.downloading_progress_tv)
    TextView ProgressTV;

    @Bind(R.id.downloading_root_rl)
    RelativeLayout RootRL;

    @Bind(R.id.downloading_cancel_rl)
    RelativeLayout CancelRL;

    private OnClickListener mListener;

    public DownloadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.widget_downloading_view, this);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.downloading_cancel_rl)
    void onCancel() {
        if (mListener != null) {
            mListener.onClick(CancelRL);
        }
    }

    public void setClickCancelListener(OnClickListener listener) {
        mListener = listener;
    }

    public void setThemeBackColor(int color) {
        RootRL.setBackground(new ColorDrawable(color));
        this.ProgressView.setThemeColor(color);
    }

    @Override
    public void setThemeForeColor(int color) {

    }

    public void setProgress(int progress) {
        this.ProgressView.setProgress(progress);
        this.ProgressTV.setText(String.valueOf(progress) + "%");
    }
}
