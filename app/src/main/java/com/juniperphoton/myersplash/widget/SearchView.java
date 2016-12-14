package com.juniperphoton.myersplash.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.juniperphoton.myersplash.R;
import com.juniperphoton.myersplash.callback.OnClickSearchCallback;
import com.juniperphoton.myersplash.utils.DisplayUtil;
import com.juniperphoton.myersplash.utils.ToastService;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

@SuppressWarnings("UnusedDeclaration")
public class SearchView extends FrameLayout {

    private Context mContext;
    private OnClickSearchCallback mSearchCallback;
    private boolean mShown = false;

    @BindView(R.id.detail_search_et)
    EditText mEditText;

    @BindView(R.id.detail_search_bar_rl)
    RelativeLayout mSearchBarRL;

    @BindView(R.id.detail_search_root_rl)
    RelativeLayout mRootRL;

    public SearchView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;

        LayoutInflater.from(mContext).inflate(R.layout.detail_search, this);

        ButterKnife.bind(this);

        mSearchBarRL.setTranslationY(-getOffsetY());
        mRootRL.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        mEditText.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    onClickSearch();
                    return true;
                }
                return false;

            }
        });
        this.setVisibility(INVISIBLE);
    }

    public void setSearchCallback(OnClickSearchCallback callback) {
        mSearchCallback = callback;
    }

    public void toggleAnimation(final boolean show) {
        mShown = show;
        this.setVisibility(VISIBLE);

        ValueAnimator backgroundAnimator = ValueAnimator.ofArgb(show ? Color.TRANSPARENT : ContextCompat.getColor(mContext, R.color.MaskColor),
                show ? ContextCompat.getColor(mContext, R.color.MaskColor) : Color.TRANSPARENT);
        backgroundAnimator.setDuration(300);
        backgroundAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mRootRL.setBackground(new ColorDrawable((int) animation.getAnimatedValue()));
            }
        });
        backgroundAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!show) {
                    SearchView.this.setVisibility(INVISIBLE);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        backgroundAnimator.start();

        ValueAnimator offsetAnimator = ValueAnimator.ofFloat(show ? -getOffsetY() : 0f, show ? 0f : -getOffsetY());
        offsetAnimator.setDuration(300);
        offsetAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        offsetAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mSearchBarRL.setTranslationY((float) animation.getAnimatedValue());
            }
        });
        offsetAnimator.start();
        if (show) {
            new Handler().postAtTime(new Runnable() {
                @Override
                public void run() {
                    mEditText.requestFocus();
                    InputMethodManager inputMethodManager = (InputMethodManager) mContext.
                            getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.showSoftInput(mEditText, 0);
                }
            }, 200);
        }
    }

    public boolean getShown() {
        return mShown;
    }

    private float getOffsetY() {
        return DisplayUtil.getDimenInPixel(180, mContext);
    }

    public void hide(){
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
        toggleAnimation(false);
        mEditText.setText("");
    }

    @OnClick(R.id.detail_search_btn_rl)
    void onClickSearch() {
        if (mEditText.getText().toString().equals("")) {
            ToastService.sendShortToast("Input the keyword to search.");
            return;
        }
        if (mSearchCallback != null) {
            mSearchCallback.onClickSearch(mEditText.getText().toString());
            hide();
        }
    }
}
