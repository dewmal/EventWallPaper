package com.juniperphoton.myersplash.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.juniperphoton.myersplash.R;
import com.juniperphoton.myersplash.model.UnsplashCategory;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchTextAdapter extends RecyclerView.Adapter<SearchTextAdapter.SearchTextVH> {
    private Context mContext;
    private List<UnsplashCategory> mData;

    public SearchTextAdapter(Context context) {
        mContext = context;
    }

    public void setData(List<UnsplashCategory> data) {
        mData = data;
    }

    @Override
    public SearchTextVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.row_search_text, null, false);
        return new SearchTextVH(view);
    }

    @Override
    public void onBindViewHolder(SearchTextVH holder, int position) {
        holder.bind(mData.get(holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public class SearchTextVH extends RecyclerView.ViewHolder {
        @BindView(R.id.search_text)
        TextView mTextView;

        public SearchTextVH(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(UnsplashCategory category) {
            mTextView.setText(category.getTitle());
        }
    }
}
