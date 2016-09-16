package com.juniperphoton.myersplash.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.RelativeLayout;

import com.facebook.common.logging.FLog;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.datasource.DataSubscriber;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.image.QualityInfo;
import com.juniperphoton.myersplash.R;
import com.juniperphoton.myersplash.activity.DetailActivity;
import com.juniperphoton.myersplash.callback.OnClickPhotoCallback;
import com.juniperphoton.myersplash.callback.OnClickQuickDownloadCallback;
import com.juniperphoton.myersplash.callback.OnLoadMoreListener;
import com.juniperphoton.myersplash.common.Constant;
import com.juniperphoton.myersplash.model.UnsplashImage;
import com.juniperphoton.myersplash.utils.LocalSettingHelper;

import java.util.List;


public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    private List<UnsplashImage> mData;

    private Context mContext;
    private OnLoadMoreListener mOnLoadMoreListener;
    private OnClickPhotoCallback mOnClickPhotoCallback;
    private OnClickQuickDownloadCallback mOnClickDownloadCallback;

    private boolean mOpenLoadMore = true;//是否开启加载更多
    private boolean isAutoLoadMore = true;//是否自动加载，当数据不满一屏幕会自动加载

    private RecyclerView mRecyclerView;

    public PhotoAdapter(List<UnsplashImage> data, Context context) {
        mData = data;
        mContext = context;
    }

    @Override
    public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case PhotoViewHolder.TYPE_COMMON_VIEW: {
                View view = LayoutInflater.from(mContext).inflate(R.layout.row_photo, parent, false);
                return new PhotoViewHolder(view);
            }
            case PhotoViewHolder.TYPE_FOOTER_VIEW: {
                View view = LayoutInflater.from(mContext).inflate(R.layout.row_footer, parent, false);
                return new PhotoViewHolder(view);
            }
        }
        return null;
    }

    @SuppressWarnings("ResourceAsColor")
    @Override
    public void onBindViewHolder(final PhotoViewHolder holder, int position) {
        if (holder.getItemViewType() == PhotoAdapter.PhotoViewHolder.TYPE_COMMON_VIEW) {
            final int index = holder.getAdapterPosition();
            final UnsplashImage image = mData.get(index);
            final String regularUrl = image.getListUrl();

            int backColor = index % 2 == 0 ?
                    ContextCompat.getColor(mContext, R.color.BackColor1) :
                    ContextCompat.getColor(mContext, R.color.BackColor2);
            if (LocalSettingHelper.getBoolean(mContext, Constant.QUICK_DOWNLOAD_CONFIG_NAME, false)) {
                holder.DownloadRL.setVisibility(View.VISIBLE);
                if (mOnClickDownloadCallback != null) {
                    mOnClickDownloadCallback.onClickQuickDownload(image);
                }
            }
            if (holder.SimpleDraweeView != null) {
                holder.RootCardView.setBackground(new ColorDrawable(backColor));
                holder.SimpleDraweeView.setImageURI(regularUrl);
                holder.SimpleDraweeView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(Fresco.getImagePipeline().isInBitmapMemoryCache(Uri.parse(regularUrl))){
                            int[] location = new int[2];
                            holder.SimpleDraweeView.getLocationOnScreen(location);
                            if (mOnClickPhotoCallback != null) {
                                mOnClickPhotoCallback.clickPhotoItem(new RectF(
                                        location[0], location[1],
                                        holder.SimpleDraweeView.getWidth(), holder.SimpleDraweeView.getHeight()), image, holder.SimpleDraweeView);
                            }
                        }
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (isFooterView(position)) {
            return PhotoViewHolder.TYPE_FOOTER_VIEW;
        } else return PhotoViewHolder.TYPE_COMMON_VIEW;
    }

    private boolean isFooterView(int position) {
        return position >= getItemCount() - 1;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        mRecyclerView = recyclerView;
        startLoadMore(recyclerView, layoutManager);
    }

    private void startLoadMore(RecyclerView recyclerView, final RecyclerView.LayoutManager layoutManager) {
        if (!mOpenLoadMore || mOnLoadMoreListener == null) {
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

    /**
     * 刷新加载更多的数据
     *
     * @param datas
     */
    public void setLoadMoreData(List<UnsplashImage> datas) {
        int size = mData.size();
        mData.addAll(datas);
        notifyItemInserted(size);
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

    public void setOnClickItemListener(OnClickPhotoCallback callback) {
        mOnClickPhotoCallback = callback;
    }

    public void setOnClickDownloadCallback(OnClickQuickDownloadCallback callback) {
        mOnClickDownloadCallback = callback;
    }

    private void scrollLoadMore() {
        mOnLoadMoreListener.OnLoadMore();
    }

    public class PhotoViewHolder extends RecyclerView.ViewHolder {
        public static final int TYPE_COMMON_VIEW = 100001;
        public static final int TYPE_FOOTER_VIEW = 100002;

        public SimpleDraweeView SimpleDraweeView;
        public CardView RootCardView;
        public RelativeLayout DownloadRL;

        public PhotoViewHolder(View itemView) {
            super(itemView);
            SimpleDraweeView = (SimpleDraweeView) itemView.findViewById(R.id.row_photo_iv);
            RootCardView = (CardView) itemView.findViewById(R.id.row_photo_cv);
            DownloadRL = (RelativeLayout) itemView.findViewById(R.id.row_photo_download_rl);
        }
    }
}
