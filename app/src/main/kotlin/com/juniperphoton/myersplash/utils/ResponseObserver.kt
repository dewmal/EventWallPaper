package com.juniperphoton.myersplash.utils

import rx.Subscriber

open class ResponseObserver<T> : Subscriber<T>() {
    override fun onCompleted() {
        onFinish()
    }

    override fun onError(e: Throwable) {
        e.printStackTrace()
        onFinish()
    }

    override fun onNext(t: T) {
    }

    open fun onFinish() {
    }
}