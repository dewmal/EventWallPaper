package com.juniperphoton.myersplash

import com.juniperphoton.myersplash.event.ScrollToTopEvent
import com.juniperphoton.myersplash.model.UnsplashCategory
import com.juniperphoton.myersplash.model.UnsplashImage

interface Contract {
    interface MainPresenter {
        var category: UnsplashCategory?
        var query: String?

        fun start()
        fun stop()
        fun refresh()
        fun reloadList()
        fun loadMore()
        fun search(query: String)
        fun onReceivedScrollToTopEvent(event: ScrollToTopEvent)
    }

    interface MainView {
        val isBusyRefreshing: Boolean

        fun setPresenter(presenter: MainPresenter)
        fun search(query: String)
        fun refreshList(images: MutableList<UnsplashImage?>?, next: Int)
        fun clearData()
        fun setRefreshing(refreshing: Boolean)
        fun updateNoItemVisibility()
        fun scrollToTop()
        fun showToast(text: String)
        fun showToast(textId: Int)
        fun registerEvent()
        fun unregisterEvent()
    }
}