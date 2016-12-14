package com.juniperphoton.myersplash.adapter;

import android.animation.ValueAnimator;
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

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.graphics.Typeface.BOLD;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryListViewHolder> {

    private List<UnsplashCategory> mData;
    private Context mContext;
    private int mSelectedIndex = -1;
    private View mLastSelectedRV = null;
    private TextView mLastSelectedTV = null;
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
        toggleSelectedAnimation(holder.leftRect, true);
        holder.titleTextView.setTextColor(Color.BLACK);
        holder.itemRoot.setBackground(new ColorDrawable(ContextCompat.getColor(mContext, R.color.SelectedBackgroundColor)));

        if (mLastSelectedRV != null) {
            toggleSelectedAnimation(mLastSelectedRV, false);
            mLastSelectedTV.setTextColor(Color.WHITE);
        }
        if (mLastSelectedCV != null) {
            mLastSelectedCV.setBackground(new ColorDrawable(Color.TRANSPARENT));
        }
        mLastSelectedRV = holder.leftRect;
        mLastSelectedTV = holder.titleTextView;
        mLastSelectedCV = holder.itemRoot;
        if (mCallback != null) {
            mCallback.onSelectItem(category);
        }
    }

    public void select(int i) {
        mSelectedIndex = i;
        notifyDataSetChanged();
    }

    private void toggleSelectedAnimation(final View view, boolean selected) {
        float from = selected ? 0 : 1;
        float to = selected ? 1 : 0;
        ValueAnimator animator = ValueAnimator.ofFloat(from, to).setDuration(selected ? 500 : 200);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                view.setAlpha((float) animation.getAnimatedValue());
            }
        });
        animator.start();
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

        @BindView(R.id.row_category_rv)
        View leftRect;

        public CategoryListViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
