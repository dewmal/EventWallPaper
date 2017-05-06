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
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.adapter.MainListFragmentAdapter
import com.juniperphoton.myersplash.event.ScrollToTopEvent
import com.juniperphoton.myersplash.extension.getDimenInPixel
import com.juniperphoton.myersplash.extension.hasNavigationBar
import com.juniperphoton.myersplash.fragment.MainListFragment
import com.juniperphoton.myersplash.model.UnsplashCategory
import com.juniperphoton.myersplash.model.UnsplashImage
import com.juniperphoton.myersplash.utils.AnimatorListenerImpl
import com.juniperphoton.myersplash.utils.FileUtil
import com.juniperphoton.myersplash.utils.PermissionUtil
import com.juniperphoton.myersplash.widget.ImageDetailView
import com.juniperphoton.myersplash.widget.PivotTitleBar
import com.juniperphoton.myersplash.widget.SearchView
import org.greenrobot.eventbus.EventBus
import rx.Observable
import rx.Subscriber
import rx.schedulers.Schedulers

class MainActivity : BaseActivity(), ImageDetailView.StateListener {
    companion object {
        private val TAG = "MainActivity"
        private val SEARCH_ID = -10000
    }

    @BindView(R.id.pivot_title_bar)
    @JvmField var pivotTitleBar: PivotTitleBar? = null

    @BindView(R.id.toolbar_layout)
    @JvmField var appBarLayout: AppBarLayout? = null

    @BindView(R.id.activity_main_cl)
    @JvmField var coordinatorLayout: CoordinatorLayout? = null

    @BindView(R.id.content_activity_search_fab)
    @JvmField var searchFAB: FloatingActionButton? = null

    @BindView(R.id.activity_main_detail_view)
    @JvmField var detailView: ImageDetailView? = null

    @BindView(R.id.activity_main_search_view)
    @JvmField var searchView: SearchView? = null

    @BindView(R.id.view_pager)
    @JvmField var viewPager: ViewPager? = null

    @BindView(R.id.main_search_tag)
    @JvmField var tagView: TextView? = null

    private var mainListFragmentAdapter: MainListFragmentAdapter? = null

