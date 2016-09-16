package com.juniperphoton.myersplash.activity;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
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
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.facebook.binaryresource.BinaryResource;
import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.CacheKey;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.request.ImageRequest;
import com.google.gson.reflect.TypeToken;
import com.juniperphoton.myersplash.R;
import com.juniperphoton.myersplash.adapter.CategoryAdapter;
import com.juniperphoton.myersplash.adapter.PhotoAdapter;
import com.juniperphoton.myersplash.callback.INavigationDrawerCallback;
import com.juniperphoton.myersplash.callback.OnClickPhotoCallback;
import com.juniperphoton.myersplash.callback.OnClickQuickDownloadCallback;
import com.juniperphoton.myersplash.callback.OnLoadMoreListener;
import com.juniperphoton.myersplash.cloudservice.CloudService;
import com.juniperphoton.myersplash.model.UnsplashCategory;
import com.juniperphoton.myersplash.model.UnsplashImage;
import com.juniperphoton.myersplash.service.BackgrdDownloadService;
import com.juniperphoton.myersplash.utils.ColorUtil;
import com.juniperphoton.myersplash.utils.DownloadUtil;
import com.juniperphoton.myersplash.utils.RequestUtil;
import com.juniperphoton.myersplash.utils.SerializerUtil;
import com.juniperphoton.myersplash.utils.ToastService;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Scanner;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import moe.feng.material.statusbar.StatusBarCompat;
import rx.Subscriber;

public class MainActivity extends AppCompatActivity implements INavigationDrawerCallback, OnLoadMoreListener, OnClickPhotoCallback, OnClickQuickDownloadCallback {

    private static final String TAG = MainActivity.class.getName();
    private static final int RESULT_CODE = 10000;

    private static final String SHARE_TEXT = "Share %s's amazing photo from MyerSplash app. %s";

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

    @Bind(R.id.detail_root_sv)
    ScrollView mDetailRootScrollView;

    @Bind(R.id.detail_hero_dv)
    SimpleDraweeView mHeroDV;

    @Bind(R.id.detail_backgrd_rl)
    RelativeLayout mDetailInfoRootLayout;

    @Bind(R.id.detail_img_rl)
    RelativeLayout mDetailImgRL;

    @Bind(R.id.detail_name_tv)
    TextView mNameTextView;

    @Bind(R.id.detail_photoby_tv)
    TextView mPhotoByTextView;

    @Bind(R.id.detail_download_fab)
    FloatingActionButton mDownloadFAB;

    @Bind(R.id.detail_share_fab)
    FloatingActionButton mShareFAB;

    private File mCopyFileForSharing;

    private int mHeroStartY = 0;
    private int mHeroEndY = 0;

    private View mClickedView;
    private UnsplashImage mClickedImage;

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

        setSupportActionBar(mToolbar);

        ButterKnife.bind(this);

