package com.juniperphoton.myersplash.activity;

import android.animation.ValueAnimator;
import android.app.SearchManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;

import com.google.gson.reflect.TypeToken;
import com.juniperphoton.myersplash.R;
import com.juniperphoton.myersplash.adapter.CategoryAdapter;
import com.juniperphoton.myersplash.adapter.PhotoAdapter;
import com.juniperphoton.myersplash.callback.DetailViewNavigationCallback;
import com.juniperphoton.myersplash.callback.INavigationDrawerCallback;
import com.juniperphoton.myersplash.callback.OnClickQuickDownloadCallback;
import com.juniperphoton.myersplash.callback.OnLoadMoreListener;
import com.juniperphoton.myersplash.cloudservice.CloudService;
import com.juniperphoton.myersplash.model.UnsplashCategory;
import com.juniperphoton.myersplash.model.UnsplashImage;
import com.juniperphoton.myersplash.utils.ColorUtil;
import com.juniperphoton.myersplash.utils.DownloadUtil;
import com.juniperphoton.myersplash.utils.SerializerUtil;
import com.juniperphoton.myersplash.utils.ToastService;
import com.juniperphoton.myersplash.view.DetailView;

import java.lang.reflect.Type;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import moe.feng.material.statusbar.StatusBarCompat;
import rx.Subscriber;

public class MainActivity extends AppCompatActivity implements INavigationDrawerCallback,
        OnLoadMoreListener, OnClickQuickDownloadCallback, DetailViewNavigationCallback {

    private static final String TAG = MainActivity.class.getName();

    @Bind(R.id.activity_drawer_rv)
    RecyclerView mDrawerRecyclerView;

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

    @Bind(R.id.activity_main_detail_view)
    DetailView mDetailView;

    private PhotoAdapter mAdapter;

    private int mCurrentPage = 1;
    private int mSelectedCategoryID = 0;

    private String mUrl = CloudService.baseUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StatusBarCompat.setUpActivity(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        initMainViews();

        mDetailView.setNavigationCallback(this);

        restorePhotoList();
        getCategories();
    }

    @Override
    protected void onResume() {
        super.onResume();
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

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.search_menu, menu);
//        // Retrieve the SearchView and plug it into SearchManager
//        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
//        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
//        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
//        return super.onCreateOptionsMenu(menu);
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mDetailView.deleteShareFileInDelay();
    }

    private void initMainViews() {
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

        mDrawerRecyclerView.setLayoutManager(new LinearLayoutManager(this,
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
    }

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
        mDrawerRecyclerView.setAdapter(adapter);
    }

    private void setCachedImageList(List<UnsplashImage> unsplashImages) {
        PhotoAdapter adapter = new PhotoAdapter(unsplashImages, MainActivity.this);
        setupCallback(adapter);
        mContentRecyclerView.setAdapter(adapter);
    }

    private void setImageList(List<UnsplashImage> unsplashImages) {
        if (mAdapter != null && mAdapter.getFirstImage() != null) {
            if (mAdapter.getFirstImage().getId().equals(unsplashImages.get(0).getId())) {
                return;
            }
        }
        mAdapter = new PhotoAdapter(unsplashImages, MainActivity.this);
        setupCallback(mAdapter);
        mContentRecyclerView.setAdapter(mAdapter);
    }

    private void setupCallback(PhotoAdapter adapter) {
        adapter.setOnLoadMoreListener(this);
        adapter.setOnClickItemListener(mDetailView);
        adapter.setOnClickDownloadCallback(this);
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
        if (mDetailView.tryHide()) {
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
    public void onClickQuickDownload(UnsplashImage image) {
        DownloadUtil.startDownloadService(this, image.getFileNameForDownload(), image.getDownloadUrl());
        ToastService.sendShortToast("Downloading...");
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
}
