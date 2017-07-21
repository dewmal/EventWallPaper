package com.juniperphoton.myersplash.data

import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.cloudservice.CloudService
import com.juniperphoton.myersplash.event.ScrollToTopEvent
import com.juniperphoton.myersplash.model.UnsplashCategory
import com.juniperphoton.myersplash.model.UnsplashImage
import com.juniperphoton.myersplash.utils.Pasteur
import com.juniperphoton.myersplash.utils.ResponseObserver
import javax.inject.Inject

open class MainListPresenter : Contract.MainPresenter {
    companion object {
        const val REFRESH_PAGING = 1
        private const val TAG = "MainListPresenter"
    }

    private var next: Int = REFRESH_PAGING
    private var refreshing: Boolean = false

    @Inject
    override lateinit var category: UnsplashCategory

    @Inject
    lateinit var mainView: Contract.MainView

    override var query: String? = null

    override fun stop() {
    }

    override fun start() {
    }

    override fun search(query: String) {
        Pasteur.d(TAG, "on search:$query")
        if (!mainView.isBusyRefreshing) {
            this.query = query
            refresh()
        }
    }

    override fun onReceivedScrollToTopEvent(event: ScrollToTopEvent) {
        if (event.id == category.id) {
            mainView.scrollToTop()
            if (event.refresh) {
                refresh()
            }
        }
    }

    override fun loadMore() {
        loadPhotoList(++next)
    }

    override fun reloadList() {
        loadPhotoList(next)
    }

    override fun refresh() {
        loadPhotoList(REFRESH_PAGING)
    }

    private fun setSignalOfEnd() {
        refreshing = false
        mainView.setRefreshing(false)
    }

    private fun loadPhotoList(next: Int) {
        this.next = next
        refreshing = true
        if (next == REFRESH_PAGING) {
            mainView.setRefreshing(true)
        }
        val subscriber = object : ResponseObserver<MutableList<UnsplashImage?>?>() {
            override fun onFinish() {
                setSignalOfEnd()
            }

            override fun onError(e: Throwable) {
                mainView.showToast(R.string.failed_to_send_request)
                mainView.updateNoItemVisibility()
            }

            override fun onNext(t: MutableList<UnsplashImage?>?) {
                mainView.refreshList(t, next)
            }
        }

        category.let {
            when (it.id) {
                UnsplashCategory.FEATURED_CATEGORY_ID ->
                    CloudService.getFeaturedPhotos(subscriber, it.requestUrl!!, next)
                UnsplashCategory.NEW_CATEGORY_ID ->
                    CloudService.getPhotos(subscriber, it.requestUrl!!, next)
                UnsplashCategory.RANDOM_CATEGORY_ID ->
                    CloudService.getRandomPhotos(subscriber, it.requestUrl!!)
                UnsplashCategory.SEARCH_ID ->
                    CloudService.searchPhotos(subscriber, it.requestUrl!!, next, query!!)
            }
        }
    }
}