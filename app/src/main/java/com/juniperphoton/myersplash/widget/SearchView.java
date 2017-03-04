package com.juniperphoton.myersplash.widget;

import android.animation.Animator;
import android.content.Context;
import android.graphics.RectF;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.juniperphoton.myersplash.R;
import com.juniperphoton.myersplash.adapter.SearchTextAdapter;
import com.juniperphoton.myersplash.event.RequestSearchEvent;
import com.juniperphoton.myersplash.fragment.MainListFragment;
import com.juniperphoton.myersplash.model.UnsplashCategory;
import com.juniperphoton.myersplash.model.UnsplashImage;
import com.juniperphoton.myersplash.utils.AnimatorListenerImpl;
import com.juniperphoton.myersplash.utils.ToastService;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT;

@SuppressWarnings("UnusedDeclaration")
public class SearchView extends FrameLayout implements ViewTreeObserver.OnGlobalLayoutListener {
    private static final String TAG = "SearchView";

    private Context mContext;

    @BindView(R.id.detail_search_et)
    EditText mEditText;

    @BindView(R.id.detail_search_root_rl)
    ViewGroup mRootRL;

    @BindView(R.id.search_result_root)
    FrameLayout mResultRoot;

    @BindView(R.id.search_detail_view)
    ImageDetailView mDetailView;

    @BindView(R.id.detail_search_btn)
    View mSearchBtn;

    @BindView(R.id.detail_clear_btn)
    View mClearBtn;

    @BindView(R.id.search_tag)
    TextView mSearchTag;

    @BindView(R.id.search_toolbar_layout)
    AppBarLayout mAppBarLayout;

    @BindView(R.id.search_box)
    View mSearchBox;

    private SearchTextAdapter mAdapter;

    private static UnsplashCategory sSearchCategory = new UnsplashCategory();
    private static List<UnsplashCategory> sCategoryList = new ArrayList<>();

    static {
        sSearchCategory.setId(UnsplashCategory.SEARCH_ID);
    }

    private MainListFragment mFragment;
    private boolean mAnimating;

    public SearchView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;

        LayoutInflater.from(mContext).inflate(R.layout.search_layout, this);

        ButterKnife.bind(this);

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

        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mEditText.getText() != null && !mEditText.getText().toString().equals("")) {
                    if (mSearchBtn.getScaleX() != 1) {
                        toggleSearchButtons(true, true);
                    }
                } else {
                    if (mSearchBtn.getScaleX() != 0) {
                        //toggleSearchButtons(false, true);
                    }
                }
            }
        });

        AppCompatActivity activity = (AppCompatActivity) context;
        mFragment = new MainListFragment();
        mFragment.setCategory(sSearchCategory, new MainListFragment.Callback() {
            @Override
            public void onScrollHide() {
                //mSearchTag.animate().alpha(1f).setDuration(200).start();
            }

            @Override
            public void onScrollShow() {
                //mSearchTag.animate().alpha(0f).setDuration(100).start();
            }

            @Override
            public void clickPhotoItem(RectF rectF, UnsplashImage unsplashImage, View itemView) {
                mDetailView.showDetailedImage(rectF, unsplashImage, itemView);
            }
        });
        activity.getSupportFragmentManager().beginTransaction().replace(R.id.search_result_root, mFragment)
                .commit();

        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                Log.d(TAG, "vertical offset=" + verticalOffset + ",height:" + appBarLayout.getHeight());
                float fraction = Math.abs(verticalOffset) * 1.0f / appBarLayout.getHeight();
                mSearchTag.setAlpha(fraction);
            }
        });

        mSearchTag.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mAppBarLayout.offsetTopAndBottom(mAppBarLayout.getHeight());
                mAppBarLayout.requestLayout();
            }
        });
    }

    private void toggleSearchButtons(final boolean show, final boolean animation) {
        if (!animation) {
            mSearchBtn.setScaleX(show ? 1f : 0f);
            mSearchBtn.setScaleY(show ? 1f : 0f);
            mClearBtn.setScaleX(show ? 1f : 0f);
            mClearBtn.setScaleY(show ? 1f : 0f);
        } else {
            if (mAnimating) return;
            mAnimating = true;
            mSearchBtn.animate().scaleX(show ? 1f : 0f).scaleY(show ? 1f : 0f).setDuration(200)
                    .setStartDelay(100)
                    .setListener(new AnimatorListenerImpl() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mAnimating = false;
                        }
                    })
                    .start();
            mClearBtn.animate().scaleX(show ? 1f : 0f).scaleY(show ? 1f : 0f).setDuration(200)
                    .start();
        }
    }

//    private void initSearchRecommendation() {
//        mList.setLayoutManager(new GridLayoutManager(mContext, 3));
//        mAdapter = new SearchTextAdapter(mContext);
//        mAdapter.setData(sCategoryList);
//        mList.setAdapter(mAdapter);
//    }

    public void onShowing() {
        mFragment.register();
        toggleSearchButtons(false, false);
    }

    public void onHiding() {
        mFragment.unregister();
        hideKeyboard();
        toggleSearchButtons(false, false);
        mSearchTag.animate().alpha(0).setDuration(100).start();
    }

    public void onShown() {
        AppBarLayout.LayoutParams layoutParams = (AppBarLayout.LayoutParams) mSearchBox.getLayoutParams();
        layoutParams.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
        mSearchBox.setLayoutParams(layoutParams);
    }

    public void reset() {
        AppBarLayout.LayoutParams layoutParams = (AppBarLayout.LayoutParams) mSearchBox.getLayoutParams();
        layoutParams.setScrollFlags(0);
        mSearchBox.setLayoutParams(layoutParams);
        mFragment.scrollToTop();
        mFragment.clear();
        mEditText.setText("");
    }

    public void showKeyboard() {
        mEditText.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
    }

    @OnClick(R.id.detail_search_btn)
    void onClickSearch() {
        hideKeyboard();
        Log.d(TAG, "onClickSearch");
        if (mEditText.getText().toString().equals("")) {
            ToastService.sendShortToast("Input the keyword to search.");
            return;
        }
        mSearchTag.setText("# " + mEditText.getText().toString().toUpperCase());
        EventBus.getDefault().post(new RequestSearchEvent(mEditText.getText().toString()));
    }

    @OnClick(R.id.detail_clear_btn)
    void onClickClear() {
        mEditText.setText("");
        toggleSearchButtons(false, true);
    }

    public boolean tryHide() {
        return mDetailView.tryHide();
    }

    @Override
    public void onGlobalLayout() {
        mEditText.requestFocus();
        final InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mEditText, SHOW_IMPLICIT);
        mEditText.getViewTreeObserver().removeOnGlobalLayoutListener(this);
    }
}
