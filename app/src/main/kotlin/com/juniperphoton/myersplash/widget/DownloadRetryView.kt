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
import butterknife.BindView
import butterknife.ButterKnife
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.extension.isLightColor

class DownloadRetryView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    @BindView(R.id.widget_retry_rl)
    @JvmField var retryRL: RelativeLayout? = null

    @BindView(R.id.retry_tv)
    @JvmField var retryTextView: TextView? = null

    @BindView(R.id.widget_retry_btn)
    @JvmField var retryBtn: View? = null

    @BindView(R.id.delete_btn)
    @JvmField var deleteView: ImageView? = null

    @BindView(R.id.delete_btn_root)
    @JvmField var deleteRoot: View? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.widget_download_retry_view, this)
        ButterKnife.bind(this)
    }

    fun setOnClickDeleteListener(listener: View.OnClickListener) {
        deleteRoot?.setOnClickListener(listener)
    }

    fun setOnClickRetryListener(listener: View.OnClickListener) {
        retryBtn?.setOnClickListener(listener)
    }

    fun setThemeBackColor(color: Int) {
        retryRL?.background = ColorDrawable(color)
        retryTextView?.setTextColor(if (color.isLightColor()) Color.BLACK else Color.WHITE)
        if (color.isLightColor()) {
            deleteView?.setImageResource(R.drawable.vector_ic_delete_black)
        }
    }
}
