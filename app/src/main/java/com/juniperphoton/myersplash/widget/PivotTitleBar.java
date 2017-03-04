package com.juniperphoton.myersplash.widget;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.PopupMenu;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.juniperphoton.myersplash.R;
import com.juniperphoton.myersplash.activity.AboutActivity;
import com.juniperphoton.myersplash.activity.ManageDownloadActivity;
import com.juniperphoton.myersplash.activity.SettingsActivity;
import com.juniperphoton.myersplash.model.UnsplashCategory;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

@SuppressWarnings("unused")
public class PivotTitleBar extends FrameLayout {
    private static final int DEFAULT_SELECTED = 1;

    @BindView(R.id.more_btn)
    View mMoreBtn;

    @BindView(R.id.pivot_item_0)
    View mItem0;

    @BindView(R.id.pivot_item_1)
    View mItem1;

    @BindView(R.id.pivot_item_2)
    View mItem2;

    private int mSelectedItem = DEFAULT_SELECTED;
    private OnClickTitleListener mCallback;

    private int mTouchingViewIndex;

    private GestureDetector mGestureDetector;
    private GestureDetector.SimpleOnGestureListener mListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (mCallback == null) return true;
            mCallback.onSingleTap(mTouchingViewIndex);
            return super.onSingleTapUp(e);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (mCallback == null) return true;
            mCallback.onDoubleTap(mTouchingViewIndex);
            return super.onDoubleTap(e);
        }
    };
    private OnTouchListener mOnTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (v == mItem0) {
                mTouchingViewIndex = 0;
            } else if (v == mItem1) {
                mTouchingViewIndex = 1;
            } else if (v == mItem2) {
                mTouchingViewIndex = 2;
            }
            mGestureDetector.onTouchEvent(event);
            return true;
        }
    };

    public PivotTitleBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.pivot_layout, this, true);
        ButterKnife.bind(this);

        mGestureDetector = new GestureDetector(context, mListener);
        mItem0.setOnTouchListener(mOnTouchListener);
        mItem1.setOnTouchListener(mOnTouchListener);
        mItem2.setOnTouchListener(mOnTouchListener);
    }

    public void setOnClickTitleListener(OnClickTitleListener listener) {
        mCallback = listener;
    }

    @OnClick(R.id.pivot_item_0)
    void onClickItem0() {
        if (mCallback != null) {
            mCallback.onSingleTap(0);
        }
    }

    @OnClick(R.id.pivot_item_1)
    void onClickItem1() {
        if (mCallback != null) {
            mCallback.onSingleTap(1);
        }
    }

    @OnClick(R.id.pivot_item_2)
    void onClickItem2() {
        if (mCallback != null) {
            mCallback.onSingleTap(2);
        }
    }

    public int getSelectedItem() {
        return mSelectedItem;
    }

    public void setSelected(int index) {
        toggleAnimation(mSelectedItem, index);
        mSelectedItem = index;
    }

    public String getSelectedString(){
        switch (mSelectedItem){
            case 0:
                return UnsplashCategory.FEATURE_S.toUpperCase();
            case 1:
                return UnsplashCategory.NEW_S.toUpperCase();
            case 2:
                return UnsplashCategory.RANDOM_S.toUpperCase();
            default:
                return UnsplashCategory.NEW_S.toUpperCase();
        }
    }

    private void toggleAnimation(int prevIndex, int newIndex) {
        View preView = getViewByIndex(prevIndex);
        View nextView = getViewByIndex(newIndex);

        preView.animate().alpha(0.3f).setDuration(300).start();
        nextView.animate().alpha(1).setDuration(300).start();
    }

    private View getViewByIndex(int index) {
        View view = null;
        switch (index) {
            case 0:
                view = mItem0;
                break;
            case 1:
                view = mItem1;
                break;
            case 2:
                view = mItem2;
                break;
        }
        return view;
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.more_btn)
    void onClickMore() {
        PopupMenu popupMenu = new PopupMenu(getContext(), mMoreBtn);
        popupMenu.inflate(R.menu.main);
        popupMenu.setGravity(Gravity.RIGHT);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = null;
                switch (item.getItemId()) {
                    case R.id.menu_settings:
                        intent = new Intent(getContext(), SettingsActivity.class);
                        getContext().startActivity(intent);
                        break;
                    case R.id.menu_downloads:
                        intent = new Intent(getContext(), ManageDownloadActivity.class);
                        getContext().startActivity(intent);
                        break;
                    case R.id.menu_about:
                        intent = new Intent(getContext(), AboutActivity.class);
                        getContext().startActivity(intent);
                        break;
                }
                return true;
            }
        });
        popupMenu.show();
    }

    public interface OnClickTitleListener {
        void onSingleTap(int index);

        void onDoubleTap(int index);
    }
}
