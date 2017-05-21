package com.juniperphoton.myersplash.extension

fun Number.pow(): Double {
    return Math.pow(this.toDouble(), 2.0)
}