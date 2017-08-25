package com.juniperphoton.myersplash.utils

import rx.Subscriber

open class SimpleObserver<T> : Subscriber<T>() {
    override fun onNext(value: T) = Unit

    override fun onError(e: Throwable) {
        e.printStackTrace()
    }

    override fun onCompleted() = Unit
}