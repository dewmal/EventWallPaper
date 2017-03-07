package com.juniperphoton.myersplash.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.juniperphoton.myersplash.R;

import java.util.List;

public class ThanksToAdapter extends RecyclerView.Adapter<ThanksToAdapter.ThanksToViewHolder> {

    private Context mContext;
    private List<String> mData;

    public ThanksToAdapter(Context context) {
        mContext = context;
    }

    public void refresh(List<String> data) {
        mData = data;
    }

    @Override
    public ThanksToViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ThanksToViewHolder(LayoutInflater.from(mContext).inflate(R.layout.row_thanks_to,
                parent, false));
    }

    @Override
    public void onBindViewHolder(ThanksToViewHolder holder, int position) {
        holder.bind(mData.get(holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    class ThanksToViewHolder extends RecyclerView.ViewHolder {
        private TextView mTextView;

        ThanksToViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView;
        }

        public void bind(String str) {
            mTextView.setText(str);
        }
    }
}
