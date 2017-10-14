package com.juniperphoton.myersplash.utils

import io.reactivex.observers.DisposableObserver

open class SimpleObserver<T> : DisposableObserver<T>() {
    override fun onComplete() = Unit

    override fun onNext(t: T) = Unit

    override fun onError(e: Throwable) {
        e.printStackTrace()
    }
}