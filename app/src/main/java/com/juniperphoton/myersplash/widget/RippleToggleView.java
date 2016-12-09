package com.juniperphoton.myersplash.widget;

import android.animation.Animator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.juniperphoton.myersplash.R;
import com.juniperphoton.myersplash.utils.AnimatorListenerImpl;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class RippleToggleView extends FrameLayout {

    public static String TAG = RippleToggleView.class.getName();

    private int mDisplayIndex = -1;

    @Bind(R.id.ripple_toggle_view_root_rl)
    RelativeLayout mRootRL;

    public RippleToggleView(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater.from(context).inflate(R.layout.widget_ripple_toggle_view, this);
        ButterKnife.bind(this);
    }

    public RippleToggleView(Context context, AttributeSet attrs, View... views) {
        this(context, attrs);
        addViews(views);
    }

    private void toggleInternal() {
        final View view = mRootRL.getChildAt(mDisplayIndex);
        view.setVisibility(VISIBLE);
        view.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                final Animator anim = ViewAnimationUtils.createCircularReveal(view, getWidth() / 2, getHeight() / 2, 0, getWidth());
                anim.addListener(new AnimatorListenerImpl() {
                    @Override
                    public void onAnimationEnd(Animator animator) {
                        for (int i = 0; i < mRootRL.getChildCount(); i++) {
                            View v = mRootRL.getChildAt(i);
                            if (i != mDisplayIndex) {
                                v.setVisibility(GONE);
                            }
                        }
                    }
                });
                anim.start();
            }
        });
//        view.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                final Animator anim = ViewAnimationUtils.createCircularReveal(view, getWidth() / 2, getHeight() / 2, 0, getWidth());
//                anim.addListener(new AnimatorListenerImpl() {
//                    @Override
//                    public void onAnimationEnd(Animator animator) {
//                        for (int i = 0; i < mRootRL.getChildCount(); i++) {
//                            View v = mRootRL.getChildAt(i);
//                            if (i != mDisplayIndex) {
//                                v.setVisibility(GONE);
//                            }
//                        }
//                    }
//                });
//                anim.start();
//            }
//        }, 1000);
    }

    public void addView(View view) {
        view.setVisibility(GONE);
        mRootRL.addView(view);
    }

    public void addViews(View... views) {
        for (View view : views) {
            addView(view);
        }
    }

    public void addViews(List<View> views) {
        for (View view : views) {
            addView(view);
        }
    }

    public void toggleTo(int index) {
        if (index < 0 || index > mRootRL.getChildCount()){
            throw new IndexOutOfBoundsException();
        }
        if (mDisplayIndex == index) {
            return;
        }
        mDisplayIndex = index;
        toggleInternal();
    }

    public void setDefaultView(int index) {
        if (index < 0 || index > mRootRL.getChildCount()) {
            throw new IndexOutOfBoundsException();
        }
        mDisplayIndex = index;
        View view = mRootRL.getChildAt(index);
        if (view != null) {
            view.setVisibility(VISIBLE);
        }
    }

    public void toggleUp() {
        int newIndex = mDisplayIndex++;
        if (newIndex == mRootRL.getChildCount() - 1) {
            newIndex = 0;
        }
        toggleTo(newIndex);
    }

    public void toggleDown() {
        int newIndex = mDisplayIndex--;
        if (newIndex < 0) {
            newIndex = mRootRL.getChildCount() - 1;
        }
        toggleTo(newIndex);
    }
}
