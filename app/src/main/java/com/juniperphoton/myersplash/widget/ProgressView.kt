package com.juniperphoton.myersplash.widget

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

import com.juniperphoton.myersplash.R

class ProgressView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var mColor: Int = 0
    private var mProgress: Int = 0

    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.ProgressView, 0, 0)
        mColor = array.getInt(R.styleable.ProgressView_BackgroundColor, Color.WHITE)
        mProgress = array.getInt(R.styleable.ProgressView_Progress, 0)
        array.recycle()
    }

    fun setProgress(progress: Int) {
        mProgress = progress
        invalidate()
    }

    fun setThemeColor(color: Int) {
        mColor = color
    }

    fun animateProgressTo(progress: Int) {
        val animator = ValueAnimator.ofInt(mProgress, progress)
        animator.duration = 300
        animator.addUpdateListener { valueAnimator -> setProgress(valueAnimator.animatedValue as Int) }
        animator.start()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        val paint = Paint()
        paint.color = mColor

        val width = width
        val progressWidth = (width * (mProgress / 100.0)).toInt()

        canvas.drawRect(0f, 0f, progressWidth.toFloat(), height.toFloat(), paint)
    }
}
