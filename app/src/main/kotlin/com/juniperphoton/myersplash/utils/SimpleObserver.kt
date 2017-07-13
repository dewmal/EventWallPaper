package com.juniperphoton.myersplash.utils

import rx.Subscriber

open class SimpleObserver<T> : Subscriber<T>() {
    override fun onNext(t: T) {
    }

    override fun onError(e: Throwable) {
        e.printStackTrace()
    }

    override fun onCompleted() {
    }
}