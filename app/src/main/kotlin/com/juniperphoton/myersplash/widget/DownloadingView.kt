package com.juniperphoton.myersplash.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.extension.isLightColor

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

    var onClickCancel: (() -> Unit)? = null

    var themeColor: Int = Color.TRANSPARENT
        set(value) {
            rootRL?.background = ColorDrawable(value)
            progressView?.color = value
            if (value.isLightColor()) {
                progressTV?.setTextColor(Color.BLACK)
                cancelImageView?.setImageResource(R.drawable.vector_ic_clear_black)
            } else {
                progressTV?.setTextColor(Color.WHITE)
            }
        }

    var progress: Int = 0
        set(value) {
            progressView?.progress = value
            progressTV?.text = "$value%"
        }

    init {
        LayoutInflater.from(context).inflate(R.layout.widget_downloading_view, this)
        ButterKnife.bind(this)
    }

    @OnClick(R.id.downloading_cancel_rl)
    internal fun onCancel() {
        onClickCancel?.invoke()
    }
}
