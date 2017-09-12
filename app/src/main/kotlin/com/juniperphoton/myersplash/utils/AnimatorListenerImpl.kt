package com.juniperphoton.myersplash.utils

import android.animation.Animator

/**
 * Handy class for implementing [Animator.AnimatorListener].
 */
abstract class AnimatorListenerImpl : Animator.AnimatorListener {
    override fun onAnimationStart(a: Animator) = Unit

    override fun onAnimationCancel(animator: Animator) = Unit

    override fun onAnimationRepeat(animator: Animator) = Unit
}