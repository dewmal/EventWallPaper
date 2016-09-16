package com.juniperphoton.myersplash.callback;

import android.graphics.RectF;
import android.view.View;

import com.juniperphoton.myersplash.model.UnsplashImage;

import java.util.Vector;

public interface OnClickPhotoCallback {
    void clickPhotoItem(RectF rectF, UnsplashImage unsplashImage,View itemView);
}
