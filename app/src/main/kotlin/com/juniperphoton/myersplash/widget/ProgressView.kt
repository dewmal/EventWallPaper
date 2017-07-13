package com.juniperphoton.myersplash.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

import com.juniperphoton.myersplash.R

class ProgressView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    var color: Int = 0
    var progress: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.CustomProgressView, 0, 0)
        color = array.getInt(R.styleable.CustomProgressView_background_color, Color.WHITE)
        progress = array.getInt(R.styleable.CustomProgressView_custom_progress, 0)
        array.recycle()
    }

    fun animateProgressTo(progress: Int) {
        val animator = ValueAnimator.ofInt(this.progress, progress)
        animator.duration = 300
        animator.addUpdateListener { valueAnimator -> this.progress = (valueAnimator.animatedValue as Int) }
        animator.start()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        val paint = Paint()
        paint.color = color

        val width = width
        val progressWidth = (width * (progress / 100.0)).toInt()

        canvas.drawRect(0f, 0f, progressWidth.toFloat(), height.toFloat(), paint)
    }
}
