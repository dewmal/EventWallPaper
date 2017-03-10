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
    private val mPaint: Paint = Paint()
    private var mProgress = 10
    private var mColor = Color.WHITE
    private val mRect: RectF = RectF()

    init {
        mPaint.color = mColor
        mPaint.strokeWidth = STROKE_WIDTH.toFloat()
        mPaint.isAntiAlias = true
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeCap = Paint.Cap.ROUND
    }

    fun setProgress(value: Int) {
        mProgress = value
        if (mProgress < 5) {
            mProgress = 5
        }
        invalidate()
    }

    fun setColor(@ColorInt color: Int) {
        mColor = color
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val center = (width / 2f).toInt()
        val radius = (center - STROKE_WIDTH / 2f).toInt()

        mPaint.color = mColor
        mRect.set((center - radius).toFloat(), (center - radius).toFloat(),
                (center + radius).toFloat(), (center + radius).toFloat())
        val angle = (360 * mProgress / 100f).toInt()
        canvas.drawArc(mRect, -90f, angle.toFloat(), false, mPaint)
    }

    companion object {
        private val STROKE_WIDTH = 5
    }
}

