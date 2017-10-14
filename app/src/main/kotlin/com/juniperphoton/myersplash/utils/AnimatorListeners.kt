package com.juniperphoton.myersplash.utils

import android.animation.Animator

/**
 * Handy class for implementing [Animator.AnimatorListener].
 */
class AnimatorListeners {
    abstract class End : Animator.AnimatorListener {
        override fun onAnimationStart(a: Animator) = Unit

        override fun onAnimationCancel(animator: Animator) = Unit

        override fun onAnimationRepeat(animator: Animator) = Unit
    }

    open class Simple : Animator.AnimatorListener {
        override fun onAnimationRepeat(animation: Animator?) = Unit

        override fun onAnimationEnd(animation: Animator?) = Unit

        override fun onAnimationCancel(animation: Animator?) = Unit

        override fun onAnimationStart(animation: Animator?) = Unit
    }
}
