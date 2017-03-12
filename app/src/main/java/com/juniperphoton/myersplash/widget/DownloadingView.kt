package com.juniperphoton.myersplash.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView

import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.utils.ColorUtil

import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick

@Suppress("UNUSED")
class DownloadingView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    @BindView(R.id.downloading_progress_pv)
    @JvmField var progressView: ProgressView? = null

    @BindView(R.id.downloading_progress_tv)
    @JvmField var progressTV: TextView? = null

    @BindView(R.id.downloading_root_rl)
    @JvmField var rootRL: RelativeLayout? = null

    @BindView(R.id.downloading_cancel_rl)
    @JvmField var cancelRL: RelativeLayout? = null

    @BindView(R.id.cancel_ic)
    @JvmField var cancelImageView: ImageView? = null

    private var mListener: View.OnClickListener? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.widget_downloading_view, this)
        ButterKnife.bind(this)
    }

    @OnClick(R.id.downloading_cancel_rl)
    internal fun onCancel() {
        mListener?.onClick(cancelRL)
    }

    fun setClickCancelListener(listener: View.OnClickListener) {
        mListener = listener
    }

    fun setThemeBackColor(color: Int) {
        rootRL?.background = ColorDrawable(color)
        progressView?.setThemeColor(color)
        if (ColorUtil.isColorLight(color)) {
            progressTV?.setTextColor(Color.BLACK)
            cancelImageView?.setImageResource(R.drawable.vector_ic_clear_black)
        } else {
            progressTV?.setTextColor(Color.WHITE)
        }
    }

    fun setProgress(progress: Int) {
        progressView?.setProgress(progress)
        progressTV?.text = "$progress%"
    }
}
