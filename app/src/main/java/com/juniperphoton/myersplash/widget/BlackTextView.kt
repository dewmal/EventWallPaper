package com.juniperphoton.myersplash.widget

import android.content.Context
import android.support.v7.widget.AppCompatTextView
import android.util.AttributeSet

import com.juniperphoton.myersplash.App
import com.juniperphoton.myersplash.utils.TypefaceUtil

class BlackTextView(context: Context, attrs: AttributeSet?) : AppCompatTextView(context, attrs) {
    init {
        setTypeface()
    }

    private fun setTypeface() {
        TypefaceUtil.setTypeface(this, TypefaceUtil.SEGUIBI_NAME, App.instance)
    }
}