    private var handleShortcut: Boolean = false
    private var defaultIndex = 1
    private var lastX: Int = 0
    private var lastY: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)

        handleShortcutsAction()
        clearSharedFiles()
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
        detailView!!.registerEventBus()
        searchView!!.registerEventBus()

        PermissionUtil.checkAndRequest(this@MainActivity)
    }

    override fun onPause() {
        super.onPause()
        detailView!!.unregisterEventBus()
        searchView!!.unregisterEventBus()
    }

    @OnClick(R.id.content_activity_search_fab)
    internal fun onClickSearchFAB() {
        toggleSearchView(true, true)
    }

    private fun toggleSearchView(show: Boolean, useAnimation: Boolean) {
        if (show) {
            searchFAB!!.hide()
        } else {
            searchFAB!!.show()
        }
        val location = IntArray(2)
        searchFAB!!.getLocationOnScreen(location)

        if (show) {
            lastX = (location[0] + searchFAB!!.width / 2f).toInt()
            lastY = (location[1] + searchFAB!!.height / 2f).toInt()
        }

        val width = window.decorView.width
        val height = window.decorView.height

        val radius = Math.sqrt(Math.pow(width.toDouble(), 2.0) + Math.pow(height.toDouble(), 2.0)).toInt()
        val animator = ViewAnimationUtils.createCircularReveal(searchView, lastX, lastY, (if (show) 0 else radius).toFloat(), (if (show) radius else 0).toFloat())
        animator.addListener(object : AnimatorListenerImpl() {
            override fun onAnimationEnd(animation: Animator) {
                if (!show) {
                    searchView!!.reset()
                    searchView!!.visibility = View.GONE
                } else {
                    searchView!!.onShown()
                }
            }
        })
        searchView!!.visibility = View.VISIBLE
        if (show) {
            searchView!!.tryShowKeyboard()
            searchView!!.onShowing()
        } else {
            searchView!!.onHiding()
        }
        if (useAnimation) {
            animator.start()
        }
    }

    private fun getIdByIndex(index: Int): Int {
        when (index) {
            0 -> return UnsplashCategory.FEATURED_CATEGORY_ID
            1 -> return UnsplashCategory.NEW_CATEGORY_ID
            2 -> return UnsplashCategory.RANDOM_CATEGORY_ID
            else -> return UnsplashCategory.NEW_CATEGORY_ID
        }
    }

    private fun clearSharedFiles() {
        Observable.just(FileUtil.sharePath)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(object : Subscriber<String>() {
                    override fun onCompleted() {
                    }

                    override fun onNext(t: String?) {
                        FileUtil.clearFilesToShared()
                    }

                    override fun onError(e: Throwable?) {
                        e?.printStackTrace()
                    }
                })
    }

    private fun initMainViews() {
        detailView!!.setNavigationCallback(this)
        pivotTitleBar!!.setOnClickTitleListener(object : PivotTitleBar.OnClickTitleListener {
            override fun onSingleTap(index: Int) {
                if (viewPager != null) {
                    viewPager!!.currentItem = index
                    EventBus.getDefault().post(ScrollToTopEvent(getIdByIndex(index), false))
                }
            }

            override fun onDoubleTap(index: Int) {
                if (viewPager != null) {
                    viewPager!!.currentItem = index
                    EventBus.getDefault().post(ScrollToTopEvent(getIdByIndex(index), true))
                }
            }
        })
        pivotTitleBar!!.selectedItem = defaultIndex

        mainListFragmentAdapter = MainListFragmentAdapter({ rectF, unsplashImage, itemView ->
            val location = IntArray(2)
            tagView!!.getLocationOnScreen(location)
            if (rectF.top <= location[1] + tagView!!.height) {
                tagView!!.animate().alpha(0f).setDuration(100).start()
            }
            detailView!!.showDetailedImage(rectF, unsplashImage, itemView)
        }, supportFragmentManager)

        viewPager!!.adapter = mainListFragmentAdapter
        viewPager!!.currentItem = defaultIndex
        viewPager!!.offscreenPageLimit = 2
        viewPager!!.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                pivotTitleBar!!.selectedItem = position
                tagView!!.text = "# ${pivotTitleBar!!.selectedString}"
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

        if (!hasNavigationBar()) {
            val params = searchFAB!!.layoutParams as RelativeLayout.LayoutParams
            params.setMargins(0, 0, getDimenInPixel(24), getDimenInPixel(24))
            searchFAB!!.layoutParams = params
        }

        appBarLayout!!.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (Math.abs(verticalOffset) - appBarLayout.height == 0) {
                tagView!!.animate().alpha(1f).setDuration(300).start()
                searchFAB!!.hide()
            } else {
                tagView!!.animate().alpha(0f).setDuration(100).start()
                searchFAB!!.show()
            }
        }

        tagView!!.setOnClickListener { EventBus.getDefault().post(ScrollToTopEvent(getIdByIndex(pivotTitleBar!!.selectedItem), false)) }
    }

    private fun handleShortcutsAction() {
        if (handleShortcut) {
            return
        }
        val action = intent.action
        if (action != null) {
            when (action) {
                "action.search" -> {
                    handleShortcut = true
                    appBarLayout!!.post { toggleSearchView(true, false) }
                }
                "action.download" -> {
                    val intent = Intent(this, ManageDownloadActivity::class.java)
                    startActivity(intent)
                }
                "action.random" -> {
                    handleShortcut = true
                    defaultIndex = 2
                }
            }
        }
    }

    override fun onShowing() {
        searchFAB!!.hide()
    }

    override fun onHiding() {
    }

    override fun onShown() {
    }

    override fun onHidden() {
        searchFAB!!.show()
        if (appBarLayout!!.height - Math.abs(appBarLayout!!.top) < 0.01) {
            tagView!!.animate().alpha(1f).setDuration(300).start()
        }
    }

    override fun onBackPressed() {
        if (searchView!!.visibility == View.VISIBLE) {
            if (searchView!!.tryHide()) {
                return
            }
            toggleSearchView(false, true)
            return
        }
        if (detailView!!.tryHide()) {
            return
        }
        super.onBackPressed()
    }
}
