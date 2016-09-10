package com.juniperphoton.myersplash.activity;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.reflect.TypeToken;
import com.juniperphoton.myersplash.R;
import com.juniperphoton.myersplash.adapter.CategoryAdapter;
import com.juniperphoton.myersplash.adapter.PhotoAdapter;
import com.juniperphoton.myersplash.callback.INavigationDrawerCallback;
import com.juniperphoton.myersplash.callback.OnClickPhotoCallback;
import com.juniperphoton.myersplash.callback.OnLoadMoreListener;
import com.juniperphoton.myersplash.cloudservice.CloudService;
import com.juniperphoton.myersplash.model.UnsplashCategory;
import com.juniperphoton.myersplash.model.UnsplashImage;
import com.juniperphoton.myersplash.utils.RequestUtil;
import com.juniperphoton.myersplash.utils.SerializerUtil;
import com.juniperphoton.myersplash.utils.ToastService;

import java.lang.reflect.Type;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import moe.feng.material.statusbar.StatusBarCompat;
import rx.Subscriber;

public class MainActivity extends AppCompatActivity implements INavigationDrawerCallback, OnLoadMoreListener, OnClickPhotoCallback {

    private static final String TAG = MainActivity.class.getName();

    @Bind(R.id.activity_drawer_rv)
    RecyclerView mDrawerRecyclerview;

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @SuppressWarnings("UnusedDeclaration")
    @Bind(R.id.toolbar_layout)
    AppBarLayout mAppBarLayout;

