package com.juniperphoton.myersplash.widget

import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.support.v4.content.FileProvider
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView

import com.juniperphoton.myersplash.App
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.utils.ColorUtil

import java.io.File

import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick

@Suppress("UNUSED")
class DownloadCompleteView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    @BindView(R.id.widget_set_as_rl)
    @JvmField var setAsBtn: RelativeLayout? = null

    @BindView(R.id.widget_set_as_root_rl)
    @JvmField var setAsRL: RelativeLayout? = null

    @BindView(R.id.set_as_tv)
    @JvmField var setAsTextView: TextView? = null

    private var mFileUrl: String? = null

    private var ctx: Context? = null

    init {
        ctx = context
        LayoutInflater.from(context).inflate(R.layout.widget_download_complete_view, this)
        ButterKnife.bind(this)
    }

    fun setFilePath(filePath: String) {
        mFileUrl = filePath
    }

    @OnClick(R.id.widget_set_as_rl)
    internal fun setAs() {
        if (mFileUrl != null) {
            val file = File(mFileUrl)
            val uri = FileProvider.getUriForFile(ctx, ctx?.getString(R.string.authorities), file)
            val intent = WallpaperManager.getInstance(ctx).getCropAndSetWallpaperIntent(uri)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            App.instance.startActivity(intent)
        }
    }

    fun setThemeBackColor(color: Int) {
        setAsRL!!.background = ColorDrawable(color)
        setAsTextView!!.setTextColor(if (ColorUtil.isColorLight(color)) Color.BLACK else Color.WHITE)
    }
}
