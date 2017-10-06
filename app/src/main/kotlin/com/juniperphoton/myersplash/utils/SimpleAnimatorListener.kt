package com.juniperphoton.myersplash.utils

import android.animation.Animator

@Suppress("unused")
open class SimpleAnimatorListener: Animator.AnimatorListener{
    override fun onAnimationRepeat(animation: Animator?) = Unit

    override fun onAnimationEnd(animation: Animator?) = Unit

    override fun onAnimationCancel(animation: Animator?) = Unit

    override fun onAnimationStart(animation: Animator?) = Unit
}