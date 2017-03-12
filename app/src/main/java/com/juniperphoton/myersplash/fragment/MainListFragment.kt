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

import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.adapter.PhotoAdapter
import com.juniperphoton.myersplash.callback.OnClickQuickDownloadCallback
import com.juniperphoton.myersplash.callback.OnLoadMoreListener
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

import butterknife.BindView
import butterknife.ButterKnife
import rx.Subscriber

class MainListFragment : Fragment(), OnLoadMoreListener, OnClickQuickDownloadCallback {
    private var mAdapter: PhotoAdapter? = null

    @BindView(R.id.content_activity_rv)
    @JvmField var mContentRecyclerView: RecyclerView? = null

    @BindView(R.id.content_activity_srl)
    @JvmField var mRefreshLayout: SwipeRefreshLayout? = null

    @BindView(R.id.no_item_layout)
    @JvmField var mNoItemLayout: LinearLayout? = null

    @BindView(R.id.no_item_retry_btn)
    @JvmField var mRetryBtn: View? = null

    private var mCallback: Callback? = null

    private var mCategory: UnsplashCategory? = null
    private var mNext = 1
    private var mLoadedData: Boolean = false
    private var mVisible: Boolean = false
    private var mLoadView: Boolean = false
    private var mRefreshing: Boolean = false

    private var mQuery: String? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = LayoutInflater.from(activity).inflate(R.layout.fragment_list, null, false)
        ButterKnife.bind(this, view)
        mLoadView = true
        init()
        if (mVisible && !mLoadedData) {
            loadPhotoList()
            mLoadedData = true
        }
        return view
    }

    override fun onStart() {
        super.onStart()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        Log.d(TAG, "isVisibleToUser:" + isVisibleToUser)
        mVisible = isVisibleToUser
        if (mVisible && !mLoadedData && mLoadView) {
            loadPhotoList()
            mLoadedData = true
        }
        if (mVisible) {
            register()
        } else if (EventBus.getDefault().isRegistered(this)) {
            unregister()
        }
    }

    fun scrollToTop() {
        if (mContentRecyclerView != null) {
            mContentRecyclerView!!.smoothScrollToPosition(0)
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
        if (mAdapter != null) {
            mAdapter!!.clear()
        }
    }

    fun requestRefresh() {
        if (mRefreshing) {
            return
        }
        mNext = 1
        loadPhotoList()
    }

    fun init() {
        mRefreshLayout!!.setOnRefreshListener { requestRefresh() }
        mContentRecyclerView!!.layoutManager = LinearLayoutManager(activity,
                LinearLayoutManager.VERTICAL, false)
        mContentRecyclerView!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
            }

            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 20) {
                    if (mCallback != null) {
                        mCallback!!.onScrollHide()
                    }
                } else if (dy < -20) {
                    if (mCallback != null) {
                        mCallback!!.onScrollShow()
                    }
                }
            }
        })
        mRetryBtn!!.setOnClickListener {
            updateNoItemVisibility(false)
            loadPhotoList()
        }
    }

    override fun OnLoadMore() {
        mNext++
        loadPhotoList()
    }

    override fun onClickQuickDownload(image: UnsplashImage) {
        DownloadUtil.checkAndDownload(activity, image)
    }

    fun setCategory(category: UnsplashCategory, callback: Callback) {
        mCategory = category
        mCallback = callback
    }

    private fun setImageList(unsplashImages: MutableList<UnsplashImage?>?) {
        if (mAdapter != null && mAdapter!!.firstImage != null) {
            if (mAdapter?.firstImage?.id == unsplashImages?.get(0)?.id) {
                return
            }
        }
        mAdapter = PhotoAdapter(unsplashImages, activity)
        mAdapter?.setOnLoadMoreListener(this)
        mAdapter?.setOnClickDownloadCallback(this)
        mAdapter?.setOnClickItemListener(mCallback)
        mContentRecyclerView!!.adapter = mAdapter
    }

    fun updateNoItemVisibility(show: Boolean) {
        mNoItemLayout!!.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun setSignalOfRequestEnd() {
        mRefreshLayout!!.isRefreshing = false
        mRefreshing = false
    }

    private fun loadPhotoList() {
        mRefreshing = true
        if (mNext == 1) {
            mRefreshLayout!!.isRefreshing = true
        }
        val subscriber = object : Subscriber<MutableList<UnsplashImage?>?>() {
            override fun onCompleted() {
                setSignalOfRequestEnd()
            }

            override fun onError(e: Throwable) {
                setSignalOfRequestEnd()
                ToastService.sendShortToast("Fail to send request.")
                if (mAdapter != null && mAdapter!!.itemCount > 0) {
                    updateNoItemVisibility(false)
                } else {
                    updateNoItemVisibility(true)
                }
            }

            override fun onNext(images: MutableList<UnsplashImage?>?) {
                if (mNext == 1 || mAdapter == null) {
                    setImageList(images)
                } else {
                    mAdapter?.setLoadMoreData(images)
                }
                if (mAdapter == null) {
                    updateNoItemVisibility(true)
                } else if (images?.size == 0 && mAdapter!!.itemCount == 0) {
                    updateNoItemVisibility(true)
                } else {
                    updateNoItemVisibility(false)
                }
                if (mNext == 1) {
                    ToastService.sendShortToast("Loaded :D")
                }
            }
        }

        if (mCategory == null) {
            return
        }
        when (mCategory!!.id) {
            UnsplashCategory.FEATURED_CATEGORY_ID -> CloudService.getFeaturedPhotos(subscriber, mCategory!!.requestUrl!!, mNext)
            UnsplashCategory.NEW_CATEGORY_ID -> CloudService.getPhotos(subscriber, mCategory!!.requestUrl!!, mNext)
            UnsplashCategory.RANDOM_CATEGORY_ID -> CloudService.getRandomPhotos(subscriber, mCategory!!.requestUrl!!)
            UnsplashCategory.SEARCH_ID -> CloudService.searchPhotos(subscriber, mCategory!!.requestUrl!!, mNext, mQuery!!)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: ScrollToTopEvent) {
        if (event.id == mCategory!!.id) {
            mContentRecyclerView!!.smoothScrollToPosition(0)
            if (event.refresh) {
                requestRefresh()
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: RefreshAllEvent) {
        if (mAdapter != null) {
            mAdapter!!.notifyDataSetChanged()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: RequestSearchEvent) {
        if (mCategory!!.id != UnsplashCategory.SEARCH_ID) {
            return
        }
        Log.d(TAG, "RequestSearchEvent received:" + event.query!!)
        mQuery = event.query
        requestRefresh()
    }

    interface Callback {
        fun onScrollHide()

        fun onScrollShow()

        fun clickPhotoItem(rectF: RectF, unsplashImage: UnsplashImage, itemView: View)
    }

    companion object {
        private val TAG = "MainListFragment"
    }
}
