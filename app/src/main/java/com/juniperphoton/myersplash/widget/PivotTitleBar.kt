package com.juniperphoton.myersplash.widget

import android.content.Context
import android.content.Intent
import android.support.v7.widget.PopupMenu
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout

import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.activity.AboutActivity
import com.juniperphoton.myersplash.activity.ManageDownloadActivity
import com.juniperphoton.myersplash.activity.SettingsActivity
import com.juniperphoton.myersplash.model.UnsplashCategory

import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick

@Suppress("UNUSED")
class PivotTitleBar(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    @BindView(R.id.more_btn)
    internal var mMoreBtn: View? = null

    @BindView(R.id.pivot_item_0)
    internal var mItem0: View? = null

    @BindView(R.id.pivot_item_1)
    internal var mItem1: View? = null

    @BindView(R.id.pivot_item_2)
    internal var mItem2: View? = null

    var selectedItem = DEFAULT_SELECTED
        private set(value) {}

    private var mCallback: OnClickTitleListener? = null

    private var mTouchingViewIndex: Int = 0

    private lateinit var mGestureDetector: GestureDetector

    private val mListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            if (mCallback == null) return true
            mCallback?.onSingleTap(mTouchingViewIndex)
            return super.onSingleTapUp(e)
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            if (mCallback == null) return true
            mCallback?.onDoubleTap(mTouchingViewIndex)
            return super.onDoubleTap(e)
        }
    }

    private val mOnTouchListener = View.OnTouchListener { v, event ->
        if (v === mItem0) {
            mTouchingViewIndex = 0
        } else if (v === mItem1) {
            mTouchingViewIndex = 1
        } else if (v === mItem2) {
            mTouchingViewIndex = 2
        }
        mGestureDetector.onTouchEvent(event)
        true
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.pivot_layout, this, true)
        ButterKnife.bind(this)

        mGestureDetector = GestureDetector(context, mListener)
        mItem0?.setOnTouchListener(mOnTouchListener)
        mItem1?.setOnTouchListener(mOnTouchListener)
        mItem2?.setOnTouchListener(mOnTouchListener)
    }

    fun setOnClickTitleListener(listener: OnClickTitleListener) {
        mCallback = listener
    }

    @OnClick(R.id.pivot_item_0)
    internal fun onClickItem0() {
        if (mCallback != null) {
            mCallback!!.onSingleTap(0)
        }
    }

    @OnClick(R.id.pivot_item_1)
    internal fun onClickItem1() {
        if (mCallback != null) {
            mCallback!!.onSingleTap(1)
        }
    }

    @OnClick(R.id.pivot_item_2)
    internal fun onClickItem2() {
        if (mCallback != null) {
            mCallback!!.onSingleTap(2)
        }
    }

    fun setSelected(index: Int) {
        toggleAnimation(selectedItem, index)
        selectedItem = index
    }

    val selectedString: String
        get() {
            when (selectedItem) {
                0 -> return UnsplashCategory.FEATURE_S.toUpperCase()
                1 -> return UnsplashCategory.NEW_S.toUpperCase()
                2 -> return UnsplashCategory.RANDOM_S.toUpperCase()
                else -> return UnsplashCategory.NEW_S.toUpperCase()
            }
        }

    private fun toggleAnimation(prevIndex: Int, newIndex: Int) {
        val preView = getViewByIndex(prevIndex)
        val nextView = getViewByIndex(newIndex)

        preView?.animate()?.alpha(0.3f)?.setDuration(300)?.start()
        nextView?.animate()?.alpha(1f)?.setDuration(300)?.start()
    }

    private fun getViewByIndex(index: Int): View? {
        return when (index) {
            0 -> mItem0
            1 -> mItem1
            2 -> mItem2
            else -> null
        }
    }

    @OnClick(R.id.more_btn)
    internal fun onClickMore() {
        val popupMenu = PopupMenu(context, mMoreBtn!!)
        popupMenu.inflate(R.menu.main)
        popupMenu.gravity = Gravity.RIGHT
        popupMenu.setOnMenuItemClickListener { item ->
            var intent: Intent?
            when (item.itemId) {
                R.id.menu_settings -> {
                    intent = Intent(context, SettingsActivity::class.java)
                    context.startActivity(intent)
                }
                R.id.menu_downloads -> {
                    intent = Intent(context, ManageDownloadActivity::class.java)
                    context.startActivity(intent)
                }
                R.id.menu_about -> {
                    intent = Intent(context, AboutActivity::class.java)
                    context.startActivity(intent)
                }
            }
            true
        }
        popupMenu.show()
    }

    interface OnClickTitleListener {
        fun onSingleTap(index: Int)

        fun onDoubleTap(index: Int)
    }

    companion object {
        private val DEFAULT_SELECTED = 1
    }
}
