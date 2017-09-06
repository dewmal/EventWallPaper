package com.juniperphoton.myersplash.data

interface Contract {
    interface BasePresenter {
        fun start()
        fun stop()
    }

    interface BaseView<T> {
        var presenter: T
    }
}