    @Bind(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @Bind(R.id.content_activity_rv)
    RecyclerView mContentRecyclerView;

    @Bind(R.id.content_activity_srl)
    SwipeRefreshLayout mRefreshLayout;

    @SuppressWarnings("UnusedDeclaration")
    @Bind(R.id.activity_main_cl)
    CoordinatorLayout mCoordinatorLayout;

    @Bind(R.id.content_activity_search_fab)
    FloatingActionButton mSearchFAB;

    @Bind(R.id.detail_mask_rl)
    RelativeLayout mMaskRL;

    @Bind(R.id.detail_root_rl)
    RelativeLayout mDetailRootRL;

    @Bind(R.id.detail_hero_dv)
    SimpleDraweeView mHeroDV;

    private PhotoAdapter mAdapter;

    private int mCurrentPage = 1;
    private int mSelectedCategoryID = 0;

    private String mUrl = CloudService.baseUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StatusBarCompat.setUpActivity(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        setContentView(R.layout.activity_main);

        setSupportActionBar(mToolbar);

        ButterKnife.bind(this);

        if (mDrawerLayout != null) {
            final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            mDrawerLayout.addDrawerListener(toggle);
            mDrawerLayout.post(new Runnable() {
                @Override
                public void run() {
                    toggle.syncState();
                }
            });
        }

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCurrentPage = 1;
                loadPhotoList();
            }
        });

        mDrawerRecyclerview.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        mContentRecyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));

        mContentRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 20) {
                    mSearchFAB.hide();
                    toggleToolbarAnimation(false);
                } else if (dy < -20) {
                    mSearchFAB.show();
                    toggleToolbarAnimation(true);
                }
            }
        });

        mDetailRootRL.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });

        mDetailRootRL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.toggleMaskAnimation(false);
            }
        });

        RequestUtil.check(this);
        mRefreshLayout.setRefreshing(true);
        restorePhotoList();
        getCategories();
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.toolbar)
    void onClickToolbar() {
        mContentRecyclerView.smoothScrollToPosition(0);
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.content_activity_search_fab)
    void onClickFAB() {
        ToastService.sendShortToast("Still working on this.");
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.drawer_settings_ll)
    void onClickSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.drawer_about_ll)
    void onClickAbout() {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    //进行网络请求
    private void getCategories() {
        if (restoreCategoryList()) {
            return;
        }
        CloudService.getInstance().getCategories(new Subscriber<List<UnsplashCategory>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(List<UnsplashCategory> unsplashCategories) {
                UnsplashCategory featureCategory = new UnsplashCategory();
                featureCategory.setId(UnsplashCategory.FEATURED_CATEGORY_ID);
                featureCategory.setTitle(UnsplashCategory.FEATURE_S);

                UnsplashCategory newCategory = new UnsplashCategory();
                newCategory.setId(UnsplashCategory.NEW_CATEGORY_ID);
                newCategory.setTitle(UnsplashCategory.NEW_S);

                unsplashCategories.add(0, newCategory);
                unsplashCategories.add(0, featureCategory);

                SerializerUtil.serializeToFile(MainActivity.this, unsplashCategories,
                        SerializerUtil.CATEGORY_LIST_FILE_NAME);

                setCategoryList(unsplashCategories);
            }
        });
    }

    private void setCategoryList(List<UnsplashCategory> unsplashCategories) {
        CategoryAdapter adapter = new CategoryAdapter(unsplashCategories, MainActivity.this);
        adapter.setCallback(MainActivity.this);
        adapter.select(1);
        mDrawerRecyclerview.setAdapter(adapter);
    }

    private void setCachedImageList(List<UnsplashImage> unsplashImages) {
        PhotoAdapter adapter = new PhotoAdapter(unsplashImages, MainActivity.this);
        adapter.setOnLoadMoreListener(MainActivity.this);
        adapter.setOnClickItemListener(MainActivity.this);
        mContentRecyclerView.setAdapter(adapter);
    }

    private void setImageList(List<UnsplashImage> unsplashImages) {
        mAdapter = new PhotoAdapter(unsplashImages, MainActivity.this);
        mAdapter.setOnLoadMoreListener(MainActivity.this);
        mAdapter.setOnClickItemListener(MainActivity.this);
        mContentRecyclerView.setAdapter(mAdapter);
    }

    private boolean restoreCategoryList() {
        Type type = new TypeToken<List<UnsplashCategory>>() {
        }.getType();
        List<UnsplashCategory> unsplashCategories = SerializerUtil.deSerializeFromFile(type, this, SerializerUtil.CATEGORY_LIST_FILE_NAME);
        if (unsplashCategories != null) {
            if (unsplashCategories.size() > 0) {
                Log.d(TAG, "Cached category list");
                setCategoryList(unsplashCategories);
                return true;
            }
        }
        return false;
    }

    private boolean restorePhotoList() {
        Type type = new TypeToken<List<UnsplashImage>>() {
        }.getType();
        List<UnsplashImage> unsplashCategories = SerializerUtil.deSerializeFromFile(type, this, SerializerUtil.IMAGE_LIST_FILE_NAME);
        if (unsplashCategories != null) {
            if (unsplashCategories.size() > 0) {
                Log.d(TAG, "Cached category list");
                setCachedImageList(unsplashCategories);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (mDetailRootRL.getVisibility() == View.VISIBLE) {
            toggleMaskAnimation(false);
            return;
        }
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public void onSelectItem(UnsplashCategory category) {
        mSelectedCategoryID = category.getId();
        mToolbar.setTitle(category.getTitle().toUpperCase());
        mDrawerLayout.closeDrawer(GravityCompat.START);
        mCurrentPage = 1;
        mUrl = category.getUrl();
        mRefreshLayout.setRefreshing(true);
        loadPhotoList();
    }

    private void loadPhotoList() {
        Subscriber<List<UnsplashImage>> subscriber = new Subscriber<List<UnsplashImage>>() {
            @Override
            public void onCompleted() {
                mRefreshLayout.setRefreshing(false);
                if (mCurrentPage == 1) {
                    ToastService.sendShortToast("Loaded :D");
                }
            }

            @Override
            public void onError(Throwable e) {
                mRefreshLayout.setRefreshing(false);
                ToastService.sendShortToast("Fail to send request.");
            }

            @Override
            public void onNext(List<UnsplashImage> unsplashImages) {
                if (mCurrentPage == 1 || mAdapter == null) {
                    setImageList(unsplashImages);
                    if (mSelectedCategoryID == UnsplashCategory.NEW_CATEGORY_ID) {
                        SerializerUtil.serializeToFile(MainActivity.this, unsplashImages,
                                SerializerUtil.IMAGE_LIST_FILE_NAME);
                    }
                } else {
                    mAdapter.setLoadMoreData(unsplashImages);
                }
            }
        };

        if (mSelectedCategoryID == UnsplashCategory.FEATURED_CATEGORY_ID) {
            CloudService.getInstance().getFeaturedPhotos(subscriber, mUrl, mCurrentPage);
        } else {
            CloudService.getInstance().getPhotos(subscriber, mUrl, mCurrentPage);
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
    public void OnLoadMore() {
        ++mCurrentPage;
        loadPhotoList();
    }

    @Override
    public void clickPhotoItem(final RectF rectF, final UnsplashImage unsplashImage) {
        mHeroDV.setImageURI(unsplashImage.getListUrl());
        mDetailRootRL.setVisibility(View.VISIBLE);
        toggleMaskAnimation(true);

        ViewTreeObserver viewTreeObserver = mHeroDV.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mHeroDV.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    int[] position = new int[2];
                    mHeroDV.getLocationOnScreen(position);

                    float targetY = rectF.top;
                    //mHeroDV.scrollTo(0, 0);
                    //mHeroDV.scrollBy(0, position[1] - (int) targetY);
                    MainActivity.this.toggleHeroViewAnimation((int) targetY);
                }
            });
        }
    }

    private void toggleHeroViewAnimation(int startY) {
        TranslateAnimation animation = new TranslateAnimation(0, 0, 200, 0);
        animation.setDuration(300);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        animation.setFillAfter(true);
        mHeroDV.startAnimation(animation);
    }

    private void toggleMaskAnimation(final boolean show) {
        ValueAnimator animator = new ValueAnimator();
        if (show) {
            animator.setFloatValues(0, 0.7f);
            mMaskRL.setAlpha(0f);
        } else {
            animator.setFloatValues(0.7f, 0);
            mMaskRL.setAlpha(0.7f);
        }
        animator.setDuration(300);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mMaskRL.setAlpha((float) animation.getAnimatedValue());
                if ((float) animation.getAnimatedValue() == 0f && !show) {
                    mDetailRootRL.setVisibility(View.GONE);
                }
            }
        });
        animator.start();
    }
}
