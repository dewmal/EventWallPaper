package com.juniperphoton.myersplash.activity

import android.animation.Animator
import android.content.Intent
import android.graphics.RectF
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.v4.view.ViewPager
import android.view.View
import android.view.ViewAnimationUtils
import android.widget.RelativeLayout
import android.widget.TextView

import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.adapter.MainListFragmentAdapter
import com.juniperphoton.myersplash.common.Constant
import com.juniperphoton.myersplash.event.ScrollToTopEvent
import com.juniperphoton.myersplash.fragment.MainListFragment
import com.juniperphoton.myersplash.model.UnsplashCategory
import com.juniperphoton.myersplash.model.UnsplashImage
import com.juniperphoton.myersplash.utils.AnimatorListenerImpl
import com.juniperphoton.myersplash.utils.DeviceUtil
import com.juniperphoton.myersplash.utils.DisplayUtil
import com.juniperphoton.myersplash.utils.LocalSettingHelper
import com.juniperphoton.myersplash.utils.PermissionUtil
import com.juniperphoton.myersplash.widget.ImageDetailView
import com.juniperphoton.myersplash.widget.PivotTitleBar

import org.greenrobot.eventbus.EventBus

import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.juniperphoton.myersplash.widget.SearchView

class MainActivity : BaseActivity(), ImageDetailView.StateListener, MainListFragment.Callback {

    @BindView(R.id.pivot_title_bar)
    @JvmField var mPivotTitleBar: PivotTitleBar? = null

    @BindView(R.id.toolbar_layout)
    @JvmField var mAppBarLayout: AppBarLayout? = null

    @BindView(R.id.activity_main_cl)
    @JvmField var mCoordinatorLayout: CoordinatorLayout? = null

    @BindView(R.id.content_activity_search_fab)
    @JvmField var mSearchFAB: FloatingActionButton? = null

    @BindView(R.id.activity_main_detail_view)
    @JvmField var mDetailView: ImageDetailView? = null

    @BindView(R.id.activity_main_search_view)
    @JvmField var mSearchView: SearchView? = null

    @BindView(R.id.view_pager)
    @JvmField var mViewPager: ViewPager? = null

    @BindView(R.id.main_search_tag)
    @JvmField var mTagView: TextView? = null

    private var mMainListFragmentAdapter: MainListFragmentAdapter? = null

