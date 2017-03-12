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

import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.callback.OnClickQuickDownloadCallback
import com.juniperphoton.myersplash.callback.OnLoadMoreListener
import com.juniperphoton.myersplash.common.Constant
import com.juniperphoton.myersplash.fragment.MainListFragment
import com.juniperphoton.myersplash.model.UnsplashImage
import com.juniperphoton.myersplash.utils.ColorUtil
import com.juniperphoton.myersplash.utils.LocalSettingHelper

import butterknife.BindView
import butterknife.ButterKnife

class PhotoAdapter(private val mData: MutableList<UnsplashImage?>?, private val mContext: Context) :
        RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {
    private val FOOTER_FLAG_NOT_SHOW = 0
    private val FOOTER_FLAG_SHOW = 1
    private val FOOTER_FLAG_SHOW_END = 1 shl 1 or FOOTER_FLAG_SHOW
    private var mOnLoadMoreListener: OnLoadMoreListener? = null
    private var mOnClickPhotoCallback: MainListFragment.Callback? = null
    private var mOnClickDownloadCallback: OnClickQuickDownloadCallback? = null

    private var isAutoLoadMore = true
    private var footerFlag = FOOTER_FLAG_SHOW

    private var mRecyclerView: RecyclerView? = null
    private var mLayoutManager: RecyclerView.LayoutManager? = null
    private var mLastPosition = -1

    init {
        mLastPosition = -1
        var size = mData?.size ?: 0
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
        val lastItemIndex = findLastVisibleItemPosition(mLayoutManager)
        if (position >= maxPhotoCountOnScreen || position <= mLastPosition
                || lastItemIndex >= maxPhotoCountOnScreen) {
            return
        }

        mLastPosition = position

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
            val height = mRecyclerView!!.height
            val imgHeight = mRecyclerView!!.resources.getDimensionPixelSize(R.dimen.img_height)
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
        mRecyclerView = recyclerView
        mLastPosition = -1
        mLayoutManager = recyclerView!!.layoutManager
        startLoadMore(recyclerView, mLayoutManager)
    }

    private fun startLoadMore(recyclerView: RecyclerView?, layoutManager: RecyclerView.LayoutManager?) {
        if (mOnLoadMoreListener == null) {
            return
        }

        recyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (!isAutoLoadMore && findLastVisibleItemPosition(layoutManager) + 1 == itemCount) {
                        scrollLoadMore()
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
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

    fun setOnLoadMoreListener(loadMoreListener: OnLoadMoreListener) {
        mOnLoadMoreListener = loadMoreListener
    }

    fun setOnClickItemListener(callback: MainListFragment.Callback?) {
        mOnClickPhotoCallback = callback
    }

    fun setOnClickDownloadCallback(callback: OnClickQuickDownloadCallback) {
        mOnClickDownloadCallback = callback
    }

    val firstImage: UnsplashImage?
        get() {
            if (mData != null && mData.size > 0) {
                return mData[0]
            }
            return null
        }

    private fun scrollLoadMore() {
        mOnLoadMoreListener!!.OnLoadMore()
    }

    companion object {
        val TYPE_COMMON_VIEW = 1
        val TYPE_FOOTER_VIEW = 1 shl 1
    }

    inner class PhotoViewHolder(itemView: View, type: Int, footerFlag: Int) : RecyclerView.ViewHolder(itemView) {
        @BindView(R.id.row_photo_iv)
        @JvmField var mSimpleDraweeView: SimpleDraweeView? = null

        @BindView(R.id.row_photo_cv)
        @JvmField var mRootCardView: CardView? = null

        @BindView(R.id.row_photo_download_rl)
        @JvmField var mDownloadRL: ViewGroup? = null

        @BindView(R.id.row_photo_ripple_mask_rl)
        @JvmField var mRippleMaskRL: ViewGroup? = null

        @BindView(R.id.row_footer_rl)
        @JvmField var mFooterRL: ViewGroup? = null

        init {
            ButterKnife.bind(this, itemView)
            if (type == PhotoAdapter.TYPE_COMMON_VIEW) {
                // Ignore
            } else {
                if (footerFlag == FOOTER_FLAG_NOT_SHOW) {
                    mFooterRL!!.visibility = View.INVISIBLE
                }
            }
        }

        fun bind(image: UnsplashImage?, pos: Int) {
            if (image == null) return
            val regularUrl = image.listUrl

            val backColor = ColorUtil.getDarkerColor(image.themeColor, 0.7f)

            if (LocalSettingHelper.getBoolean(mContext, Constant.QUICK_DOWNLOAD_CONFIG_NAME, false)) {
                if (!image.hasDownloaded()) {
                    mDownloadRL!!.visibility = View.VISIBLE
                    mDownloadRL!!.setOnClickListener {
                        if (mOnClickDownloadCallback != null) {
                            mOnClickDownloadCallback!!.onClickQuickDownload(image)
                        }
                    }
                } else {
                    mDownloadRL!!.visibility = View.GONE
                }
            } else {
                mDownloadRL!!.visibility = View.GONE
            }
            if (mSimpleDraweeView != null) {
                mRootCardView!!.background = ColorDrawable(backColor)
                mSimpleDraweeView!!.setImageURI(regularUrl)
                mRippleMaskRL!!.setOnClickListener(View.OnClickListener {
                    if (regularUrl == null) {
                        return@OnClickListener
                    }
                    if (!Fresco.getImagePipeline().isInBitmapMemoryCache(Uri.parse(regularUrl))) {
                        return@OnClickListener
                    }
                    val location = IntArray(2)
                    mSimpleDraweeView!!.getLocationOnScreen(location)
                    if (mOnClickPhotoCallback != null) {
                        mOnClickPhotoCallback!!.clickPhotoItem(RectF(
                                location[0].toFloat(), location[1].toFloat(),
                                mSimpleDraweeView!!.width.toFloat(), mSimpleDraweeView!!.height.toFloat()), image, mRootCardView!!)
                    }
                })
            }
            animateContainer(mRootCardView!!, pos)
        }
    }
}


