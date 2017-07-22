package com.juniperphoton.myersplash.adapter

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.RectF
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.model.UnsplashImage
import com.juniperphoton.myersplash.widget.item.PhotoItemView

class PhotoAdapter(private val imageData: MutableList<UnsplashImage?>?,
                   private val context: Context)
    : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {
    companion object {
        val TYPE_COMMON_VIEW = 0
        val TYPE_FOOTER_VIEW = 1
    }

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
        val size = imageData?.size ?: 0
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
                val view = LayoutInflater.from(context).inflate(R.layout.row_photo, parent, false)
                return PhotoViewHolder(view, viewType, footerFlag)
            }
            TYPE_FOOTER_VIEW -> {
                val view: View = when (footerFlag) {
                    FOOTER_FLAG_SHOW_END -> LayoutInflater.from(context).inflate(R.layout.row_footer_end, parent, false)
                    else -> LayoutInflater.from(context).inflate(R.layout.row_footer, parent, false)
                }
                return PhotoViewHolder(view, viewType, footerFlag)
            }
        }
        return null
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        if (holder.itemView is PhotoItemView) {
            holder.itemView.onBind = { v, p ->
                animateContainer(v, p)
            }
            holder.itemView.onClickPhoto = onClickPhoto
            holder.itemView.onClickQuickDownload = onClickQuickDownload
            holder.itemView.bind(imageData!![holder.adapterPosition], position)
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
        if (imageData == null) return 0
        return if (footerFlag != FOOTER_FLAG_NOT_SHOW) imageData.size + 1 else imageData.size
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
        imageData!!.clear()
        notifyDataSetChanged()
    }

    fun setLoadMoreData(data: MutableList<UnsplashImage?>?) {
        val size = this.imageData!!.size
        this.imageData.addAll(data!!)
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
            if (imageData != null && imageData.size > 0) {
                return imageData[0]
            }
            return null
        }

    private fun scrollLoadMore() {
        onLoadMore?.invoke()
    }

    inner class PhotoViewHolder(itemView: View, type: Int, footerFlag: Int) : RecyclerView.ViewHolder(itemView) {
        init {
            if (type != PhotoAdapter.TYPE_COMMON_VIEW && footerFlag == FOOTER_FLAG_NOT_SHOW) {
                itemView.visibility = View.INVISIBLE
            }
        }
    }
}