    private var mHandleShortcut: Boolean = false
    private var mDefaultIndex = 1
    private var mLastX: Int = 0
    private var mLastY: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)

        handleShortcutsAction()

        initMainViews()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onResume() {
        super.onResume()
        mDetailView!!.registerEventBus()
        mSearchView!!.registerEventBus()

        PermissionUtil.checkAndRequest(this@MainActivity)
    }

    override fun onPause() {
        super.onPause()
        mDetailView!!.unregisterEventBus()
        mSearchView!!.unregisterEventBus()
    }

    @OnClick(R.id.content_activity_search_fab)
    internal fun onClickSearchFAB() {
        toggleSearchView(true, true)
    }

    private fun toggleSearchView(show: Boolean, useAnimation: Boolean) {
        if (show) {
            mSearchFAB!!.hide()
        } else {
            mSearchFAB!!.show()
        }
        val location = IntArray(2)
        mSearchFAB!!.getLocationOnScreen(location)

        if (show) {
            mLastX = (location[0] + mSearchFAB!!.width / 2f).toInt()
            mLastY = (location[1] + mSearchFAB!!.height / 2f).toInt()
        }

        val width = window.decorView.width
        val height = window.decorView.height

        val radius = Math.sqrt(Math.pow(width.toDouble(), 2.0) + Math.pow(height.toDouble(), 2.0)).toInt()
        val animator = ViewAnimationUtils.createCircularReveal(mSearchView, mLastX, mLastY, (if (show) 0 else radius).toFloat(), (if (show) radius else 0).toFloat())
        animator.addListener(object : AnimatorListenerImpl() {
            override fun onAnimationEnd(animation: Animator) {
                if (!show) {
                    mSearchView!!.reset()
                    mSearchView!!.visibility = View.GONE
                } else {
                    mSearchView!!.onShown()
                }
            }
        })
        mSearchView!!.visibility = View.VISIBLE
        if (show) {
            mSearchView!!.tryShowKeyboard()
            mSearchView!!.onShowing()
        } else {
            mSearchView!!.onHiding()
        }
        if (useAnimation) {
            animator.start()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mDetailView!!.deleteShareFileInDelay()
    }

    private fun getIdByIndex(index: Int): Int {
        when (index) {
            0 -> return UnsplashCategory.FEATURED_CATEGORY_ID
            1 -> return UnsplashCategory.NEW_CATEGORY_ID
            2 -> return UnsplashCategory.RANDOM_CATEGORY_ID
            else -> return UnsplashCategory.NEW_CATEGORY_ID
        }
    }

    private fun initMainViews() {
        mDetailView!!.setNavigationCallback(this)
        mPivotTitleBar!!.setOnClickTitleListener(object : PivotTitleBar.OnClickTitleListener {
            override fun onSingleTap(index: Int) {
                if (mViewPager != null) {
                    mViewPager!!.currentItem = index
                    EventBus.getDefault().post(ScrollToTopEvent(getIdByIndex(index), false))
                }
            }

            override fun onDoubleTap(index: Int) {
                if (mViewPager != null) {
                    mViewPager!!.currentItem = index
                    EventBus.getDefault().post(ScrollToTopEvent(getIdByIndex(index), true))
                }
            }
        })
        mPivotTitleBar!!.selectedItem = mDefaultIndex

        mMainListFragmentAdapter = MainListFragmentAdapter(this, supportFragmentManager)
        mViewPager!!.adapter = mMainListFragmentAdapter
        mViewPager!!.currentItem = mDefaultIndex
        mViewPager!!.offscreenPageLimit = 2
        mViewPager!!.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                mPivotTitleBar!!.selectedItem = position
                mTagView!!.text = "# ${mPivotTitleBar!!.selectedString}"
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

        if (!DeviceUtil.hasNavigationBar(this)) {
            val params = mSearchFAB!!.layoutParams as RelativeLayout.LayoutParams
            params.setMargins(0, 0,
                    DisplayUtil.getDimenInPixel(24, this),
                    DisplayUtil.getDimenInPixel(24, this))
            mSearchFAB!!.layoutParams = params
        }

        mAppBarLayout!!.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (Math.abs(verticalOffset) - appBarLayout.height == 0) {
                mTagView!!.animate().alpha(1f).setDuration(300).start()
                mSearchFAB!!.hide()
            } else {
                mTagView!!.animate().alpha(0f).setDuration(100).start()
                mSearchFAB!!.show()
            }
        }

        mTagView!!.setOnClickListener { EventBus.getDefault().post(ScrollToTopEvent(getIdByIndex(mPivotTitleBar!!.selectedItem), false)) }
    }

    private fun handleShortcutsAction() {
        if (mHandleShortcut) {
            return
        }
        val action = intent.action
        if (action != null) {
            when (action) {
                "action.search" -> {
                    mHandleShortcut = true
                    mAppBarLayout!!.post { toggleSearchView(true, false) }
                }
                "action.download" -> {
                    val intent = Intent(this, ManageDownloadActivity::class.java)
                    startActivity(intent)
                }
                "action.random" -> {
                    mHandleShortcut = true
                    mDefaultIndex = 2
                }
            }
        }
    }

    override fun onShowing() {
        mSearchFAB!!.hide()
    }

    override fun onHiding() {

    }

    override fun onShown() {

    }

    override fun onHidden() {
        mSearchFAB!!.show()
        if (mAppBarLayout!!.height - Math.abs(mAppBarLayout!!.top) < 0.01) {
            mTagView!!.animate().alpha(1f).setDuration(300).start()
        }
    }

    override fun onBackPressed() {
        if (mSearchView!!.visibility == View.VISIBLE) {
            if (mSearchView!!.tryHide()) {
                return
            }
            toggleSearchView(false, true)
            return
        }
        if (mDetailView!!.tryHide()) {
            return
        }
        super.onBackPressed()
    }

    override fun onScrollHide() {}

    override fun onScrollShow() {}

    override fun clickPhotoItem(rectF: RectF, unsplashImage: UnsplashImage, itemView: View) {
        val location = IntArray(2)
        mTagView!!.getLocationOnScreen(location)
        if (rectF.top <= location[1] + mTagView!!.height) {
            mTagView!!.animate().alpha(0f).setDuration(100).start()
        }
        mDetailView!!.showDetailedImage(rectF, unsplashImage, itemView)
    }

    companion object {
        private val TAG = "MainActivity"

        private val SEARCH_ID = -10000
    }
}
