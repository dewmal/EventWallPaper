package com.juniperphoton.myersplash.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.juniperphoton.myersplash.App
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.extension.isLightColor
import com.juniperphoton.myersplash.utils.IntentUtil
import java.io.File

@Suppress("UNUSED")
class DownloadCompleteView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    @BindView(R.id.widget_set_as_rl)
    @JvmField var setAsBtn: RelativeLayout? = null

    @BindView(R.id.widget_set_as_root_rl)
    @JvmField var setAsRL: RelativeLayout? = null

    @BindView(R.id.set_as_tv)
    @JvmField var setAsTextView: TextView? = null

    var filePath: String? = null
    private var ctx: Context? = null

    init {
        ctx = context
        LayoutInflater.from(context).inflate(R.layout.widget_download_complete_view, this)
        ButterKnife.bind(this)
    }

    @OnClick(R.id.widget_set_as_rl)
    internal fun setAs() {
        filePath?.let {
            val intent = IntentUtil.getSetAsWallpaperIntent(File(it))
            App.instance.startActivity(intent)
        }
    }

    fun setThemeBackColor(color: Int) {
        setAsRL!!.background = ColorDrawable(color)
        setAsTextView!!.setTextColor(if (color.isLightColor()) Color.BLACK else Color.WHITE)
    }
}
