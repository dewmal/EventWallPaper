package com.juniperphoton.myersplash.utils

import android.support.annotation.CallSuper
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.cloudservice.APIException
import rx.Subscriber
import java.net.SocketTimeoutException

open class ResponseObserver<T> : Subscriber<T>() {
    override fun onCompleted() {
        onFinish()
    }

    @CallSuper
    override fun onError(e: Throwable) {
        e.printStackTrace()
        when (e) {
            is SocketTimeoutException -> {
                ToastService.sendShortToast(R.string.timeout)
            }
            is APIException -> {
                ToastService.sendShortToast(R.string.failed_to_send_request)
            }
            else -> onErrorOccurs(e)
        }
        onFinish()
    }

    open fun onErrorOccurs(e: Throwable) = Unit

    override fun onNext(t: T) = Unit

    open fun onFinish() = Unit
}