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
import com.juniperphoton.myersplash.event.ScrollToTopEvent;
import com.juniperphoton.myersplash.model.UnsplashCategory;
import com.juniperphoton.myersplash.model.UnsplashImage;
import com.juniperphoton.myersplash.utils.DownloadUtil;
import com.juniperphoton.myersplash.utils.SerializerUtil;
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

    private Callback mCallback;

    private UnsplashCategory mCategory;
    private int mNext = 1;
    private boolean mLoaded;
    private boolean mVisible;
    private boolean mLoadView;
    private boolean mRefreshing;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_list, null, false);
        ButterKnife.bind(this, view);
        mLoadView = true;
        if (mVisible && !mLoaded) {
            init();
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
        if (mVisible && !mLoaded && mLoadView) {
            init();
        }
        if (mVisible) {
            EventBus.getDefault().register(this);
        } else if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    public MainListFragment() {
        super();
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
        loadPhotoList();
        mLoaded = true;
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

    public PhotoAdapter getPhotoAdapter() {
        return ((PhotoAdapter) mContentRecyclerView.getAdapter());
    }

    public void updateNoItemVisibility(boolean show) {
        if (show) {
            mNoItemLayout.setVisibility(View.VISIBLE);
        } else {
            mNoItemLayout.setVisibility(View.GONE);
        }
    }

    private void loadPhotoList() {
        mRefreshing = true;
        if (mNext == 1) {
            mRefreshLayout.setRefreshing(true);
        }
        Subscriber<List<UnsplashImage>> subscriber = new Subscriber<List<UnsplashImage>>() {
            @Override
            public void onCompleted() {
                mRefreshLayout.setRefreshing(false);
                mRefreshing = false;
            }

            @Override
            public void onError(Throwable e) {
                ToastService.sendShortToast("Fail to send request.");
            }

            @Override
            public void onNext(List<UnsplashImage> images) {
                if (mNext == 1 || mAdapter == null) {
                    setImageList(images);
                    SerializerUtil.serializeToFile(getActivity(), images,
                            SerializerUtil.IMAGE_LIST_FILE_NAME);
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
                if (mNext == 1) {
                    ToastService.sendShortToast("Loaded :D");
                }
            }
        };

        switch (mCategory.getId()) {
            case UnsplashCategory.FEATURED_CATEGORY_ID:
                CloudService.getInstance().getFeaturedPhotos(subscriber, mCategory.getRequestUrl(), mNext);
                break;
            case UnsplashCategory.NEW_CATEGORY_ID:
                CloudService.getInstance().getPhotos(subscriber, mCategory.getRequestUrl(), mNext);
                break;
            case UnsplashCategory.RANDOM_CATEOGORY_ID:
                CloudService.getInstance().getRandomPhotos(subscriber, mCategory.getRequestUrl());
                break;
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ScrollToTopEvent event) {
        boolean refresh = event.requestRefresh;
        int id = event.categoryId;
        if (id == mCategory.getId()) {
            mContentRecyclerView.smoothScrollToPosition(0);
        }
        if (refresh) {
            requestRefresh();
        }
    }

    public interface Callback {
        void onScrollHide();

        void onScrollShow();

        void clickPhotoItem(RectF rectF, UnsplashImage unsplashImage, View itemView);
    }
}