        initMainViews();
        initDetailViews();

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

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.detail_download_fab)
    void onClickDownload() {
        RequestUtil.check(this);

        if (mClickedImage == null) {
            return;
        }
        Intent intent = new Intent(MainActivity.this, BackgrdDownloadService.class);
        intent.putExtra("name", mClickedImage.getFileNameForDownload());
        intent.putExtra("url", mClickedImage.getDownloadUrl());
        startService(intent);
        ToastService.sendShortToast("Downloading...");
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.detail_root_sv)
    void onClickMaskSV() {
        hideDetailPanel();
    }

    @SuppressWarnings("UnusedDeclaration")
    @OnClick(R.id.detail_share_fab)
    void onClickShare() {
        CacheKey cacheKey = DefaultCacheKeyFactory.getInstance().getEncodedCacheKey(
                ImageRequest.fromUri(Uri.parse(mClickedImage.getListUrl())), null);

        File localFile = null;

        if (cacheKey != null) {
            if (ImagePipelineFactory.getInstance().getMainFileCache().hasKey(cacheKey)) {
                BinaryResource resource = ImagePipelineFactory.getInstance().getMainFileCache().getResource(cacheKey);
                localFile = ((FileBinaryResource) resource).getFile();
            }
        }

        boolean copied = false;
        if (localFile != null && localFile.exists()) {
            mCopyFileForSharing = new File(DownloadUtil.getGalleryPath(), "Share-" + localFile.getName().replace("cnt", "cnt"));
            copied = DownloadUtil.copyFile(localFile, mCopyFileForSharing);
        }

        if (mCopyFileForSharing == null || !mCopyFileForSharing.exists() || !copied) {
            ToastService.sendShortToast("Something went wrong :-(");
            return;
        }

        String shareText = String.format(SHARE_TEXT, mClickedImage.getUserName(), mClickedImage.getDownloadUrl());

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/jpg");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(mCopyFileForSharing));
        intent.putExtra(Intent.EXTRA_SUBJECT, "Share");
        intent.putExtra(Intent.EXTRA_TEXT, shareText);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(Intent.createChooser(intent, "Share"), RESULT_CODE, null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //TODO: Should has a better way to do this.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mCopyFileForSharing != null && mCopyFileForSharing.exists()) {
                    boolean ok = mCopyFileForSharing.delete();
                }
            }
        }, 5000);
    }

    private void initDetailViews() {
        mDetailRootScrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });

        mDetailRootScrollView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.hideDetailPanel();
            }
        });

        mDetailRootScrollView.setVisibility(View.INVISIBLE);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mDetailInfoRootLayout.getLayoutParams());
        params.setMargins(0, 0, 0,
                getResources().getDimensionPixelOffset(R.dimen.img_detail_info_height));
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        mDetailInfoRootLayout.setLayoutParams(params);

        RelativeLayout.LayoutParams paramsDFAB = new RelativeLayout.LayoutParams(mDownloadFAB.getLayoutParams());
        paramsDFAB.setMargins(0, 0, getResources().getDimensionPixelOffset(R.dimen.download_btn_margin_right_hide),
                getResources().getDimensionPixelOffset(R.dimen.download_btn_margin_bottom));
        paramsDFAB.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        paramsDFAB.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        mDownloadFAB.setLayoutParams(paramsDFAB);

        RelativeLayout.LayoutParams paramsSFAB = new RelativeLayout.LayoutParams(mDownloadFAB.getLayoutParams());
        paramsSFAB.setMargins(0, 0, getResources().getDimensionPixelOffset(R.dimen.share_btn_margin_right),
                getResources().getDimensionPixelOffset(R.dimen.share_btn_margin_bottom));
        paramsSFAB.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        paramsSFAB.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        mShareFAB.setLayoutParams(paramsSFAB);
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
        mAdapter = new PhotoAdapter(unsplashImages, MainActivity.this);
        setupCallback(mAdapter);
        mContentRecyclerView.setAdapter(mAdapter);
    }

    private void setupCallback(PhotoAdapter adapter) {
        adapter.setOnLoadMoreListener(MainActivity.this);
        adapter.setOnClickItemListener(MainActivity.this);
        adapter.setOnClickDownloadCallback(MainActivity.this);
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
        if (mDetailRootScrollView.getVisibility() == View.VISIBLE) {
            hideDetailPanel();
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

    private void hideDetailPanel() {
        toggleDetailRLAnimation(false);
        toggleDownloadBtnAnimation(false);
        toggleShareBtnAnimation(false);
    }

    @Override
    public void clickPhotoItem(final RectF rectF, final UnsplashImage unsplashImage, View itemView) {
        if (mClickedView != null) {
            return;
        }
        mClickedImage = unsplashImage;
        mClickedView = itemView;
        mClickedView.setVisibility(View.INVISIBLE);
        mDetailInfoRootLayout.setBackground(new ColorDrawable(unsplashImage.getThemeColor()));
        mNameTextView.setText(unsplashImage.getUserName());

        int backColor = unsplashImage.getThemeColor();
        if (!ColorUtil.isColorLight(backColor)) {
            mNameTextView.setTextColor(Color.WHITE);
            mPhotoByTextView.setTextColor(Color.WHITE);
        } else {
            mNameTextView.setTextColor(Color.BLACK);
            mPhotoByTextView.setTextColor(Color.BLACK);
        }

        mHeroDV.setImageURI(unsplashImage.getListUrl());
        mDetailRootScrollView.setVisibility(View.VISIBLE);
        toggleMaskAnimation(true);

        int[] heroImgePosition = new int[2];
        mDetailImgRL.getLocationOnScreen(heroImgePosition);

        int[] recyclerViewLocation = new int[2];
        mContentRecyclerView.getLocationOnScreen(recyclerViewLocation);

        int itemY = (int) rectF.top;

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mDetailImgRL.getLayoutParams());
        params.setMargins(0, itemY - (int) (20 * 3.5), 0, 0);

        mDetailImgRL.setLayoutParams(params);

        int targetPositonY = getTargetY();

        toggleHeroViewAnimation(itemY, targetPositonY, true);
        toggleDownloadBtnAnimation(true);
        toggleShareBtnAnimation(true);
        mSearchFAB.hide();
    }

    private void toggleHeroViewAnimation(int startY, int endY, final boolean show) {
        if (show) {
            mHeroStartY = startY;
            mHeroEndY = endY;
        }

        Logger.d("start:" + startY + ",end:" + endY);

        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setIntValues(startY, endY);
        valueAnimator.setDuration(300);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mDetailImgRL.getLayoutParams());
                params.setMargins(0, (int) animation.getAnimatedValue(), 0, 0);
                mDetailImgRL.setLayoutParams(params);
            }
        });
        valueAnimator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!show && mClickedView != null) {
                    mClickedView.setVisibility(View.VISIBLE);
                    toggleMaskAnimation(false);
                    mClickedView = null;
                    mClickedImage = null;
                } else {
                    toggleDetailRLAnimation(true);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        valueAnimator.start();
    }

    private int getTargetY() {
        return (getWindow().getDecorView().getHeight() -
                (getResources().getDimensionPixelSize(R.dimen.img_detail_height))) / 2;
    }

    private void toggleDetailRLAnimation(final boolean show) {

        int startY = show ? (getResources().getDimensionPixelOffset(R.dimen.img_detail_info_height)) : 0;
        int endY = show ? 0 : (getResources().getDimensionPixelOffset(R.dimen.img_detail_info_height));

        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setIntValues(startY, endY);
        valueAnimator.setDuration(500);
        valueAnimator.setInterpolator(new FastOutSlowInInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mDetailInfoRootLayout.getLayoutParams());
                params.setMargins(0, 0, 0,
                        (int) animation.getAnimatedValue());
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                mDetailInfoRootLayout.setLayoutParams(params);
            }
        });
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!show) {
                    toggleHeroViewAnimation(mHeroEndY, mHeroStartY, false);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        valueAnimator.start();
    }

    private void toggleDownloadBtnAnimation(final boolean show) {
        int normalX = getResources().getDimensionPixelOffset(R.dimen.download_btn_margin_right);

        int hideX = getResources().getDimensionPixelOffset(R.dimen.download_btn_margin_right_hide);

        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setIntValues(show ? hideX : normalX, show ? normalX : hideX);
        valueAnimator.setDuration(700);
        valueAnimator.setInterpolator(new FastOutSlowInInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                RelativeLayout.LayoutParams paramsFAB = new RelativeLayout.LayoutParams(mDownloadFAB.getLayoutParams());
                paramsFAB.setMargins(0, 0, (int) animation.getAnimatedValue(),
                        getResources().getDimensionPixelOffset(R.dimen.download_btn_margin_bottom));
                paramsFAB.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                paramsFAB.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                mDownloadFAB.setLayoutParams(paramsFAB);
            }
        });
        valueAnimator.start();
    }

    private void toggleShareBtnAnimation(final boolean show) {
        int normalX = getResources().getDimensionPixelOffset(R.dimen.share_btn_margin_right);

        int hideX = getResources().getDimensionPixelOffset(R.dimen.share_btn_margin_right_hide);

        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setIntValues(show ? hideX : normalX, show ? normalX : hideX);
        valueAnimator.setDuration(700);
        valueAnimator.setInterpolator(new FastOutSlowInInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                RelativeLayout.LayoutParams paramsFAB = new RelativeLayout.LayoutParams(mShareFAB.getLayoutParams());
                paramsFAB.setMargins(0, 0, (int) animation.getAnimatedValue(),
                        getResources().getDimensionPixelOffset(R.dimen.share_btn_margin_bottom));
                paramsFAB.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                paramsFAB.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                mShareFAB.setLayoutParams(paramsFAB);
            }
        });
        valueAnimator.start();
    }

    private void toggleMaskAnimation(final boolean show) {
        ValueAnimator animator = ValueAnimator.ofArgb(show ? Color.TRANSPARENT : ContextCompat.getColor(this, R.color.MaskColor),
                show ? ContextCompat.getColor(this, R.color.MaskColor) : Color.TRANSPARENT);
        animator.setDuration(300);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mDetailRootScrollView.setBackground(new ColorDrawable((int) animation.getAnimatedValue()));
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!show) {
                    mDetailRootScrollView.setVisibility(View.INVISIBLE);
                    mSearchFAB.show();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }

    @Override
    public void onClickQuickDownload(UnsplashImage image) {
        mClickedImage = image;
        onClickDownload();
    }
}
