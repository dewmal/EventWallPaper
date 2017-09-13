package com.juniperphoton.myersplash.utils

import android.util.Log

object Pasteur {
    private const val TAG_DEFAULT = "MYERSPLASH"

    private var debugMode: Boolean = false

    fun init(debug: Boolean) {
        debugMode = debug
    }

    fun d(tag: String?, string: String) {
        debug(tag, string)
    }

    fun debug(tag: String?, string: String) {
        if (debugMode) {
            Log.d(tag ?: TAG_DEFAULT, string)
        }
    }

    fun i(tag: String?, string: String) {
        i(tag, string)
    }

    fun info(tag: String?, string: String) {
        if (debugMode) {
            Log.i(tag ?: TAG_DEFAULT, string)
        }
    }

    fun w(tag: String?, string: String) {
        warn(tag, string)
    }

    fun warn(tag: String?, string: String) {
        if (debugMode) {
            Log.w(tag ?: TAG_DEFAULT, string)
        }
    }

    fun e(tag: String?, string: String) {
        error(tag, string)
    }

    fun error(tag: String?, string: String) {
        if (debugMode) {
            Log.e(tag ?: TAG_DEFAULT, string)
        }
    }
}