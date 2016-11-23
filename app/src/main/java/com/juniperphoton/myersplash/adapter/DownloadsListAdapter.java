package com.juniperphoton.myersplash.adapter;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.facebook.drawee.view.SimpleDraweeView;
import com.juniperphoton.myersplash.R;
import com.juniperphoton.myersplash.model.DownloadItem;

import java.util.ArrayList;

public class DownloadsListAdapter extends RecyclerView.Adapter<DownloadsListAdapter.DownloadItemViewHolder> {

    private Context mContext;
    private ArrayList<DownloadItem> mData;

    public DownloadsListAdapter(ArrayList<DownloadItem> data, Context context) {
        mData = data;
        mContext = context;
    }

    @Override
    public DownloadItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.row_downloaditem, parent, false);
        return new DownloadItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DownloadItemViewHolder holder, int position) {
        DownloadItem item = mData.get(holder.getAdapterPosition());
        holder.setUrl(item.getThumbUrl());
        holder.setColor(item.getColor());
    }

    @Override
    public int getItemCount() {
        if (mData == null) return 0;
        else return mData.size();
    }

    public class DownloadItemViewHolder extends RecyclerView.ViewHolder {

        private SimpleDraweeView mDraweeView;
        private RelativeLayout mBottomRL;

        public DownloadItemViewHolder(View itemView) {
            super(itemView);
            mDraweeView = (SimpleDraweeView) itemView.findViewById(R.id.row_downloaditem_dv);
            mBottomRL = (RelativeLayout) itemView.findViewById(R.id.row_downloaditem_bottom_rl);
        }

        public void setUrl(String url) {
            mDraweeView.setImageURI(Uri.parse(url));
        }

        public void setColor(int color) {
            mBottomRL.setBackground(new ColorDrawable(color));
        }
    }

}
