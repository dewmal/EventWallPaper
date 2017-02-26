package com.juniperphoton.myersplash.adapter;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.juniperphoton.myersplash.R;
import com.juniperphoton.myersplash.callback.OnClickQuickDownloadCallback;
import com.juniperphoton.myersplash.callback.OnLoadMoreListener;
import com.juniperphoton.myersplash.common.Constant;
import com.juniperphoton.myersplash.fragment.MainListFragment;
import com.juniperphoton.myersplash.model.UnsplashImage;
import com.juniperphoton.myersplash.utils.ColorUtil;
import com.juniperphoton.myersplash.utils.LocalSettingHelper;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {
    private final int FOOTER_FLAG_NOT_SHOW = 0;
    private final int FOOTER_FLAG_SHOW = 1;
    private final int FOOTER_FLAG_SHOW_END = 1 << 1 | FOOTER_FLAG_SHOW;

    private List<UnsplashImage> mData;

    private Context mContext;
    private OnLoadMoreListener mOnLoadMoreListener;
    private MainListFragment.Callback mOnClickPhotoCallback;
    private OnClickQuickDownloadCallback mOnClickDownloadCallback;

    private boolean isAutoLoadMore = true;
    private int footerFlag = FOOTER_FLAG_SHOW;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private int mLastPosition = -1;

    public PhotoAdapter(List<UnsplashImage> data, Context context) {
        mData = data;
        mContext = context;
        mLastPosition = -1;
        if (data.size() >= 10) {
            isAutoLoadMore = true;
            footerFlag = FOOTER_FLAG_SHOW;
        } else if (data.size() > 0) {
            isAutoLoadMore = false;
            footerFlag = FOOTER_FLAG_SHOW_END;
        } else {
            isAutoLoadMore = false;
            footerFlag = FOOTER_FLAG_NOT_SHOW;
        }
    }

    @Override
    public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case PhotoViewHolder.TYPE_COMMON_VIEW: {
                View view = LayoutInflater.from(mContext).inflate(R.layout.row_photo, parent, false);
                return new PhotoViewHolder(view, viewType, footerFlag);
            }
            case PhotoViewHolder.TYPE_FOOTER_VIEW: {
                View view;
                if (footerFlag == FOOTER_FLAG_SHOW_END) {
                    view = LayoutInflater.from(mContext).inflate(R.layout.row_footer_end, parent, false);
                } else {
                    view = LayoutInflater.from(mContext).inflate(R.layout.row_footer, parent, false);
                }
                return new PhotoViewHolder(view, viewType, footerFlag);
            }
        }
        return null;
    }

    @SuppressWarnings("ResourceAsColor")
    @Override
    public void onBindViewHolder(final PhotoViewHolder holder, int position) {
        if (holder.getItemViewType() == PhotoAdapter.PhotoViewHolder.TYPE_COMMON_VIEW) {
            holder.bind(mData.get(holder.getAdapterPosition()), position);
        }
    }

    private void animateContainer(final View container, int position) {
        int lastItemIndex = findLastVisibleItemPosition(mLayoutManager);
        if (position >= getMaxPhotoCountOnScreen() || position <= mLastPosition
                || (lastItemIndex >= getMaxPhotoCountOnScreen())) {
            return;
        }

        mLastPosition = position;

        int delay = 300 * (position + 1);
        int duration = 800;

        container.setAlpha(0f);
        container.setTranslationX(300);

        ValueAnimator animator = ValueAnimator.ofFloat(0.0f, 1.0f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                container.setAlpha((float) valueAnimator.getAnimatedValue());
            }
        });
        animator.setStartDelay(delay);
        animator.setDuration(duration);
        animator.start();

        ValueAnimator animator2 = ValueAnimator.ofInt(300, 0);
        animator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                container.setTranslationX((int) valueAnimator.getAnimatedValue());
            }
        });
        animator2.setInterpolator(new DecelerateInterpolator());
        animator2.setStartDelay(delay);
        animator2.setDuration(duration);
        animator2.start();
    }

    private int getMaxPhotoCountOnScreen() {
        int height = mRecyclerView.getHeight();
        int imgHeight = mRecyclerView.getResources().getDimensionPixelSize(R.dimen.img_height);
        int max = (int) Math.ceil((double) height / (double) imgHeight);
        return max;
    }

    @Override
    public int getItemCount() {
        if (mData == null) return 0;
        int size = footerFlag != FOOTER_FLAG_NOT_SHOW ? mData.size() + 1 : mData.size();
        return size;
    }

    @Override
    public int getItemViewType(int position) {
        if (isFooterView(position)) {
            return PhotoViewHolder.TYPE_FOOTER_VIEW;
        } else return PhotoViewHolder.TYPE_COMMON_VIEW;
    }

    private boolean isFooterView(int position) {
        return footerFlag != FOOTER_FLAG_NOT_SHOW && position >= getItemCount() - 1;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
        mLastPosition = -1;
        mLayoutManager = recyclerView.getLayoutManager();
        startLoadMore(recyclerView, mLayoutManager);
    }

    private void startLoadMore(RecyclerView recyclerView, final RecyclerView.LayoutManager layoutManager) {
        if (mOnLoadMoreListener == null) {
            return;
        }

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (!isAutoLoadMore && findLastVisibleItemPosition(layoutManager) + 1 == getItemCount()) {
                        scrollLoadMore();
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (isAutoLoadMore && findLastVisibleItemPosition(layoutManager) + 1 == getItemCount()) {
                    scrollLoadMore();
                } else if (isAutoLoadMore) {
                    isAutoLoadMore = false;
                }
            }
        });
    }

    public void clear() {
        footerFlag = FOOTER_FLAG_NOT_SHOW;
        mData.clear();
        notifyDataSetChanged();
    }

    public void setLoadMoreData(List<UnsplashImage> data) {
        int size = mData.size();
        mData.addAll(data);
        if (data.size() >= 10) {
            isAutoLoadMore = true;
            footerFlag |= FOOTER_FLAG_SHOW;
            notifyItemInserted(size);
        } else if (data.size() > 0) {
            isAutoLoadMore = false;
            footerFlag |= FOOTER_FLAG_SHOW;
            footerFlag |= FOOTER_FLAG_SHOW_END;
            notifyItemInserted(size);
        } else {
            isAutoLoadMore = false;
            footerFlag = FOOTER_FLAG_NOT_SHOW;
        }
    }

    private int findLastVisibleItemPosition(RecyclerView.LayoutManager layoutManager) {
        if (layoutManager instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
        }
        return -1;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener loadMoreListener) {
        mOnLoadMoreListener = loadMoreListener;
    }

    public void setOnClickItemListener(MainListFragment.Callback callback) {
        mOnClickPhotoCallback = callback;
    }

    public void setOnClickDownloadCallback(OnClickQuickDownloadCallback callback) {
        mOnClickDownloadCallback = callback;
    }

    public UnsplashImage getFirstImage() {
        if (mData != null && mData.size() > 0) {
            return mData.get(0);
        }
        return null;
    }

    private void scrollLoadMore() {
        mOnLoadMoreListener.OnLoadMore();
    }

    public class PhotoViewHolder extends RecyclerView.ViewHolder {
        static final int TYPE_COMMON_VIEW = 1;
        static final int TYPE_FOOTER_VIEW = 1 << 1;

        @Nullable
        @BindView(R.id.row_photo_iv)
        SimpleDraweeView mSimpleDraweeView;

        @Nullable
        @BindView(R.id.row_photo_cv)
        CardView mRootCardView;

        @Nullable
        @BindView(R.id.row_photo_download_rl)
        RelativeLayout mDownloadRL;

        @Nullable
        @BindView(R.id.row_photo_ripple_mask_rl)
        RelativeLayout mRippleMaskRL;

        @Nullable
        @BindView(R.id.row_footer_rl)
        RelativeLayout mFooterRL;

        public PhotoViewHolder(View itemView, int type, int footerFlag) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            if (type == TYPE_COMMON_VIEW) {
                // Ignore
            } else {
                if (footerFlag == FOOTER_FLAG_NOT_SHOW) {
                    mFooterRL.setVisibility(View.INVISIBLE);
                }
            }
        }

        public void bind(final UnsplashImage image, int pos) {
            final String regularUrl = image.getListUrl();

            int backColor = ColorUtil.getDarkerColor(image.getThemeColor(), 0.7f);

            if (LocalSettingHelper.getBoolean(mContext, Constant.QUICK_DOWNLOAD_CONFIG_NAME, false)) {
                if (!image.hasDownloaded()) {
                    mDownloadRL.setVisibility(View.VISIBLE);
                    mDownloadRL.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mOnClickDownloadCallback != null) {
                                mOnClickDownloadCallback.onClickQuickDownload(image);
                            }
                        }
                    });
                } else {
                    mDownloadRL.setVisibility(View.GONE);
                }
            } else {
                mDownloadRL.setVisibility(View.GONE);
            }
            if (mSimpleDraweeView != null) {
                mRootCardView.setBackground(new ColorDrawable(backColor));
                mSimpleDraweeView.setImageURI(regularUrl);
                mRippleMaskRL.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (regularUrl == null) {
                            return;
                        }
                        if (!Fresco.getImagePipeline().isInBitmapMemoryCache(Uri.parse(regularUrl))) {
                            return;
                        }
                        int[] location = new int[2];
                        mSimpleDraweeView.getLocationOnScreen(location);
                        if (mOnClickPhotoCallback != null) {
                            mOnClickPhotoCallback.clickPhotoItem(new RectF(
                                    location[0], location[1],
                                    mSimpleDraweeView.getWidth(), mSimpleDraweeView.getHeight()), image, mRootCardView);
                        }
                    }
                });
            }
            animateContainer(mRootCardView, pos);
        }
    }
}
