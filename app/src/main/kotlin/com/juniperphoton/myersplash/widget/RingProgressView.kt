package com.juniperphoton.myersplash.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.support.annotation.ColorInt
import android.util.AttributeSet
import android.view.View

class RingProgressView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val paint: Paint = Paint()
    private var progress = 10
    private var color = Color.WHITE
    private val rect: RectF = RectF()

    init {
        paint.color = color
        paint.strokeWidth = STROKE_WIDTH.toFloat()
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
    }

    fun setProgress(value: Int) {
        progress = value
        if (progress < 5) {
            progress = 5
        }
        invalidate()
    }

    fun setColor(@ColorInt color: Int) {
        this.color = color
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val center = (width / 2f).toInt()
        val radius = (center - STROKE_WIDTH / 2f).toInt()

        paint.color = color
        rect.set((center - radius).toFloat(), (center - radius).toFloat(),
                (center + radius).toFloat(), (center + radius).toFloat())
        val angle = (360 * progress / 100f).toInt()
        canvas.drawArc(rect, -90f, angle.toFloat(), false, paint)
    }

    companion object {
        private val STROKE_WIDTH = 5
    }
}

