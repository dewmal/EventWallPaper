package com.juniperphoton.myersplash.activity;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.juniperphoton.myersplash.R;
import com.juniperphoton.myersplash.adapter.MainListFragmentAdapter;
import com.juniperphoton.myersplash.callback.DetailViewNavigationCallback;
import com.juniperphoton.myersplash.callback.OnClickSearchCallback;
import com.juniperphoton.myersplash.common.Constant;
import com.juniperphoton.myersplash.common.RandomIntentStatus;
import com.juniperphoton.myersplash.event.ScrollToTopEvent;
import com.juniperphoton.myersplash.fragment.MainListFragment;
import com.juniperphoton.myersplash.model.UnsplashCategory;
import com.juniperphoton.myersplash.model.UnsplashImage;
import com.juniperphoton.myersplash.utils.DeviceUtil;
import com.juniperphoton.myersplash.utils.LocalSettingHelper;
import com.juniperphoton.myersplash.utils.RequestUtil;
import com.juniperphoton.myersplash.widget.ImageDetailView;
import com.juniperphoton.myersplash.widget.PivotTitleBar;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.juniperphoton.myersplash.utils.DisplayUtil.getDimenInPixel;

@SuppressWarnings("UnusedDeclaration")
public class MainActivity extends BaseActivity implements DetailViewNavigationCallback,
        OnClickSearchCallback, MainListFragment.Callback {

    private static final String TAG = MainActivity.class.getName();

    private static final int SEARCH_ID = -10000;

    @BindView(R.id.pivot_title_bar)
    PivotTitleBar mPivotTitleBar;

    @SuppressWarnings("UnusedDeclaration")
    @BindView(R.id.toolbar_layout)
    AppBarLayout mAppBarLayout;

    @SuppressWarnings("UnusedDeclaration")
    @BindView(R.id.activity_main_cl)
    CoordinatorLayout mCoordinatorLayout;

    @BindView(R.id.content_activity_search_fab)
    FloatingActionButton mSearchFAB;

    @BindView(R.id.activity_main_detail_view)
    ImageDetailView mDetailView;

    @BindView(R.id.activity_main_search_view)
    com.juniperphoton.myersplash.widget.SearchView mSearchView;

    @BindView(R.id.view_pager)
    ViewPager mViewPager;

    private MainListFragmentAdapter mMainListFragmentAdapter;

    private String mQuery;
    private int mRandomIntentStatus = RandomIntentStatus.NOT_RECEIVED;
    private boolean mHandledSearch = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initMainViews();
        handleShortcutsAction();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean scrollBar = LocalSettingHelper.getBoolean(this, Constant.SCROLL_TOOLBAR, true);
        AppBarLayout.LayoutParams params =
                (AppBarLayout.LayoutParams) mPivotTitleBar.getLayoutParams();
        if (scrollBar) {
            params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                    | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
        } else {
            params.setScrollFlags(0);
        }
        mPivotTitleBar.setLayoutParams(params);

        RequestUtil.checkAndRequest(MainActivity.this);
    }

    @OnClick(R.id.content_activity_search_fab)
    void onClickSearchFAB() {
        mSearchFAB.hide();
        mSearchView.toggleAnimation(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mDetailView.deleteShareFileInDelay();
    }

    private int getIdByIndex(int index) {
        switch (index) {
            case 0:
                return UnsplashCategory.FEATURED_CATEGORY_ID;
            case 1:
                return UnsplashCategory.NEW_CATEGORY_ID;
            case 2:
                return UnsplashCategory.RANDOM_CATEOGORY_ID;
            default:
                return UnsplashCategory.NEW_CATEGORY_ID;
        }
    }

    private void initMainViews() {
        mDetailView.setNavigationCallback(this);
        mSearchView.setSearchCallback(this);
        mPivotTitleBar.setOnClickTitleListener(new PivotTitleBar.OnClickTitleListener() {
            @Override
            public void onSingleTap(int index) {
                if (mViewPager != null) {
                    mViewPager.setCurrentItem(index);
                    EventBus.getDefault().post(new ScrollToTopEvent(getIdByIndex(index), false));
                }
            }

            @Override
            public void onDoubleTap(int index) {
                if (mViewPager != null) {
                    mViewPager.setCurrentItem(index);
                    EventBus.getDefault().post(new ScrollToTopEvent(getIdByIndex(index), true));
                }
            }
        });

        mMainListFragmentAdapter = new MainListFragmentAdapter(this, getSupportFragmentManager());
        mViewPager.setAdapter(mMainListFragmentAdapter);
        mViewPager.setCurrentItem(1);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mPivotTitleBar.setSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        if (!DeviceUtil.hasNavigationBar(this)) {
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mSearchFAB.getLayoutParams();
            params.setMargins(0, 0, getDimenInPixel(24, this), getDimenInPixel(24, this));
            mSearchFAB.setLayoutParams(params);
        }
    }

    private void handleShortcutsAction() {
        String action = getIntent().getAction();
        if (action != null) {
            switch (action) {
                case "action.search": {
                    if (!mHandledSearch) {
                        mHandledSearch = true;
                        onClickSearchFAB();
                    }
                }
                break;

                case "action.random": {
                    if (mRandomIntentStatus == RandomIntentStatus.NOT_RECEIVED) {
                        mRandomIntentStatus = RandomIntentStatus.PENDING;
                    }
                }
                break;
            }
        }
    }

    private void toggleToolbarAnimation(boolean show) {
        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setDuration(300);
        valueAnimator.setInterpolator(new DecelerateInterpolator(1.5f));
        if (show) {
            valueAnimator.setIntValues(200, 0);
        } else {
            valueAnimator.setIntValues(0, 200);
        }
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //mAppBarLayout.scrollTo(0, (int) animation.getAnimatedValue());
            }
        });
        valueAnimator.start();
    }

    @Override
    public void onShow() {
        mSearchFAB.hide();
    }

    @Override
    public void onHide() {

    }

    @Override
    public void onShown() {

    }

    @Override
    public void onHidden() {
        mSearchFAB.show();
    }

    @Override
    public void onClickSearch(String keyword) {
//        mCurrentRequestPage = 1;
//        mAdapter = null;
//        mUrl = CloudService.SEARCH_URL;
//        mQuery = keyword;
//        mRefreshLayout.setRefreshing(true);
//
//        //// TODO: 2/24/2017
//        //mToolbar.setTitle(keyword.toUpperCase());
//        mContentRecyclerView.setAdapter(null);
//
//        mSearchFAB.show();
//        loadPhotoList();
    }

    @Override
    public void onBackPressed() {
        if (mSearchView.getShown()) {
            mSearchView.hide();
            mSearchFAB.show();
            return;
        }
        if (mDetailView.tryHide()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onScrollHide() {
        mSearchFAB.hide();
        toggleToolbarAnimation(false);
    }

    @Override
    public void onScrollShow() {
        mSearchFAB.show();
        toggleToolbarAnimation(true);
    }

    @Override
    public void clickPhotoItem(RectF rectF, UnsplashImage unsplashImage, View itemView) {
        mDetailView.clickPhotoItem(rectF, unsplashImage, itemView);
    }
}
