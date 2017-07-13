package com.juniperphoton.myersplash.fragment

import android.graphics.RectF
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import butterknife.BindView
import butterknife.ButterKnife
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.adapter.PhotoAdapter
import com.juniperphoton.myersplash.cloudservice.CloudService
import com.juniperphoton.myersplash.event.RefreshAllEvent
import com.juniperphoton.myersplash.event.RequestSearchEvent
import com.juniperphoton.myersplash.event.ScrollToTopEvent
import com.juniperphoton.myersplash.model.UnsplashCategory
import com.juniperphoton.myersplash.model.UnsplashImage
import com.juniperphoton.myersplash.utils.DownloadUtil
import com.juniperphoton.myersplash.utils.ToastService
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import rx.Subscriber

@Suppress("UNUSED", "UNUSED_PARAMETER")
class MainListFragment : Fragment() {
    companion object {
        private val TAG = "MainListFragment"
    }

    private var adapter: PhotoAdapter? = null

    @BindView(R.id.content_activity_rv)
    @JvmField var contentRecyclerView: RecyclerView? = null

    @BindView(R.id.content_activity_srl)
    @JvmField var refreshLayout: SwipeRefreshLayout? = null

    @BindView(R.id.no_item_layout)
    @JvmField var noItemLayout: LinearLayout? = null

    @BindView(R.id.no_item_retry_btn)
    @JvmField var retryBtn: View? = null

    var onScrollHide: (() -> Unit)? = null
    var onScrollShow: (() -> Unit)? = null
    var onClickPhotoItem: ((rectF: RectF, unsplashImage: UnsplashImage, itemView: View) -> Unit)? = null

    private var category: UnsplashCategory? = null
    private var next = 1
    private var loadedData: Boolean = false
    private var visible: Boolean = false
    private var loadView: Boolean = false
    private var refreshing: Boolean = false

    private var query: String? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = LayoutInflater.from(activity!!).inflate(R.layout.fragment_list, null, false)
        ButterKnife.bind(this, view)
        loadView = true
        init()
        if (visible && !loadedData) {
            loadPhotoList()
            loadedData = true
        }
        return view
    }

    override fun onStart() {
        super.onStart()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        Log.d(TAG, "isVisibleToUser:" + isVisibleToUser)
        visible = isVisibleToUser
        if (visible && !loadedData && loadView) {
            loadPhotoList()
            loadedData = true
        }
        if (visible) {
            register()
        } else if (EventBus.getDefault().isRegistered(this)) {
            unregister()
        }
    }

    fun scrollToTop() {
        if (contentRecyclerView != null) {
            contentRecyclerView!!.smoothScrollToPosition(0)
        }
    }

    fun register() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    fun unregister() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    fun clear() {
        if (adapter != null) {
            adapter!!.clear()
        }
    }

    fun requestRefresh() {
        if (refreshing) {
            return
        }
        next = 1
        loadPhotoList()
    }

    fun init() {
        refreshLayout!!.setOnRefreshListener { requestRefresh() }
        contentRecyclerView!!.layoutManager = LinearLayoutManager(activity,
                LinearLayoutManager.VERTICAL, false)
        contentRecyclerView!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(list: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(list, newState)
            }

            override fun onScrolled(list: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(list, dx, dy)
                if (dy > 20) {
                    onScrollHide?.invoke()
                } else if (dy < -20) {
                    onScrollShow?.invoke()
                }
            }
        })
        retryBtn!!.setOnClickListener {
            updateNoItemVisibility(false)
            loadPhotoList()
        }
    }

    fun setCategory(category: UnsplashCategory, callback: ((rectF: RectF, unsplashImage: UnsplashImage, itemView: View) -> Unit)?) {
        this.category = category
        this.onClickPhotoItem = callback
    }

    private fun setImageList(unsplashImages: MutableList<UnsplashImage?>?) {
        if (adapter != null && adapter!!.firstImage != null) {
            if (adapter?.firstImage?.id == unsplashImages?.get(0)?.id) {
                return
            }
        }
        adapter = PhotoAdapter(unsplashImages, activity)
        adapter?.onLoadMore = {
            next++
            loadPhotoList()
        }
        adapter?.onClickQuickDownload = { image ->
            DownloadUtil.checkAndDownload(activity, image)
        }
        adapter?.onClickPhoto = onClickPhotoItem
        contentRecyclerView!!.adapter = adapter
    }

    fun updateNoItemVisibility(show: Boolean) {
        noItemLayout!!.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun setSignalOfRequestEnd() {
        refreshLayout!!.isRefreshing = false
        refreshing = false
    }

    private fun loadPhotoList() {
        refreshing = true
        if (next == 1) {
            refreshLayout!!.isRefreshing = true
        }
        val subscriber = object : Subscriber<MutableList<UnsplashImage?>?>() {
            override fun onCompleted() {
                setSignalOfRequestEnd()
            }

            override fun onError(e: Throwable) {
                setSignalOfRequestEnd()
                ToastService.sendShortToast("Fail to send request.")
                if (adapter != null && adapter!!.itemCount > 0) {
                    updateNoItemVisibility(false)
                } else {
                    updateNoItemVisibility(true)
                }
            }

            override fun onNext(images: MutableList<UnsplashImage?>?) {
                if (next == 1 || adapter == null) {
                    setImageList(images)
                } else {
                    adapter?.setLoadMoreData(images)
                }
                if (adapter == null) {
                    updateNoItemVisibility(true)
                } else if (images?.size == 0 && adapter!!.itemCount == 0) {
                    updateNoItemVisibility(true)
                } else {
                    updateNoItemVisibility(false)
                }
                if (next == 1) {
                    ToastService.sendShortToast("Loaded :D")
                }
            }
        }

        if (category == null) {
            return
        }
        when (category!!.id) {
            UnsplashCategory.FEATURED_CATEGORY_ID -> CloudService.getFeaturedPhotos(subscriber, category!!.requestUrl!!, next)
            UnsplashCategory.NEW_CATEGORY_ID -> CloudService.getPhotos(subscriber, category!!.requestUrl!!, next)
            UnsplashCategory.RANDOM_CATEGORY_ID -> CloudService.getRandomPhotos(subscriber, category!!.requestUrl!!)
            UnsplashCategory.SEARCH_ID -> CloudService.searchPhotos(subscriber, category!!.requestUrl!!, next, query!!)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: ScrollToTopEvent) {
        if (event.id == category!!.id) {
            contentRecyclerView!!.smoothScrollToPosition(0)
            if (event.refresh) {
                requestRefresh()
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: RefreshAllEvent) {
        if (adapter != null) {
            adapter!!.notifyDataSetChanged()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: RequestSearchEvent) {
        if (category!!.id != UnsplashCategory.SEARCH_ID) {
            return
        }
        Log.d(TAG, "RequestSearchEvent received:" + event.query!!)
        query = event.query
        requestRefresh()
    }
}
