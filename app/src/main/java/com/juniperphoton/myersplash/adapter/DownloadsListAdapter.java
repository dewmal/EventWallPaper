package com.juniperphoton.myersplash.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.view.SimpleDraweeView;
import com.juniperphoton.flipperviewlib.FlipperView;
import com.juniperphoton.myersplash.R;
import com.juniperphoton.myersplash.base.App;
import com.juniperphoton.myersplash.model.DownloadItem;
import com.juniperphoton.myersplash.service.BackgroundDownloadService;
import com.juniperphoton.myersplash.utils.DownloadItemTransactionHelper;
import com.juniperphoton.myersplash.utils.Params;
import com.juniperphoton.myersplash.widget.DownloadCompleteView;
import com.juniperphoton.myersplash.widget.DownloadRetryView;
import com.juniperphoton.myersplash.widget.DownloadingView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DownloadsListAdapter extends RecyclerView.Adapter<DownloadsListAdapter.DownloadItemViewHolder> {
    private Context mContext;
    private List<DownloadItem> mData;

    private DownloadStateChangedCallback mCallback;

    private final static int ITEM = 1;
    private final static int FOOTER = 1 << 1;

    public DownloadsListAdapter(List<DownloadItem> data, Context context) {
        mData = data;
        mContext = context;
    }

    @Override
    public DownloadItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.row_download_item, parent, false);
            int width = mContext.getResources().getDisplayMetrics().widthPixels;
            ViewGroup.LayoutParams params = view.getLayoutParams();
            params.height = (int) (width / 1.7d);
            view.setLayoutParams(params);
            return new DownloadItemViewHolder(view);
        } else if (viewType == FOOTER) {
            View footer = LayoutInflater.from(mContext).inflate(R.layout.row_footer_blank, parent, false);
            return new DownloadItemViewHolder(footer);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final DownloadItemViewHolder holder, int position) {
        if (getItemViewType(position) == FOOTER) {
            return;
        }
        final DownloadItem item = mData.get(holder.getAdapterPosition());

        holder.downloadCompleteView.setFilePath(item.getFilePath());
        holder.downloadCompleteView.setThemeBackColor(item.getColor());

        holder.draweeView.setImageURI(item.getThumbUrl());
        holder.downloadingView.setProgress(item.getProgress());

        holder.downloadRetryView.setThemeBackColor(item.getColor());
        holder.downloadRetryView.setOnClickDeleteListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mData.remove(holder.getAdapterPosition());
                    notifyItemRemoved(holder.getAdapterPosition());

                    Intent intent = new Intent(App.getInstance(), BackgroundDownloadService.class);
                    intent.putExtra(Params.CANCELED_KEY, true);
                    intent.putExtra(Params.URL_KEY, item.getDownloadUrl());
                    mContext.startService(intent);

                    DownloadItemTransactionHelper.delete(item);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        holder.downloadRetryView.setOnClickRetryListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DownloadItemTransactionHelper.updateStatus(item, DownloadItem.DOWNLOAD_STATUS_DOWNLOADING);
                holder.flipperView.next(item.getStatus());

                Intent intent = new Intent(mContext, BackgroundDownloadService.class);
                intent.putExtra(Params.NAME_KEY, item.getFileName());
                intent.putExtra(Params.URL_KEY, item.getDownloadUrl());
                mContext.startService(intent);
            }
        });

        holder.downloadingView.setProgress(item.getProgress());
        holder.downloadingView.setThemeBackColor(item.getColor());
        holder.downloadingView.setClickCancelListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DownloadItemTransactionHelper.updateStatus(item, DownloadItem.DOWNLOAD_STATUS_FAILED);
                holder.flipperView.next(item.getStatus());

                Intent intent = new Intent(App.getInstance(), BackgroundDownloadService.class);
                intent.putExtra(Params.CANCELED_KEY, true);
                intent.putExtra(Params.URL_KEY, item.getDownloadUrl());
                mContext.startService(intent);
            }
        });

        int last = item.getLastStatus();
        if (last != item.getStatus()) {
            holder.flipperView.next(item.getStatus());
            item.syncStatus();
        } else {
            holder.flipperView.next(item.getStatus(), false);
        }
    }

    public void setCallback(DownloadStateChangedCallback callback) {
        mCallback = callback;
    }

    private void removeItem(DownloadItem item) {
        for (DownloadItem downloadItem : mData) {
            if (downloadItem.getId().equals(item.getId())) {
                int index = mData.indexOf(downloadItem);
                mData.remove(index);
                break;
            }
        }
        if (mCallback != null) {
            mCallback.onDataChanged();
        }
    }

    @Override
    public int getItemCount() {
        if (mData == null) return 0;
        else return mData.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position >= getItemCount() - 1) {
            return FOOTER;
        } else return ITEM;
    }

    public class DownloadItemViewHolder extends RecyclerView.ViewHolder {
        @Nullable
        @BindView(R.id.row_download_item_dv)
        SimpleDraweeView draweeView;

        @Nullable
        @BindView(R.id.row_download_flipper_view)
        FlipperView flipperView;

        @Nullable
        @BindView(R.id.row_downloading_view)
        DownloadingView downloadingView;

        @Nullable
        @BindView(R.id.row_download_retry_view)
        DownloadRetryView downloadRetryView;

        @Nullable
        @BindView(R.id.row_download_complete_view)
        DownloadCompleteView downloadCompleteView;

        public DownloadItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public void updateItem(DownloadItem item) {
        int index = mData.indexOf(item);
        if (index >= 0 && index <= mData.size()) {
            Log.d("adapter", "notifyItemChanged:" + index);
            notifyItemChanged(index);
        }
    }

    public void refreshItems(List<DownloadItem> items) {
        mData = items;
        notifyDataSetChanged();
    }

    public void clear() {
        mData.clear();
        notifyDataSetChanged();
    }

    public List<DownloadItem> getData() {
        return mData;
    }

    public interface DownloadStateChangedCallback {
        void onDataChanged();

        void onRetryDownload(String id);
    }
}
