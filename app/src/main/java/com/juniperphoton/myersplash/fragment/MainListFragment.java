package com.juniperphoton.myersplash.fragment;

import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.juniperphoton.myersplash.R;
import com.juniperphoton.myersplash.adapter.PhotoAdapter;
import com.juniperphoton.myersplash.callback.OnClickQuickDownloadCallback;
import com.juniperphoton.myersplash.callback.OnLoadMoreListener;
import com.juniperphoton.myersplash.cloudservice.CloudService;
import com.juniperphoton.myersplash.event.RefreshAllEvent;
import com.juniperphoton.myersplash.event.RequestSearchEvent;
import com.juniperphoton.myersplash.event.ScrollToTopEvent;
import com.juniperphoton.myersplash.model.UnsplashCategory;
import com.juniperphoton.myersplash.model.UnsplashImage;
import com.juniperphoton.myersplash.utils.DownloadUtil;
import com.juniperphoton.myersplash.utils.ToastService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Subscriber;

public class MainListFragment extends Fragment implements OnLoadMoreListener, OnClickQuickDownloadCallback {
    private static final String TAG = "MainListFragment";
    private PhotoAdapter mAdapter;

    @BindView(R.id.content_activity_rv)
    RecyclerView mContentRecyclerView;

    @BindView(R.id.content_activity_srl)
    SwipeRefreshLayout mRefreshLayout;

    @BindView(R.id.no_item_layout)
    LinearLayout mNoItemLayout;

    @BindView(R.id.no_item_retry_btn)
    View mRetryBtn;

    private Callback mCallback;

    private UnsplashCategory mCategory;
    private int mNext = 1;
    private boolean mLoadedData;
    private boolean mVisible;
    private boolean mLoadView;
    private boolean mRefreshing;

    private String mQuery;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_list, null, false);
        ButterKnife.bind(this, view);
        mLoadView = true;
        init();
        if (mVisible && !mLoadedData) {
            loadPhotoList();
            mLoadedData = true;
        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.d(TAG, "isVisibleToUser:" + isVisibleToUser);
        mVisible = isVisibleToUser;
        if (mVisible && !mLoadedData && mLoadView) {
            loadPhotoList();
            mLoadedData = true;
        }
        if (mVisible) {
            register();
        } else if (EventBus.getDefault().isRegistered(this)) {
            unregister();
        }
    }

    public MainListFragment() {
        super();
    }

    public void scrollToTop() {
        if (mContentRecyclerView != null) {
            mContentRecyclerView.smoothScrollToPosition(0);
        }
    }

    public void register() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    public void unregister() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    public void clear() {
        if (mAdapter != null) {
            mAdapter.clear();
        }
    }

    public void requestRefresh() {
        if (mRefreshing) {
            return;
        }
        mNext = 1;
        loadPhotoList();
    }

    public void init() {
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestRefresh();
            }
        });
        mContentRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(),
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
                    if (mCallback != null) {
                        mCallback.onScrollHide();
                    }
                } else if (dy < -20) {
                    if (mCallback != null) {
                        mCallback.onScrollShow();
                    }
                }
            }
        });
        mRetryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateNoItemVisibility(false);
                loadPhotoList();
            }
        });
    }

    @Override
    public void OnLoadMore() {
        mNext++;
        loadPhotoList();
    }

    @Override
    public void onClickQuickDownload(final UnsplashImage image) {
        DownloadUtil.checkAndDownload(getActivity(), image);
    }

    public void setCategory(UnsplashCategory category, Callback callback) {
        mCategory = category;
        mCallback = callback;
    }

    private void setImageList(List<UnsplashImage> unsplashImages) {
        if (mAdapter != null && mAdapter.getFirstImage() != null) {
            if (mAdapter.getFirstImage().getId().equals(unsplashImages.get(0).getId())) {
                return;
            }
        }
        mAdapter = new PhotoAdapter(unsplashImages, getActivity());
        mAdapter.setOnLoadMoreListener(this);
        mAdapter.setOnClickDownloadCallback(this);
        mAdapter.setOnClickItemListener(mCallback);
        mContentRecyclerView.setAdapter(mAdapter);
    }

    public void updateNoItemVisibility(boolean show) {
        mNoItemLayout.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void setSignalOfRequestEnd() {
        mRefreshLayout.setRefreshing(false);
        mRefreshing = false;
    }

    private void loadPhotoList() {
        mRefreshing = true;
        if (mNext == 1) {
            mRefreshLayout.setRefreshing(true);
        }
        Subscriber<List<UnsplashImage>> subscriber = new Subscriber<List<UnsplashImage>>() {
            @Override
            public void onCompleted() {
                setSignalOfRequestEnd();
            }

            @Override
            public void onError(Throwable e) {
                setSignalOfRequestEnd();
                ToastService.sendShortToast("Fail to send request.");
                if (mAdapter != null && mAdapter.getItemCount() > 0) {
                    updateNoItemVisibility(false);
                } else {
                    updateNoItemVisibility(true);
                }
            }

            @Override
            public void onNext(List<UnsplashImage> images) {
                if (mNext == 1 || mAdapter == null) {
                    setImageList(images);
                } else {
                    mAdapter.setLoadMoreData(images);
                }
                if (mAdapter == null) {
                    updateNoItemVisibility(true);
                } else if (images.size() == 0 && mAdapter.getItemCount() == 0) {
                    updateNoItemVisibility(true);
                } else {
                    updateNoItemVisibility(false);
                }
                if (mNext == 1) {
                    ToastService.sendShortToast("Loaded :D");
                }
            }
        };

        if (mCategory == null) {
            return;
        }
        switch (mCategory.getId()) {
            case UnsplashCategory.FEATURED_CATEGORY_ID:
                CloudService.getInstance().getFeaturedPhotos(subscriber, mCategory.getRequestUrl(), mNext);
                break;
            case UnsplashCategory.NEW_CATEGORY_ID:
                CloudService.getInstance().getPhotos(subscriber, mCategory.getRequestUrl(), mNext);
                break;
            case UnsplashCategory.RANDOM_CATEGORY_ID:
                CloudService.getInstance().getRandomPhotos(subscriber, mCategory.getRequestUrl());
                break;
            case UnsplashCategory.SEARCH_ID:
                CloudService.getInstance().searchPhotos(subscriber, mCategory.getRequestUrl(), mNext, mQuery);
                break;
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ScrollToTopEvent event) {
        if (event.categoryId == mCategory.getId()) {
            mContentRecyclerView.smoothScrollToPosition(0);
            if (event.requestRefresh) {
                requestRefresh();
            }
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(RefreshAllEvent event) {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(RequestSearchEvent event) {
        if (mCategory.getId() != UnsplashCategory.SEARCH_ID) {
            return;
        }
        Log.d(TAG, "RequestSearchEvent received:" + event.query);
        mQuery = event.query;
        requestRefresh();
    }

    public interface Callback {
        void onScrollHide();

        void onScrollShow();

        void clickPhotoItem(RectF rectF, UnsplashImage unsplashImage, View itemView);
    }
}
