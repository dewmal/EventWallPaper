package com.juniperphoton.myersplash.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.extension.use

class RingProgressView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    companion object {
        private const val STROKE_WIDTH = 5
        private const val INIT_PROGRESS_VALUE = 10
        private const val INTERVAL_PROGRESS_VALUE = 5
    }

    private val paint: Paint = Paint()

    var progress = INIT_PROGRESS_VALUE
        set(value) {
            var finalValue = value
            if (finalValue < INTERVAL_PROGRESS_VALUE) {
                finalValue = INTERVAL_PROGRESS_VALUE
            }
            field = finalValue
            invalidate()
        }

    var color = Color.WHITE
        set(value) {
            field = value
            invalidate()
        }

    private val rect: RectF = RectF()

    init {
        paint.color = color
        paint.strokeWidth = STROKE_WIDTH.toFloat()
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND

        context.obtainStyledAttributes(attrs, R.styleable.CustomProgressView).use {
            progress = getInt(R.styleable.CustomProgressView_custom_progress, 0)
        }
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
}

