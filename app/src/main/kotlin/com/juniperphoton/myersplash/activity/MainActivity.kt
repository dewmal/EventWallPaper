package com.juniperphoton.myersplash.activity

import android.animation.Animator
import android.content.Intent
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.view.View
import android.view.ViewAnimationUtils
import android.widget.RelativeLayout
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.adapter.MainListFragmentAdapter
import com.juniperphoton.myersplash.event.ScrollToTopEvent
import com.juniperphoton.myersplash.extension.getDimenInPixel
import com.juniperphoton.myersplash.extension.hasNavigationBar
import com.juniperphoton.myersplash.extension.pow
import com.juniperphoton.myersplash.model.UnsplashCategory
import com.juniperphoton.myersplash.utils.AnimatorListenerImpl
import com.juniperphoton.myersplash.utils.FileUtil
import com.juniperphoton.myersplash.utils.PermissionUtil
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import rx.Observable
import rx.Subscriber
import rx.schedulers.Schedulers

class MainActivity : BaseActivity() {
    private var mainListFragmentAdapter: MainListFragmentAdapter? = null

    private var handleShortcut: Boolean = false
    private var initNavigationIndex = 1
    private var fabPositionX: Int = 0
    private var fabPositionY: Int = 0

    private val idMaps = mutableMapOf(
            Pair(0, UnsplashCategory.FEATURED_CATEGORY_ID),
            Pair(1, UnsplashCategory.NEW_CATEGORY_ID),
            Pair(2, UnsplashCategory.RANDOM_CATEGORY_ID))

    private val coordinateLayout by lazy {
        coordinator_layout
    }

    private val toolbarLayout by lazy {
        toolbar_layout
    }

    private val pivotTitleBar by lazy {
        pivot_title_bar
    }

    private val viewPager by lazy {
        view_pager
    }

    private val tagView by lazy {
        tag_view
    }

    private val imageDetailView by lazy {
        detail_view
    }

    private val searchFab by lazy {
        search_fab
    }

    private val searchView by lazy {
        search_view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        handleShortcutsAction()
        clearSharedFiles()
        initMainViews()
    }

    override fun onResume() {
        super.onResume()
        imageDetailView.registerEventBus()
        searchView.registerEventBus()

        PermissionUtil.checkAndRequest(this@MainActivity)
    }

    override fun onPause() {
        super.onPause()
        imageDetailView.unregisterEventBus()
        searchView.unregisterEventBus()
    }

    private fun toggleSearchView(show: Boolean, useAnimation: Boolean) {
        if (show) {
            searchFab.hide()
        } else {
            searchFab.show()
        }

        val location = IntArray(2)
        searchFab.getLocationOnScreen(location)

        if (show) {
            fabPositionX = (location[0] + searchFab.width / 2f).toInt()
            fabPositionY = (location[1] + searchFab.height / 2f).toInt()
        }

        val width = window.decorView.width
        val height = window.decorView.height

        val radius = Math.sqrt(width.pow() + height.pow()).toInt()
        val animator = ViewAnimationUtils.createCircularReveal(searchView,
                fabPositionX, fabPositionY,
                (if (show) 0 else radius).toFloat(), (if (show) radius else 0).toFloat())
        animator.addListener(object : AnimatorListenerImpl() {
            override fun onAnimationEnd(a: Animator) {
                if (!show) {
                    searchView.reset()
                    searchView.visibility = View.GONE
                } else {
                    searchView.onShown()
                }
            }
        })

        searchView.visibility = View.VISIBLE

        if (show) {
            searchView.tryShowKeyboard()
            searchView.onShowing()
        } else {
            searchView.onHiding()
        }
        if (useAnimation) {
            animator.start()
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
        imageDetailView.apply {
            onShowing = {
                searchFab.hide()
            }
            onHidden = {
                searchFab.show()
                if (toolbarLayout.height - Math.abs(toolbarLayout.top) < 0.01) {
                    tagView.animate().alpha(1f).setDuration(300).start()
                }
            }
        }

        searchFab.setOnClickListener {
            toggleSearchView(true, true)
        }

        pivotTitleBar.apply {
            onSingleTap = {
                if (viewPager != null) {
                    viewPager.currentItem = it
                    EventBus.getDefault().post(ScrollToTopEvent(idMaps[it]!!, false))
                }
            }
            onDoubleTap = {
                if (viewPager != null) {
                    viewPager.currentItem = it
                    EventBus.getDefault().post(ScrollToTopEvent(idMaps[it]!!, true))
                }
            }
            selectedItem = initNavigationIndex
        }

        mainListFragmentAdapter = MainListFragmentAdapter({ rectF, unsplashImage, itemView ->
            val location = IntArray(2)
            tagView.getLocationOnScreen(location)
            if (rectF.top <= location[1] + tagView.height) {
                tagView.animate().alpha(0f).setDuration(100).start()
            }
            imageDetailView.showDetailedImage(rectF, unsplashImage, itemView)
        }, supportFragmentManager)

        viewPager.apply {
            adapter = mainListFragmentAdapter
            currentItem = initNavigationIndex
            offscreenPageLimit = 2
            addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

                override fun onPageSelected(position: Int) {
                    pivotTitleBar.selectedItem = position
                    tagView.text = "# ${pivotTitleBar.selectedString}"
                }

                override fun onPageScrollStateChanged(state: Int) {
                }
            })
        }

        if (!hasNavigationBar()) {
            val params = searchFab.layoutParams as RelativeLayout.LayoutParams
            params.setMargins(0, 0, getDimenInPixel(24), getDimenInPixel(24))
            searchFab.layoutParams = params
        }

        toolbarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (Math.abs(verticalOffset) - appBarLayout.height == 0) {
                tagView.animate().alpha(1f).setDuration(300).start()
                searchFab.hide()
            } else {
                tagView.animate().alpha(0f).setDuration(100).start()
                searchFab.show()
            }
        }

        tagView.setOnClickListener { EventBus.getDefault().post(ScrollToTopEvent(idMaps[pivotTitleBar.selectedItem]!!, false)) }
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
                    toolbarLayout.post { toggleSearchView(true, false) }
                }
                "action.download" -> {
                    val intent = Intent(this, ManageDownloadActivity::class.java)
                    startActivity(intent)
                }
                "action.random" -> {
                    handleShortcut = true
                    initNavigationIndex = 2
                }
            }
        }
    }

    override fun onBackPressed() {
        if (searchView.visibility == View.VISIBLE) {
            if (searchView.tryHide()) {
                return
            }
            toggleSearchView(false, true)
            return
        }
        if (imageDetailView.tryHide()) {
            return
        }
        super.onBackPressed()
    }
}
