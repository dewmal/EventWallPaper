package com.juniperphoton.myersplash.activity;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.juniperphoton.myersplash.R;
import com.juniperphoton.myersplash.adapter.CategoryAdapter;
import com.juniperphoton.myersplash.adapter.PhotoAdapter;
import com.juniperphoton.myersplash.callback.INavigationDrawerCallback;
import com.juniperphoton.myersplash.callback.OnLoadMoreListener;
import com.juniperphoton.myersplash.cloudservice.CloudService;
import com.juniperphoton.myersplash.common.Constant;
import com.juniperphoton.myersplash.model.UnsplashCategory;
import com.juniperphoton.myersplash.model.UnsplashImage;
import com.juniperphoton.myersplash.utils.SnackbarUtil;
import com.juniperphoton.myersplash.utils.ToastService;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import moe.feng.material.statusbar.StatusBarCompat;
import rx.Subscriber;

public class MainActivity extends AppCompatActivity implements INavigationDrawerCallback, OnLoadMoreListener {

    @Bind(R.id.activity_drawer_rv)
    RecyclerView mDrawerRecyclerview;

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Bind(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @Bind(R.id.content_activity_rv)
    RecyclerView mContentRecyclerView;

    @Bind(R.id.content_activity_srl)
    SwipeRefreshLayout mRefreshLayout;

    @Bind(R.id.activity_main_cl)
    CoordinatorLayout mCoordinatorLayout;

    @Bind(R.id.content_activity_search_fab)
    FloatingActionButton mSearchFAB;

    private PhotoAdapter mAdapter;

    private int mCurrentPage = 1;

    private String mUrl = CloudService.baseUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StatusBarCompat.setUpActivity(this);

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

        mDrawerRecyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mContentRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
//        mContentRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//
//            }
//
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                if (dy > 20) {
//                    ToggleSearchBtnAnimation(false);
//                    ToggleToolbarAnimation(false);
//                } else if (dy < -20) {
//                    ToggleSearchBtnAnimation(true);
//                    ToggleToolbarAnimation(true);
//                }
//            }
//        });
        mToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContentRecyclerView.smoothScrollToPosition(0);
            }
        });
        getCategories();
    }

    //进行网络请求
    private void getCategories() {
        CloudService.getInstance().getCategories(new Subscriber<List<UnsplashCategory>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(List<UnsplashCategory> unsplashCategories) {
                UnsplashCategory newCategory = new UnsplashCategory();
                newCategory.setId(-1);
                newCategory.setTitle(Constant.NEW_S);
                UnsplashCategory featureCategory = new UnsplashCategory();
                featureCategory.setId(-2);
                featureCategory.setTitle(Constant.FEATURE_S);
                unsplashCategories.add(0, featureCategory);
                unsplashCategories.add(0, newCategory);
                CategoryAdapter adapter = new CategoryAdapter(unsplashCategories, MainActivity.this);
                adapter.setCallback(MainActivity.this);
                adapter.select(1);
                mDrawerRecyclerview.setAdapter(adapter);
            }
        });
    }

    @Override
    public void onBackPressed() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onSelectItem(UnsplashCategory category) {
        mToolbar.setTitle(category.getTitle().toUpperCase());
        mDrawerLayout.closeDrawer(GravityCompat.START);
        mCurrentPage = 1;
        mUrl = category.getUrl();
        mRefreshLayout.setRefreshing(true);
        loadPhotoList();
    }

    private void loadPhotoList() {
        CloudService.getInstance().getPhotos(new Subscriber<List<UnsplashImage>>() {
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
            }

            @Override
            public void onNext(List<UnsplashImage> unsplashImages) {
                if (mCurrentPage == 1 || mAdapter == null) {
                    mAdapter = new PhotoAdapter(unsplashImages, MainActivity.this);
                    mAdapter.setOnLoadMoreListener(MainActivity.this);
                    mContentRecyclerView.setAdapter(mAdapter);
                } else {
                    mAdapter.setLoadMoreData(unsplashImages);
                }
            }
        }, mUrl, mCurrentPage);
    }

    private void ToggleSearchBtnAnimation(boolean show) {
        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setDuration(300);
        valueAnimator.setInterpolator(new DecelerateInterpolator(1.5f));
        if (show) {
            valueAnimator.setIntValues(100, 0);
        } else {
            valueAnimator.setIntValues(0, 100);
        }
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mSearchFAB.scrollTo(0, (int) animation.getAnimatedValue());
            }
        });
        valueAnimator.start();
    }

    private void ToggleToolbarAnimation(boolean show) {
        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setDuration(300);
        valueAnimator.setInterpolator(new DecelerateInterpolator(1.5f));
        if (show) {
            valueAnimator.setIntValues(-100, 0);
        } else {
            valueAnimator.setIntValues(0, -100);
        }
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mToolbar.scrollTo(0, (int) animation.getAnimatedValue());
            }
        });
        valueAnimator.start();

    }

    @Override
    public void OnLoadMore() {
        ++mCurrentPage;
        loadPhotoList();
    }
}
