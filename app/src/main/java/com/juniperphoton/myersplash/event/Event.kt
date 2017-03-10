package com.juniperphoton.myersplash.event

class DownloadStartedEvent(var id: String)

class RefreshAllEvent

class RequestSearchEvent(query: String) {
    var query: String? = null
}

class ScrollToTopEvent(val id: Int, val refresh: Boolean)