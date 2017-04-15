package com.juniperphoton.myersplash.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.juniperphoton.myersplash.R

class CategoryAdapter(val context: Context, val list: MutableList<String>) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {
    companion object ResMap {
        const val BUILDINGS = "Buildings"
        const val FOOD = "Food"
        const val NATURE = "Nature"
        const val TECHNOLOGY = "Technology"
        const val TRAVEL = "Travel"
        const val PEOPLE = "People"
        const val SEA = "Sea"
        const val DUSK = "Dusk"
        const val MOUNTAIN = "Mountain"
        const val GALAXY = "Galaxy"
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
