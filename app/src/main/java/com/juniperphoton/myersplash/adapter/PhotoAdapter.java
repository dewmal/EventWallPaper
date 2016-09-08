package com.juniperphoton.myersplash.adapter;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.view.SimpleDraweeView;
import com.juniperphoton.myersplash.R;
import com.juniperphoton.myersplash.callback.OnLoadMoreListener;
import com.juniperphoton.myersplash.model.UnsplashCategory;
import com.juniperphoton.myersplash.model.UnsplashImage;

import java.util.ArrayList;
import java.util.List;


public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    private List<UnsplashImage> mData;
    private Context mContext;
    private OnLoadMoreListener mOnLoadMoreListener;
    private boolean mOpenLoadMore = true;//是否开启加载更多
    private boolean isAutoLoadMore = true;//是否自动加载，当数据不满一屏幕会自动加载

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

    @Override
    public void onBindViewHolder(PhotoViewHolder holder, int position) {
        if (holder.getItemViewType() == PhotoAdapter.PhotoViewHolder.TYPE_COMMON_VIEW) {
            final int index = holder.getAdapterPosition();
            UnsplashImage image = mData.get(index);
            String regularUrl = image.getRegularUrl();
            int backColor = index % 2 == 0 ?
                    ContextCompat.getColor(mContext, R.color.BackColor1) : ContextCompat.getColor(mContext, R.color.BackColor2);

            holder.RootCardView.setBackground(new ColorDrawable(backColor));
            if (holder.SimpleDraweeView != null) {
                holder.SimpleDraweeView.setImageURI(regularUrl);
                holder.SimpleDraweeView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

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

    private void scrollLoadMore() {
        mOnLoadMoreListener.OnLoadMore();
    }

    public class PhotoViewHolder extends RecyclerView.ViewHolder {
        public static final int TYPE_COMMON_VIEW = 100001;
        public static final int TYPE_FOOTER_VIEW = 100002;

        public SimpleDraweeView SimpleDraweeView;
        public CardView RootCardView;

        public PhotoViewHolder(View itemView) {
            super(itemView);
            SimpleDraweeView = (SimpleDraweeView) itemView.findViewById(R.id.row_photo_iv);
            RootCardView = (CardView) itemView.findViewById(R.id.row_photo_cv);
        }
    }
}
