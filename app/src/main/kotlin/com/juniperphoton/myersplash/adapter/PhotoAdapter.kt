package com.juniperphoton.myersplash.adapter

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.RectF
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import butterknife.BindView
import butterknife.ButterKnife
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.common.Constant
import com.juniperphoton.myersplash.extension.getDarker
import com.juniperphoton.myersplash.model.UnsplashImage
import com.juniperphoton.myersplash.utils.LocalSettingHelper

class PhotoAdapter(private val mData: MutableList<UnsplashImage?>?, private val mContext: Context) :
        RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {
    private val FOOTER_FLAG_NOT_SHOW = 0
    private val FOOTER_FLAG_SHOW = 1
    private val FOOTER_FLAG_SHOW_END = 1 shl 1 or FOOTER_FLAG_SHOW

    private var isAutoLoadMore = true
    private var footerFlag = FOOTER_FLAG_SHOW

    private var recyclerView: RecyclerView? = null
    private var layoutManager: RecyclerView.LayoutManager? = null
    private var lastPosition = -1

    var onClickPhoto: ((rectF: RectF, unsplashImage: UnsplashImage, itemView: View) -> Unit)? = null
    var onClickQuickDownload: ((image: UnsplashImage) -> Unit)? = null
    var onLoadMore: (() -> Unit)? = null

    init {
        lastPosition = -1
        val size = mData?.size ?: 0
        if (size >= 10) {
            isAutoLoadMore = true
            footerFlag = FOOTER_FLAG_SHOW
        } else if (size > 0) {
            isAutoLoadMore = false
            footerFlag = FOOTER_FLAG_SHOW_END
        } else {
            isAutoLoadMore = false
            footerFlag = FOOTER_FLAG_NOT_SHOW
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder? {
        when (viewType) {
            TYPE_COMMON_VIEW -> {
                val view = LayoutInflater.from(mContext).inflate(R.layout.row_photo, parent, false)
                return PhotoViewHolder(view, viewType, footerFlag)
            }
            TYPE_FOOTER_VIEW -> {
                val view: View
                if (footerFlag == FOOTER_FLAG_SHOW_END) {
                    view = LayoutInflater.from(mContext).inflate(R.layout.row_footer_end, parent, false)
                } else {
                    view = LayoutInflater.from(mContext).inflate(R.layout.row_footer, parent, false)
                }
                return PhotoViewHolder(view, viewType, footerFlag)
            }
        }
        return null
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        if (holder.itemViewType == TYPE_COMMON_VIEW) {
            holder.bind(mData!![holder.adapterPosition], position)
        }
    }

    private fun animateContainer(container: View, position: Int) {
        val lastItemIndex = findLastVisibleItemPosition(layoutManager)
        if (position >= maxPhotoCountOnScreen || position <= lastPosition
                || lastItemIndex >= maxPhotoCountOnScreen) {
            return
        }

        lastPosition = position

        val delay = 300 * (position + 1)
        val duration = 800

        container.alpha = 0f
        container.translationX = 300f

        val animator = ValueAnimator.ofFloat(0.0f, 1.0f)
        animator.addUpdateListener { valueAnimator -> container.alpha = valueAnimator.animatedValue as Float }
        animator.startDelay = delay.toLong()
        animator.duration = duration.toLong()
        animator.start()

        val animator2 = ValueAnimator.ofInt(300, 0)
        animator2.addUpdateListener { valueAnimator -> container.translationX = (valueAnimator.animatedValue as Int).toFloat() }
        animator2.interpolator = DecelerateInterpolator()
        animator2.startDelay = delay.toLong()
        animator2.duration = duration.toLong()
        animator2.start()
    }

    private val maxPhotoCountOnScreen: Int
        get() {
            val height = recyclerView!!.height
            val imgHeight = recyclerView!!.resources.getDimensionPixelSize(R.dimen.img_height)
            return Math.ceil(height.toDouble() / imgHeight.toDouble()).toInt()
        }

    override fun getItemCount(): Int {
        if (mData == null) return 0
        return if (footerFlag != FOOTER_FLAG_NOT_SHOW) mData.size + 1 else mData.size
    }

    override fun getItemViewType(position: Int): Int {
        if (isFooterView(position)) {
            return TYPE_FOOTER_VIEW
        } else
            return TYPE_COMMON_VIEW
    }

    private fun isFooterView(position: Int): Boolean {
        return footerFlag != FOOTER_FLAG_NOT_SHOW && position >= itemCount - 1
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
        lastPosition = -1
        layoutManager = recyclerView!!.layoutManager
        startLoadMore(recyclerView, layoutManager)
    }

    private fun startLoadMore(recyclerView: RecyclerView?, layoutManager: RecyclerView.LayoutManager?) {
        if (onLoadMore == null) {
            return
        }

        recyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(list: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(list, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (!isAutoLoadMore && findLastVisibleItemPosition(layoutManager) + 1 == itemCount) {
                        scrollLoadMore()
                    }
                }
            }

            override fun onScrolled(list: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(list, dx, dy)
                if (isAutoLoadMore && findLastVisibleItemPosition(layoutManager) + 1 == itemCount) {
                    scrollLoadMore()
                } else if (isAutoLoadMore) {
                    isAutoLoadMore = false
                }
            }
        })
    }

    fun clear() {
        footerFlag = FOOTER_FLAG_NOT_SHOW
        mData!!.clear()
        notifyDataSetChanged()
    }

    fun setLoadMoreData(data: MutableList<UnsplashImage?>?) {
        val size = mData!!.size
        mData.addAll(data!!)
        if (data.size >= 10) {
            isAutoLoadMore = true
            footerFlag = footerFlag or FOOTER_FLAG_SHOW
            notifyItemInserted(size)
        } else if (data.size > 0) {
            isAutoLoadMore = false
            footerFlag = footerFlag or FOOTER_FLAG_SHOW
            footerFlag = footerFlag or FOOTER_FLAG_SHOW_END
            notifyItemInserted(size)
        } else {
            isAutoLoadMore = false
            footerFlag = FOOTER_FLAG_NOT_SHOW
        }
    }

    private fun findLastVisibleItemPosition(layoutManager: RecyclerView.LayoutManager?): Int {
        if (layoutManager is LinearLayoutManager) {
            return layoutManager.findLastVisibleItemPosition()
        }
        return -1
    }

    val firstImage: UnsplashImage?
        get() {
            if (mData != null && mData.size > 0) {
                return mData[0]
            }
            return null
        }

    private fun scrollLoadMore() {
        onLoadMore?.invoke()
    }

    companion object {
        val TYPE_COMMON_VIEW = 1
        val TYPE_FOOTER_VIEW = 1 shl 1
    }

    inner class PhotoViewHolder(itemView: View, type: Int, footerFlag: Int) : RecyclerView.ViewHolder(itemView) {
        @BindView(R.id.row_photo_iv)
        @JvmField var simpleDraweeView: SimpleDraweeView? = null

        @BindView(R.id.row_photo_cv)
        @JvmField var rootCardView: CardView? = null

        @BindView(R.id.row_photo_download_rl)
        @JvmField var downloadRL: ViewGroup? = null

        @BindView(R.id.row_photo_ripple_mask_rl)
        @JvmField var rippleMaskRL: ViewGroup? = null

        @BindView(R.id.row_footer_rl)
        @JvmField var footerRL: ViewGroup? = null

        init {
            ButterKnife.bind(this, itemView)
            if (type == PhotoAdapter.TYPE_COMMON_VIEW) {
                // Ignore
            } else {
                if (footerFlag == FOOTER_FLAG_NOT_SHOW) {
                    footerRL!!.visibility = View.INVISIBLE
                }
            }
        }

        fun bind(image: UnsplashImage?, pos: Int) {
            if (image == null) return
            val regularUrl = image.listUrl

            val backColor = image.themeColor.getDarker(0.7f)

            if (LocalSettingHelper.getBoolean(mContext, Constant.QUICK_DOWNLOAD_CONFIG_NAME, true)) {
                if (!image.hasDownloaded()) {
                    downloadRL!!.visibility = View.VISIBLE
                    downloadRL!!.setOnClickListener {
                        onClickQuickDownload?.invoke(image)
                    }
                } else {
                    downloadRL!!.visibility = View.GONE
                }
            } else {
                downloadRL!!.visibility = View.GONE
            }
            if (simpleDraweeView != null) {
                rootCardView!!.background = ColorDrawable(backColor)
                simpleDraweeView!!.setImageURI(regularUrl)
                rippleMaskRL!!.setOnClickListener(View.OnClickListener {
                    if (regularUrl == null) {
                        return@OnClickListener
                    }
                    if (!Fresco.getImagePipeline().isInBitmapMemoryCache(Uri.parse(regularUrl))) {
                        return@OnClickListener
                    }
                    val location = IntArray(2)
                    simpleDraweeView!!.getLocationOnScreen(location)
                    if (onClickPhoto != null) {
                        onClickPhoto?.invoke(RectF(
                                location[0].toFloat(), location[1].toFloat(),
                                simpleDraweeView!!.width.toFloat(), simpleDraweeView!!.height.toFloat()), image, rootCardView!!)
                    }
                })
            }
            animateContainer(rootCardView!!, pos)
        }
    }
}


