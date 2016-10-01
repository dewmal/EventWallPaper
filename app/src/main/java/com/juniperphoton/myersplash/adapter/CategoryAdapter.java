package com.juniperphoton.myersplash.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.juniperphoton.myersplash.R;
import com.juniperphoton.myersplash.callback.INavigationDrawerCallback;
import com.juniperphoton.myersplash.model.UnsplashCategory;
import com.juniperphoton.myersplash.widget.RectView;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryListViewHolder> {

    private List<UnsplashCategory> mData;
    private Context mContext;
    private int mSelectedIndex = -1;
    private RectView mLastSelectedRV = null;
    private CardView mLastSelectedCV = null;
    private INavigationDrawerCallback mCallback = null;


    public CategoryAdapter(List<UnsplashCategory> data, Context context) {
        mData = data;
        mContext = context;
    }

    public void setCallback(INavigationDrawerCallback callback) {
        mCallback = callback;
    }

    @Override
    public CategoryListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.row_category, parent, false);
        return new CategoryListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CategoryListViewHolder holder, int position) {
        final int index = holder.getAdapterPosition();
        final UnsplashCategory category = mData.get(index);
        holder.TitleTextView.setText(category.getTitle());
        holder.ItemRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (index == mSelectedIndex) {
                    return;
                }
                mSelectedIndex = index;
                selectItem(holder, category);
            }
        });
        if (index == mSelectedIndex) {
            selectItem(holder, category);
        }
    }

    private void selectItem(CategoryListViewHolder holder, UnsplashCategory category) {
        holder.LeftRect.setVisibility(View.VISIBLE);
        holder.ItemRoot.setBackground(new ColorDrawable(ContextCompat.getColor(mContext, R.color.SelectedBackgroundColor)));

        if (mLastSelectedRV != null) {
            mLastSelectedRV.setVisibility(View.INVISIBLE);
        }
        if (mLastSelectedCV != null) {
            mLastSelectedCV.setBackground(new ColorDrawable(Color.TRANSPARENT));
        }
        mLastSelectedRV = holder.LeftRect;
        mLastSelectedCV = holder.ItemRoot;
        if (mCallback != null) {
            mCallback.onSelectItem(category);
        }
    }

    public void select(int i) {
        mSelectedIndex = i;
        notifyDataSetChanged();
    }

    public int getSelectedIndex() {
        return mSelectedIndex;
    }

    public UnsplashCategory getCategoryByIndex(int index) {
        if (index > 0 && index < mData.size()) {
            return mData.get(index);
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public class CategoryListViewHolder extends RecyclerView.ViewHolder {
        public TextView TitleTextView;
        public CardView ItemRoot;
        public RectView LeftRect;

        public CategoryListViewHolder(View itemView) {
            super(itemView);
            TitleTextView = (TextView) itemView.findViewById(R.id.row_category);
            ItemRoot = (CardView) itemView.findViewById(R.id.row_category_cv);
            LeftRect = (RectView) itemView.findViewById(R.id.row_cateogory_rv);
        }
    }
}
