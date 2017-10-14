package com.juniperphoton.myersplash.extension

import android.content.res.TypedArray

fun TypedArray.use(block: TypedArray.() -> Unit) {
    block(this)
    this.recycle()
}