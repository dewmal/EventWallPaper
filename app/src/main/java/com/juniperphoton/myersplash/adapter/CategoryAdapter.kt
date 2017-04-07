package com.juniperphoton.myersplash.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.juniperphoton.myersplash.R

class CategoryAdapter(val context: Context, val list: MutableList<String>) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {
    companion object ResMap {
        const val BUILDINGS = "buildings"
        const val FOOD = "food"
        const val NATURE = "nature"
        const val TECHNOLOGY = "technology"
        const val TRAVEL = "travel"
        const val PEOPLE = "people"
        const val SEA = "sea"
        const val SKY = "sky"
        const val SPRING = "spring"
    }

    var onClickItem: ((string: String) -> Unit)? = null

    override fun onBindViewHolder(holder: CategoryViewHolder?, position: Int) {
        holder?.bind(list[holder.adapterPosition])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(LayoutInflater.from(context).inflate(R.layout.row_search_category, parent, false))
    }

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @BindView(R.id.category_text)
        @JvmField var categoryName: TextView? = null

        private var category: String? = null

        init {
            ButterKnife.bind(this, itemView)
            itemView.setOnClickListener {
                if (category != null) {
                    onClickItem?.invoke(category!!)
                }
            }
        }

        fun bind(cate: String) {
            category = cate
            categoryName?.text = cate
        }
    }
}
