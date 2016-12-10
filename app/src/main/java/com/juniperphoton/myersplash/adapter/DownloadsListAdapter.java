package com.juniperphoton.myersplash.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.juniperphoton.myersplash.R;
import com.juniperphoton.myersplash.base.App;
import com.juniperphoton.myersplash.callback.DownloadItemsChangedCallback;
import com.juniperphoton.myersplash.model.DownloadItem;
import com.juniperphoton.myersplash.service.BackgroundDownloadService;
import com.juniperphoton.myersplash.widget.DownloadCompleteView;
import com.juniperphoton.myersplash.widget.DownloadRetryView;
import com.juniperphoton.myersplash.widget.DownloadingView;
import com.juniperphoton.myersplash.widget.RippleToggleLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.realm.Realm;

public class DownloadsListAdapter extends RecyclerView.Adapter<DownloadsListAdapter.DownloadItemViewHolder> {

    private Context mContext;
    private List<DownloadItem> mData;

    private DownloadItemsChangedCallback mCallback;

    private final static String UPDATE_PROGRESS_PAYLOAD = "UPDATE_PROGRESS_PAYLOAD";
    private final static int ITEM = 1;
    private final static int FOOTER = 1 << 1;

    public DownloadsListAdapter(ArrayList<DownloadItem> data, Context context) {
        mData = data;
        mContext = context;
    }

    @Override
    public DownloadItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.row_downloaditem, parent, false);
            int width = mContext.getResources().getDisplayMetrics().widthPixels;
            ViewGroup.LayoutParams params = view.getLayoutParams();
            params.height = (int) (width / 1.7d);
            view.setLayoutParams(params);
            return new DownloadItemViewHolder(view, viewType);
        } else if (viewType == FOOTER) {
            View footer = LayoutInflater.from(mContext).inflate(R.layout.row_footer_blank, parent, false);
            return new DownloadItemViewHolder(footer, viewType);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final DownloadItemViewHolder holder, int position) {
        if (getItemViewType(position) == FOOTER) {
            return;
        }
        final DownloadItem item = mData.get(holder.getAdapterPosition());
        holder.DownloadCompleteView.setFilePath(item.getFilePath());
        holder.DraweeView.setImageURI(item.getThumbUrl());
        holder.ProgressStrTV.setText(item.getProgressStr());

        holder.DownloadRetryView.setThemeBackColor(item.getColor());
        holder.DownloadCompleteView.setThemeBackColor(item.getColor());

        holder.DownloadingView.setProgress(item.getProgress());
        holder.DownloadingView.setThemeBackColor(item.getColor());
        holder.DownloadingView.setClickCancelListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeItem(item);
                notifyItemRemoved(holder.getAdapterPosition());

                Intent intent = new Intent(App.getInstance(), BackgroundDownloadService.class);
                intent.putExtra(BackgroundDownloadService.CANCELED_KEY, true);
                intent.putExtra(BackgroundDownloadService.URI_KEY, item.getDownloadUrl());
                mContext.startService(intent);

                Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        DownloadItem realmItem = realm.where(DownloadItem.class).equalTo("mId", item.getId()).findFirst();
                        if (realmItem != null) {
                            realmItem.deleteFromRealm();
                        }
                    }
                });
            }
        });
        holder.RippleToggleView.toggleTo(item.getStatus());
    }

    @Override
    public void onBindViewHolder(DownloadItemViewHolder holder, int position, List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
        if (payloads.contains(UPDATE_PROGRESS_PAYLOAD)) {
            final DownloadItem item = mData.get(holder.getAdapterPosition());
            holder.DownloadingView.setProgress(item.getProgress());
        }
    }

    public void setCallback(DownloadItemsChangedCallback callback) {
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

        @Bind(R.id.row_downloaditem_dv)
        SimpleDraweeView DraweeView;

        @Bind(R.id.row_downloaditem_rtv)
        RippleToggleLayout RippleToggleView;

        @Bind(R.id.row_downloaditem_progressStr_tv)
        TextView ProgressStrTV;

        DownloadingView DownloadingView;
        DownloadRetryView DownloadRetryView;
        DownloadCompleteView DownloadCompleteView;

        public DownloadItemViewHolder(View itemView, int viewType) {
            super(itemView);
            if (viewType == ITEM) {
                ButterKnife.bind(this, itemView);
                DownloadingView = new DownloadingView(mContext, null);
                DownloadRetryView = new DownloadRetryView(mContext, null);
                DownloadCompleteView = new DownloadCompleteView(mContext, null);

                RippleToggleView.addViews(DownloadingView, DownloadRetryView, DownloadCompleteView);
            }
        }
    }

    public void updateItem(DownloadItem item) {
        int index = mData.indexOf(item);
        if (index >= 0 && index <= mData.size()) {
            notifyItemChanged(index, UPDATE_PROGRESS_PAYLOAD);
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
}
