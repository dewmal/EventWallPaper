package com.juniperphoton.myersplash.utils

import android.graphics.drawable.Animatable
import android.widget.SeekBar
import com.facebook.drawee.controller.ControllerListener
import com.facebook.imagepipeline.image.ImageInfo


open class SimpleControllerListener : ControllerListener<ImageInfo> {
    override fun onIntermediateImageFailed(id: String?, throwable: Throwable?) {
    }

    override fun onRelease(id: String?) {
    }

    override fun onIntermediateImageSet(id: String?, imageInfo: ImageInfo?) {
    }

    override fun onSubmit(id: String?, callerContext: Any?) {
    }

    override fun onFailure(id: String?, throwable: Throwable?) {
    }

    override fun onFinalImageSet(id: String?, imageInfo: ImageInfo?, animatable: Animatable?) {
    }
}

open class SimpleOnSeekBarChangeListener : SeekBar.OnSeekBarChangeListener {
    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
    }
}