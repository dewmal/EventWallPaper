package com.juniperphoton.myersplash.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.ScrollView

class NoneScrollableScrollView(ctx: Context, attrs: AttributeSet?) : ScrollView(ctx, attrs) {
    init {
        setOnTouchListener { _, _ ->
            true
        }
    }
}