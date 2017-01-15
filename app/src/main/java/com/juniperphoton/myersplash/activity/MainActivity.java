package com.juniperphoton.myersplash.activity;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.juniperphoton.myersplash.R;
import com.juniperphoton.myersplash.adapter.CategoryAdapter;
import com.juniperphoton.myersplash.adapter.PhotoAdapter;
import com.juniperphoton.myersplash.callback.DetailViewNavigationCallback;
import com.juniperphoton.myersplash.callback.INavigationDrawerCallback;
import com.juniperphoton.myersplash.callback.OnClickQuickDownloadCallback;
import com.juniperphoton.myersplash.callback.OnClickSearchCallback;
import com.juniperphoton.myersplash.callback.OnLoadMoreListener;
import com.juniperphoton.myersplash.cloudservice.CloudService;
import com.juniperphoton.myersplash.common.Constant;
import com.juniperphoton.myersplash.common.RandomIntentStatus;
import com.juniperphoton.myersplash.model.UnsplashCategory;
import com.juniperphoton.myersplash.model.UnsplashImage;
import com.juniperphoton.myersplash.utils.DeviceUtil;
import com.juniperphoton.myersplash.utils.DownloadUtil;
import com.juniperphoton.myersplash.utils.LocalSettingHelper;
import com.juniperphoton.myersplash.utils.RequestUtil;
import com.juniperphoton.myersplash.utils.SerializerUtil;
import com.juniperphoton.myersplash.utils.ToastService;
import com.juniperphoton.myersplash.widget.DetailView;

import java.io.File;
import java.lang.reflect.Type;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import moe.feng.material.statusbar.StatusBarCompat;
import rx.Subscriber;

import static com.juniperphoton.myersplash.utils.DisplayUtil.getDimenInPixel;

