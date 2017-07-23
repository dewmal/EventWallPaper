package com.juniperphoton.myersplash.widget.item

import android.content.Context
import android.graphics.RectF
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import butterknife.BindView
import butterknife.ButterKnife
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.extension.getDarker
import com.juniperphoton.myersplash.model.UnsplashImage
import com.juniperphoton.myersplash.utils.LocalSettingHelper

class PhotoItemView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    @BindView(R.id.row_photo_iv)
    lateinit var simpleDraweeView: SimpleDraweeView

    @BindView(R.id.row_photo_root)
    lateinit var rootView: ViewGroup

    @BindView(R.id.row_photo_download_rl)
    lateinit var downloadRL: ViewGroup

    @BindView(R.id.row_photo_ripple_mask_rl)
    lateinit var rippleMaskRL: ViewGroup

    @BindView(R.id.row_photo_recommended_tag)
    lateinit var recommendedTag: View

    var onClickPhoto: ((rectF: RectF, unsplashImage: UnsplashImage, itemView: View) -> Unit)? = null
    var onClickQuickDownload: ((image: UnsplashImage) -> Unit)? = null
    var onBind: ((View, Int) -> Unit)? = null

    override fun onFinishInflate() {
        super.onFinishInflate()
        ButterKnife.bind(this, this)
    }

    fun bind(image: UnsplashImage?, pos: Int) {
        if (image == null) return
        val regularUrl = image.listUrl

        val backColor = image.themeColor.getDarker(0.7f)

        if (LocalSettingHelper.getBoolean(context, context.getString(R.string.preference_key_quick_download), true)) {
            if (!image.hasDownloaded()) {
                downloadRL.visibility = View.VISIBLE
                downloadRL.setOnClickListener {
                    onClickQuickDownload?.invoke(image)
                }
            } else {
                downloadRL.visibility = View.GONE
            }
        } else {
            downloadRL.visibility = View.GONE
        }

        if (!image.isUnsplash) {
            recommendedTag.visibility = View.VISIBLE
        } else {
            recommendedTag.visibility = View.GONE
        }

        rootView.background = ColorDrawable(backColor)
        simpleDraweeView.setImageURI(regularUrl)
        rippleMaskRL.setOnClickListener(View.OnClickListener {
            if (regularUrl == null) {
                return@OnClickListener
            }
            if (!Fresco.getImagePipeline().isInBitmapMemoryCache(Uri.parse(image.listUrl))) {
                return@OnClickListener
            }
            val location = IntArray(2)
            simpleDraweeView.getLocationOnScreen(location)
            onClickPhoto?.invoke(RectF(
                    location[0].toFloat(),
                    location[1].toFloat(),
                    simpleDraweeView.width.toFloat(),
                    simpleDraweeView.height.toFloat()), image, rootView)
        })

        onBind?.invoke(rootView, pos)
    }
}