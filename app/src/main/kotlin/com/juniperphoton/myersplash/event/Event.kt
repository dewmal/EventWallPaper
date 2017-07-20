package com.juniperphoton.myersplash.event

class DownloadStartedEvent(var id: String?)

class RefreshUIEvent

class ScrollToTopEvent(val id: Int, val refresh: Boolean)