public class MainActivity extends AppCompatActivity implements INavigationDrawerCallback,
        OnLoadMoreListener, OnClickQuickDownloadCallback, DetailViewNavigationCallback, OnClickSearchCallback {

    private static final String TAG = MainActivity.class.getName();

    private static final int SEARCH_ID = -10000;
    private static final String CHECK_ONE_POINT_ONE_VERSION = "CHECK_ONE_POINT_ONE_VERSION";

    @BindView(R.id.activity_drawer_rv)
    RecyclerView mDrawerRecyclerView;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @SuppressWarnings("UnusedDeclaration")
    @BindView(R.id.toolbar_layout)
    AppBarLayout mAppBarLayout;

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @BindView(R.id.content_activity_rv)
    RecyclerView mContentRecyclerView;

    @BindView(R.id.content_activity_srl)
    SwipeRefreshLayout mRefreshLayout;

    @SuppressWarnings("UnusedDeclaration")
    @BindView(R.id.activity_main_cl)
    CoordinatorLayout mCoordinatorLayout;

    @BindView(R.id.content_activity_search_fab)
    FloatingActionButton mSearchFAB;

    @BindView(R.id.activity_main_detail_view)
    DetailView mDetailView;

    @BindView(R.id.activity_main_search_view)
    com.juniperphoton.myersplash.widget.SearchView mSearchView;

    @BindView(R.id.activity_drawer_bottom_ll)
    LinearLayout mDrawerBottomLL;

    @BindView(R.id.no_item_layout)
    LinearLayout mNoItemLayout;

    @BindView(R.id.no_item_back_tv)
    TextView mGoBackLastCategoryTV;

    @BindView(R.id.nav_naviToDownload_rl)
    RelativeLayout mNavigateToDownloadsRL;

    private int mLastCategory = -1;

    private PhotoAdapter mAdapter;

    private int mCurrentRequestPage = 1;
    private int mSelectedCategoryID = 0;
    private String mQuery;
    private String mUrl = CloudService.BASE_URL;
    private RandomIntentStatus mRandomIntentStatus = RandomIntentStatus.NotReceived;
    private boolean mHandledSearch = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StatusBarCompat.setUpActivity(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        initMainViews();
        handleShortcutsAction();

        if (!LocalSettingHelper.checkKey(this, CHECK_ONE_POINT_ONE_VERSION)) {
            File file = this.getFileStreamPath(SerializerUtil.CATEGORY_LIST_FILE_NAME);
            if (file != null) {
                file.delete();
            }
            LocalSettingHelper.putBoolean(this, CHECK_ONE_POINT_ONE_VERSION, true);
        }

        restorePhotoList();
        getCategories();
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
        if (mContentRecyclerView != null && mContentRecyclerView.getAdapter() != null) {
            mContentRecyclerView.getAdapter().notifyDataSetChanged();
        }
        boolean scrollBar = LocalSettingHelper.getBoolean(this, Constant.SCROLL_TOOLBAR, true);
        AppBarLayout.LayoutParams params =
                (AppBarLayout.LayoutParams) mToolbar.getLayoutParams();
        if (scrollBar) {
            params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                    | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
        } else {
            params.setScrollFlags(0);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                RequestUtil.checkAndRequest(MainActivity.this);
            }
        }, 1000);
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.toolbar)
    void onClickToolbar() {
        mContentRecyclerView.smoothScrollToPosition(0);
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.content_activity_search_fab)
    void onClickSearchFAB() {
        mSearchFAB.hide();
        mSearchView.toggleAnimation(true);
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.drawer_settings_ll)
    void onClickSettings() {
        mDrawerLayout.closeDrawer(GravityCompat.START);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        }, 200);
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.drawer_about_ll)
    void onClickAbout() {
        mDrawerLayout.closeDrawer(GravityCompat.START);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        }, 200);
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.nav_naviToDownload_rl)
    void onClickNaviToDownloads() {
        mDrawerLayout.closeDrawer(GravityCompat.START);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this, ManageDownloadActivity.class);
                startActivity(intent);
            }
        }, 200);
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.no_item_back_rl)
    void onClickReturn() {
        CategoryAdapter adapter = getCategoryAdapter();
        if (mLastCategory != -1 && adapter != null) {
            adapter.select(mLastCategory);
        }
    }

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
                mCurrentRequestPage = 1;
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

        mDetailView.setNavigationCallback(this);
        mSearchView.setSearchCallback(this);

        if (!DeviceUtil.hasNavigationBar(this)) {
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mSearchFAB.getLayoutParams();
            params.setMargins(0, 0, getDimenInPixel(24, this), getDimenInPixel(24, this));
            mSearchFAB.setLayoutParams(params);

            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mDrawerBottomLL.getLayoutParams();
            layoutParams.setMargins(0, 0, 0, 0);
            mDrawerBottomLL.setLayoutParams(layoutParams);

            RelativeLayout.LayoutParams layoutParamsRV = (RelativeLayout.LayoutParams) mDrawerRecyclerView.getLayoutParams();
            layoutParamsRV.setMargins(0, getResources().getDimensionPixelSize(R.dimen.navi_top_banner_height), 0, getDimenInPixel(70, this));
            mDrawerRecyclerView.setLayoutParams(layoutParamsRV);
        }
    }


    private void handleShortcutsAction() {
        String action = getIntent().getAction();
        if(action!=null){
            switch (action) {
                case "action.search": {
                    if (!mHandledSearch) {
                        mHandledSearch = true;
                        onClickSearchFAB();
                    }
                }
                break;

                case "action.random": {
                    if (mRandomIntentStatus == RandomIntentStatus.NotReceived) {
                        mRandomIntentStatus = RandomIntentStatus.Pending;
                    }
                }
                break;
            }
        }
    }

    private void getCategories() {
        List<UnsplashCategory> categories = restoreCategoryList();
        if (categories != null && categories.size() > 0) {
            configCategoryList(categories);
            setCategoryList(categories);
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
                SerializerUtil.serializeToFile(MainActivity.this, unsplashCategories,
                        SerializerUtil.CATEGORY_LIST_FILE_NAME);

                configCategoryList(unsplashCategories);
                setCategoryList(unsplashCategories);
            }
        });
    }

    private void configCategoryList(List<UnsplashCategory> unsplashCategories) {
        UnsplashCategory featureCategory = new UnsplashCategory();
        featureCategory.setId(UnsplashCategory.FEATURED_CATEGORY_ID);
        featureCategory.setTitle(UnsplashCategory.FEATURE_S);

        UnsplashCategory newCategory = new UnsplashCategory();
        newCategory.setId(UnsplashCategory.NEW_CATEGORY_ID);
        newCategory.setTitle(UnsplashCategory.NEW_S);

        UnsplashCategory randomCategory = new UnsplashCategory();
        randomCategory.setId(UnsplashCategory.RANDOM_CATEOGORY_ID);
        randomCategory.setTitle(UnsplashCategory.RANDOM_S);

        unsplashCategories.add(0, featureCategory);
        unsplashCategories.add(0, newCategory);
        unsplashCategories.add(0, randomCategory);
    }

    private void setCategoryList(List<UnsplashCategory> unsplashCategories) {
        CategoryAdapter adapter = new CategoryAdapter(unsplashCategories, MainActivity.this);
        adapter.setCallback(MainActivity.this);
        adapter.select(mRandomIntentStatus == RandomIntentStatus.Pending ? 0 : 1);
        mRandomIntentStatus = RandomIntentStatus.Handled;
        mDrawerRecyclerView.setAdapter(adapter);
    }

    private void setCachedImageList(List<UnsplashImage> unsplashImages) {
        mAdapter = new PhotoAdapter(unsplashImages, MainActivity.this);
        setupCallback(mAdapter);
        mContentRecyclerView.setAdapter(mAdapter);
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

    private List<UnsplashCategory> restoreCategoryList() {
        Type type = new TypeToken<List<UnsplashCategory>>() {
        }.getType();
        return SerializerUtil.deSerializeFromFile(type, this,
                SerializerUtil.CATEGORY_LIST_FILE_NAME);
    }

    private boolean restorePhotoList() {
        Type type = new TypeToken<List<UnsplashImage>>() {
        }.getType();
        List<UnsplashImage> unsplashCategories = SerializerUtil.deSerializeFromFile(type, this,
                SerializerUtil.IMAGE_LIST_FILE_NAME);

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
    public void onSelectItem(UnsplashCategory category) {
        mSelectedCategoryID = category.getId();
        if (category.getTitle() == null) return;

        mToolbar.setTitle(category.getTitle().toUpperCase());
        mDrawerLayout.closeDrawer(GravityCompat.START);
        mCurrentRequestPage = 1;
        mUrl = category.getRequestUrl();
        mRefreshLayout.setRefreshing(true);
        loadPhotoList();
    }

    private void loadPhotoList() {
        Subscriber<List<UnsplashImage>> subscriber = new Subscriber<List<UnsplashImage>>() {
            @Override
            public void onCompleted() {
                mRefreshLayout.setRefreshing(false);
                if (mCurrentRequestPage == 1) {
                    ToastService.sendShortToast("Loaded :D");
                }
            }

            @Override
            public void onError(Throwable e) {
                mRefreshLayout.setRefreshing(false);
                ToastService.sendShortToast("Fail to send request.");
            }

            @Override
            public void onNext(List<UnsplashImage> images) {
                if (mCurrentRequestPage == 1 || mAdapter == null) {
                    setImageList(images);
                    if (mSelectedCategoryID == UnsplashCategory.NEW_CATEGORY_ID) {
                        SerializerUtil.serializeToFile(MainActivity.this, images,
                                SerializerUtil.IMAGE_LIST_FILE_NAME);
                    }
                } else {
                    mAdapter.setLoadMoreData(images);
                }
                PhotoAdapter photoAdapter = getPhotoAdapter();
                if (photoAdapter == null) {
                    updateNoItemVisibility(true);
                } else if (images.size() == 0 && photoAdapter.getItemCount() == 0) {
                    updateNoItemVisibility(true);
                } else {
                    updateNoItemVisibility(false);
                }
            }
        };

        if (mSelectedCategoryID == UnsplashCategory.FEATURED_CATEGORY_ID) {
            CloudService.getInstance().getFeaturedPhotos(subscriber, mUrl, mCurrentRequestPage);
        } else if (mSelectedCategoryID == SEARCH_ID) {
            CloudService.getInstance().searchPhotos(subscriber, mUrl, mCurrentRequestPage, mQuery);
        } else if (mSelectedCategoryID == UnsplashCategory.RANDOM_CATEOGORY_ID) {
            CloudService.getInstance().getRandomPhotos(subscriber, CloudService.RANDOM_PHOTOS_URL);
        } else {
            CloudService.getInstance().getPhotos(subscriber, mUrl, mCurrentRequestPage);
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

    public void updateNoItemVisibility(boolean show) {
        if (show) {
            mNoItemLayout.setVisibility(View.VISIBLE);
        } else {
            mNoItemLayout.setVisibility(View.GONE);
        }
    }

    public CategoryAdapter getCategoryAdapter() {
        return ((CategoryAdapter) mDrawerRecyclerView.getAdapter());
    }

    public PhotoAdapter getPhotoAdapter() {
        return ((PhotoAdapter) mContentRecyclerView.getAdapter());
    }

    @Override
    public void OnLoadMore() {
        ++mCurrentRequestPage;
        loadPhotoList();
    }

    @Override
    public void onClickQuickDownload(final UnsplashImage image) {
        DownloadUtil.checkAndDownload(MainActivity.this, image);
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
        mCurrentRequestPage = 1;
        mAdapter = null;
        mSelectedCategoryID = SEARCH_ID;
        mUrl = CloudService.SEARCH_URL;
        mQuery = keyword;
        mRefreshLayout.setRefreshing(true);
        mToolbar.setTitle(keyword.toUpperCase());
        mContentRecyclerView.setAdapter(null);

        CategoryAdapter adapter = getCategoryAdapter();
        if (adapter != null) {
            mLastCategory = adapter.getSelectedIndex();
            if (mLastCategory != -1) {
                mGoBackLastCategoryTV.setText(String.format(getString(R.string.return_to_content),
                        adapter.getCategoryByIndex(mLastCategory).getTitle().toUpperCase()));
                adapter.select(-1);
            }
        }

        mSearchFAB.show();
        loadPhotoList();
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
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
