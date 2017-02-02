package com.juniperphoton.myersplash.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
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

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryListViewHolder> {
    private List<UnsplashCategory> mData;
    private Context mContext;
    private int mSelectedIndex = -1;
    private View mLastLeftPlaceHolder = null;
    private CardView mLastSelectedRoot = null;
    private TextView mLastText;
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
        holder.titleTextView.setText(category.getTitle());
        holder.itemRoot.setOnClickListener(new View.OnClickListener() {
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
        holder.itemRoot.setBackground(new ColorDrawable(Color.BLACK));
        holder.titleTextView.setTypeface(Typeface.DEFAULT_BOLD);
        holder.titleTextView.setTextColor(ContextCompat.getColor(mContext, R.color.MyerSplashThemeColor));
        //holder.leftRect.setVisibility(View.VISIBLE);

        if (mLastLeftPlaceHolder != null) {
            mLastLeftPlaceHolder.setVisibility(View.GONE);
        }
        if (mLastSelectedRoot != null) {
            mLastSelectedRoot.setBackground(new ColorDrawable(Color.TRANSPARENT));
        }
        if (mLastText != null) {
            mLastText.setTypeface(Typeface.DEFAULT);
            mLastText.setTextColor(Color.WHITE);
        }
        mLastLeftPlaceHolder = holder.leftRect;
        mLastSelectedRoot = holder.itemRoot;
        mLastText = holder.titleTextView;
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
        if (index >= 0 && index < mData.size()) {
            return mData.get(index);
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public class CategoryListViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.row_category)
        TextView titleTextView;

        @BindView(R.id.row_category_cv)
        CardView itemRoot;

        @BindView(R.id.row_category_selected)
        View leftRect;

        public CategoryListViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            leftRect.setVisibility(View.GONE);
        }
    }
}
