package com.juniperphoton.myersplash.widget

import android.animation.Animator
import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.graphics.RectF
import android.support.design.widget.AppBarLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.android.flexbox.FlexboxLayout
import com.google.android.flexbox.FlexboxLayoutManager
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.adapter.CategoryAdapter
import com.juniperphoton.myersplash.event.RequestSearchEvent
import com.juniperphoton.myersplash.fragment.MainListFragment
import com.juniperphoton.myersplash.model.UnsplashCategory
import com.juniperphoton.myersplash.model.UnsplashImage
import com.juniperphoton.myersplash.utils.AnimatorListenerImpl
import com.juniperphoton.myersplash.utils.ToastService
import org.greenrobot.eventbus.EventBus

@Suppress("UNUSED")
class SearchView(ctx: Context, attrs: AttributeSet) : FrameLayout(ctx, attrs) {

    @BindView(R.id.detail_search_et)
    @JvmField var editText: EditText? = null

    @BindView(R.id.detail_search_root_rl)
    @JvmField var rootRL: ViewGroup? = null

    @BindView(R.id.search_result_root)
    @JvmField var resultRoot: FrameLayout? = null

    @BindView(R.id.search_detail_view)
    @JvmField var detailView: ImageDetailView? = null

    @BindView(R.id.detail_search_btn)
    @JvmField var searchBtn: View? = null

    @BindView(R.id.detail_clear_btn)
    @JvmField var clearBtn: View? = null

    @BindView(R.id.search_tag)
    @JvmField var tagView: TextView? = null

    @BindView(R.id.search_toolbar_layout)
    @JvmField var appBarLayout: AppBarLayout? = null

    @BindView(R.id.search_box)
    @JvmField var searchBox: View? = null

    @BindView(R.id.category_list)
    @JvmField var categoryList: RecyclerView? = null

    private var categoryAdapter: CategoryAdapter? = null

    private val mainListFragment: MainListFragment
    private var animating: Boolean = false

    init {
        LayoutInflater.from(context).inflate(R.layout.search_layout, this)

        ButterKnife.bind(this)

        rootRL!!.setOnTouchListener { _, _ -> true }
        editText!!.setOnKeyListener({ _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                onClickSearch()
                true
            }
            false
        })

        editText!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                if (editText!!.text != null && editText!!.text.toString() != "") {
                    if (searchBtn!!.scaleX != 1f) {
                        toggleSearchButtons(true, true)
                    }
                } else {
                    if (searchBtn!!.scaleX != 0f) {
                        // Ignore
                    }
                }
            }
        })

        val activity = context as AppCompatActivity
        mainListFragment = MainListFragment()
        mainListFragment.setCategory(sSearchCategory, object : MainListFragment.Callback {
            override fun onScrollHide() {}

            override fun onScrollShow() {}

            override fun clickPhotoItem(rectF: RectF, unsplashImage: UnsplashImage, itemView: View) {
                detailView!!.showDetailedImage(rectF, unsplashImage, itemView)
            }
        })
        activity.supportFragmentManager.beginTransaction().replace(R.id.search_result_root, mainListFragment)
                .commit()

        appBarLayout!!.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val fraction = Math.abs(verticalOffset) * 1.0f / appBarLayout.height
            tagView!!.alpha = fraction
        }

        tagView!!.setOnTouchListener { _, _ -> true }

        initCategoryList()
    }

    private fun initCategoryList() {
        categoryAdapter = CategoryAdapter(context, arrayListOf(CategoryAdapter.BUILDINGS,
                CategoryAdapter.FOOD,
                CategoryAdapter.NATURE,
                CategoryAdapter.PEOPLE,
                CategoryAdapter.TECHNOLOGY,
                CategoryAdapter.TRAVEL,
                CategoryAdapter.SEA,
                CategoryAdapter.SKY))
        categoryAdapter!!.onClickItem = { name ->
            editText?.setText(name, TextView.BufferType.EDITABLE)
            editText?.setSelection(name.length, name.length)
            onClickSearch()
        }
        categoryList?.layoutManager = FlexboxLayoutManager()
        categoryList?.adapter = categoryAdapter
    }

    private fun toggleSearchButtons(show: Boolean, animation: Boolean) {
        if (!animation) {
            searchBtn!!.scaleX = if (show) 1f else 0f
            searchBtn!!.scaleY = if (show) 1f else 0f
            clearBtn!!.scaleX = if (show) 1f else 0f
            clearBtn!!.scaleY = if (show) 1f else 0f
        } else {
            if (animating) return
            animating = true
            searchBtn!!.animate().scaleX(if (show) 1f else 0f).scaleY(if (show) 1f else 0f).setDuration(200)
                    .setStartDelay(100)
                    .setListener(object : AnimatorListenerImpl() {
                        override fun onAnimationEnd(animation: Animator) {
                            animating = false
                        }
                    })
                    .start()
            clearBtn!!.animate().scaleX(if (show) 1f else 0f).scaleY(if (show) 1f else 0f).setDuration(200)
                    .start()
        }
    }

    fun onShowing() {
        mainListFragment.register()
        toggleSearchButtons(false, false)
    }

    fun onHiding() {
        mainListFragment.unregister()
        hideKeyboard()
        toggleSearchButtons(false, false)
        tagView!!.animate().alpha(0f).setDuration(100).start()
    }

    fun onShown() {
        val layoutParams = searchBox!!.layoutParams as AppBarLayout.LayoutParams
        layoutParams.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
        searchBox!!.layoutParams = layoutParams
    }

    fun reset() {
        val layoutParams = searchBox!!.layoutParams as AppBarLayout.LayoutParams
        layoutParams.scrollFlags = 0
        searchBox!!.layoutParams = layoutParams
        mainListFragment.scrollToTop()
        mainListFragment.clear()
        editText?.setText("")
        categoryList?.animate()?.alpha(1f)?.setListener(object : AnimatorListenerImpl() {
            override fun onAnimationEnd(animation: Animator?) {
                categoryList?.visibility = View.VISIBLE
            }
        })?.start()
        resultRoot?.visibility = View.GONE
    }

    fun tryShowKeyboard() {
        editText?.post {
            editText!!.requestFocus()
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editText, SHOW_IMPLICIT)
        }
    }

    fun hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText!!.windowToken, 0)
    }

    @OnClick(R.id.detail_search_btn)
    internal fun onClickSearch() {
        hideKeyboard()
        Log.d(TAG, "onClickSearch")
        if (editText!!.text.toString() == "") {
            ToastService.sendShortToast("Input the keyword to search.")
            return
        }
        resultRoot?.visibility = View.VISIBLE
        tagView!!.text = "# " + editText!!.text.toString().toUpperCase()
        EventBus.getDefault().post(RequestSearchEvent(editText!!.text.toString().toLowerCase()))
        categoryList?.animate()?.alpha(0f)?.setListener(object : AnimatorListenerImpl() {
            override fun onAnimationEnd(animation: Animator?) {
                categoryList?.visibility = View.GONE
            }
        })?.start()
    }

    @OnClick(R.id.detail_clear_btn)
    internal fun onClickClear() {
        editText!!.setText("")
        toggleSearchButtons(false, true)
    }

    fun tryHide(): Boolean {
        return detailView!!.tryHide()
    }

    fun registerEventBus() {
        detailView!!.registerEventBus()
    }

    fun unregisterEventBus() {
        detailView!!.unregisterEventBus()
    }

    companion object {
        private val TAG = "SearchView"

        private val sSearchCategory = UnsplashCategory()

        init {
            sSearchCategory.id = UnsplashCategory.SEARCH_ID
        }
    }
}
