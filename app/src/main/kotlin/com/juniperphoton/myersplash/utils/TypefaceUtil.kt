package com.juniperphoton.myersplash.utils

import android.content.Context
import android.graphics.Typeface
import android.widget.TextView

object TypefaceUtil {
    const val SEGUIBI_NAME = "fonts/seguibl.ttf"

    private var typefaces: HashMap<String, Typeface> = HashMap()

    fun setTypeface(textView: TextView, path: String, ctx: Context) {
        var typeface = typefaces[path]
        if (typeface == null) {
            typeface = Typeface.createFromAsset(ctx.assets, path)
            typefaces[path] = typeface
        }
        textView.typeface = typeface
    }
}