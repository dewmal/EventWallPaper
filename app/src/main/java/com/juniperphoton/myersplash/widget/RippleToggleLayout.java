package com.juniperphoton.myersplash.widget;

import android.animation.Animator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.RelativeLayout;

import com.juniperphoton.myersplash.utils.AnimatorListenerImpl;

import java.util.List;

import butterknife.ButterKnife;

public class RippleToggleLayout extends RelativeLayout {

    public static String TAG = RippleToggleLayout.class.getName();

    private int mDisplayIndex = -1;

    public RippleToggleLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        ButterKnife.bind(this);
    }

    public RippleToggleLayout(Context context, AttributeSet attrs, View... views) {
        this(context, attrs);
        addViews(views);
    }

    private void toggleInternal(boolean animated) {
        final View view = getChildAt(mDisplayIndex);
        if (animated) {
            view.post(new Runnable() {
                @Override
                public void run() {
                    final Animator anim = ViewAnimationUtils.createCircularReveal(view, getWidth() / 2, getHeight() / 2, 0, getWidth());
                    anim.addListener(new AnimatorListenerImpl() {
                        @Override
                        public void onAnimationEnd(Animator animator) {
                            for (int i = 0; i < getChildCount(); i++) {
                                View v = getChildAt(i);
                                if (i != mDisplayIndex) {
                                    v.setVisibility(GONE);
                                }
                            }
                        }
                    });
                    anim.setDuration(400);
                    view.setVisibility(VISIBLE);
                    anim.start();
                }
            });
        } else {
            view.setVisibility(VISIBLE);
        }
    }

    public void addView(View view) {
        view.setVisibility(GONE);
        super.addView(view);
    }

    public void addViews(View... views) {
        for (View view : views) {
            addView(view);
        }
    }

    public void toggleTo(int index) {
        if (index < 0 || index > getChildCount()) {
            throw new IndexOutOfBoundsException();
        }
        final boolean animated = mDisplayIndex != -1;
        if (mDisplayIndex == index) {
            return;
        }
        for (int i = 0; i < getChildCount(); i++) {
            if (mDisplayIndex != i) {
                View childView = getChildAt(i);
                childView.setVisibility(GONE);
            }
        }
        mDisplayIndex = index;
        toggleInternal(animated);
    }

    public void setDefaultView(int index) {
        if (index < 0 || index > getChildCount()) {
            throw new IndexOutOfBoundsException();
        }
        mDisplayIndex = index;
        View view = getChildAt(index);
        if (view != null) {
            view.setVisibility(VISIBLE);
        }
    }

    public void toggleUp() {
        int newIndex = mDisplayIndex++;
        if (newIndex == getChildCount() - 1) {
            newIndex = 0;
        }
        toggleTo(newIndex);
    }

    public void toggleDown() {
        int newIndex = mDisplayIndex--;
        if (newIndex < 0) {
            newIndex = getChildCount() - 1;
        }
        toggleTo(newIndex);
    }
}
