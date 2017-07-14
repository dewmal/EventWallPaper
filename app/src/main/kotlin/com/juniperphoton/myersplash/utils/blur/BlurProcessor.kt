package com.juniperphoton.myersplash.utils.blur

import android.graphics.Bitmap
import android.graphics.Canvas
import com.facebook.imagepipeline.request.BasePostprocessor

class BlurProcessor(private val radius: Int = DEFALUT_RADIUS) : BasePostprocessor() {
    companion object {
        private const val DEFAULT_SCALE = 0.2f
        private const val DEFALUT_RADIUS = 20
    }

    override fun process(bitmap: Bitmap) {
        if (radius <= 1) {
            return
        }
        val bm = BlurUtil.fastBlur(bitmap, DEFAULT_SCALE, radius)
        val c = Canvas(bitmap)
        c.drawBitmap(bm, 0f, 0f, null)
    }
}