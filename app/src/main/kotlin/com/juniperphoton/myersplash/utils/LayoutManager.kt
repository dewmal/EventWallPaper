package com.juniperphoton.myersplash.utils

import android.content.Context
import android.support.v7.widget.GridLayoutManager
import com.juniperphoton.myersplash.extension.getScreenWidth

object LayoutManager {
    private const val MIN_ITEM_WIDTH = 700

    fun createGridLayoutManager(context: Context): GridLayoutManager {
        val width = context.getScreenWidth()
        val size = width / MIN_ITEM_WIDTH
        return GridLayoutManager(context, size)
    }
}