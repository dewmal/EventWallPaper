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

        private val map: HashMap <String, Int> = HashMap()

        init {
            map.put(BUILDINGS, R.drawable.ic_building)
            map.put(FOOD, R.drawable.ic_food)
            map.put(NATURE, R.drawable.ic_nature)
            map.put(TECHNOLOGY, R.drawable.ic_tech)
            map.put(TRAVEL, R.drawable.ic_travel)
            map.put(PEOPLE, R.drawable.ic_people)
        }

        fun getDrawable(key: String?): Int? {
            if (key == null) return null
            if (map.containsKey(key)) {
                return map[key]
            }
            return null
        }
    }

    var onClickItem: ((string: String) -> Unit)? = null

    override fun onBindViewHolder(holder: CategoryViewHolder?, position: Int) {
        holder?.bind(list[holder.adapterPosition])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(LayoutInflater.from(context).inflate(R.layout.item_search_category, parent, false))
    }

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @BindView(R.id.category_icon)
        @JvmField var categoryIcon: ImageView? = null

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
            val drawable: Int? = ResMap.getDrawable(cate)
            if (drawable != null) {
                categoryIcon?.setImageResource(drawable)
            }
            categoryName?.text = cate
        }
    }
}
