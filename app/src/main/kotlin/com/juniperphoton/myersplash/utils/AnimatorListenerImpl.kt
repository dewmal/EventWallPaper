package com.juniperphoton.myersplash.utils

import android.animation.Animator

abstract class AnimatorListenerImpl : Animator.AnimatorListener {
    override fun onAnimationStart(a: Animator) {
    }

    override fun onAnimationCancel(animator: Animator) {
    }

    override fun onAnimationRepeat(animator: Animator) {
    }
}
