package com.juniperphoton.myersplash.widget

import android.animation.Animator
import android.content.Context
import android.graphics.RectF
import android.support.design.widget.AppBarLayout
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView

import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.event.RequestSearchEvent
import com.juniperphoton.myersplash.fragment.MainListFragment
import com.juniperphoton.myersplash.model.UnsplashCategory
import com.juniperphoton.myersplash.model.UnsplashImage
import com.juniperphoton.myersplash.utils.AnimatorListenerImpl
import com.juniperphoton.myersplash.utils.ToastService

import org.greenrobot.eventbus.EventBus

import java.util.ArrayList

import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick

import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT

@Suppress("UNUSED")
class SearchView(private val mContext: Context, attrs: AttributeSet) :
        FrameLayout(mContext, attrs), ViewTreeObserver.OnGlobalLayoutListener {

    @BindView(R.id.detail_search_et)
    @JvmField var mEditText: EditText? = null

    @BindView(R.id.detail_search_root_rl)
    @JvmField var mRootRL: ViewGroup? = null

    @BindView(R.id.search_result_root)
    @JvmField var mResultRoot: FrameLayout? = null

    @BindView(R.id.search_detail_view)
    @JvmField var mDetailView: ImageDetailView? = null

    @BindView(R.id.detail_search_btn)
    @JvmField var mSearchBtn: View? = null

    @BindView(R.id.detail_clear_btn)
    @JvmField var mClearBtn: View? = null

    @BindView(R.id.search_tag)
    @JvmField var mTagView: TextView? = null

    @BindView(R.id.search_toolbar_layout)
    @JvmField var mAppBarLayout: AppBarLayout? = null

    @BindView(R.id.search_box)
    @JvmField var mSearchBox: View? = null

    private val mFragment: MainListFragment
    private var mAnimating: Boolean = false

    init {

        LayoutInflater.from(mContext).inflate(R.layout.search_layout, this)

        ButterKnife.bind(this)

        mRootRL!!.setOnTouchListener { v, event -> true }
        mEditText!!.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                onClickSearch()
                true
            }
            false
        })

        mEditText!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                if (mEditText!!.text != null && mEditText!!.text.toString() != "") {
                    if (mSearchBtn!!.scaleX != 1f) {
                        toggleSearchButtons(true, true)
                    }
                } else {
                    if (mSearchBtn!!.scaleX != 0f) {
                        // Ignore
                    }
                }
            }
        })

        val activity = mContext as AppCompatActivity
        mFragment = MainListFragment()
        mFragment.setCategory(sSearchCategory, object : MainListFragment.Callback {
            override fun onScrollHide() {}

            override fun onScrollShow() {}

            override fun clickPhotoItem(rectF: RectF, unsplashImage: UnsplashImage, itemView: View) {
                mDetailView!!.showDetailedImage(rectF, unsplashImage, itemView)
            }
        })
        activity.supportFragmentManager.beginTransaction().replace(R.id.search_result_root, mFragment)
                .commit()

        mAppBarLayout!!.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val fraction = Math.abs(verticalOffset) * 1.0f / appBarLayout.height
            mTagView!!.alpha = fraction
        }

        mTagView!!.setOnTouchListener { v, event -> true }
    }

    private fun toggleSearchButtons(show: Boolean, animation: Boolean) {
        if (!animation) {
            mSearchBtn!!.scaleX = if (show) 1f else 0f
            mSearchBtn!!.scaleY = if (show) 1f else 0f
            mClearBtn!!.scaleX = if (show) 1f else 0f
            mClearBtn!!.scaleY = if (show) 1f else 0f
        } else {
            if (mAnimating) return
            mAnimating = true
            mSearchBtn!!.animate().scaleX(if (show) 1f else 0f).scaleY(if (show) 1f else 0f).setDuration(200)
                    .setStartDelay(100)
                    .setListener(object : AnimatorListenerImpl() {
                        override fun onAnimationEnd(animation: Animator) {
                            mAnimating = false
                        }
                    })
                    .start()
            mClearBtn!!.animate().scaleX(if (show) 1f else 0f).scaleY(if (show) 1f else 0f).setDuration(200)
                    .start()
        }
    }

    fun onShowing() {
        mFragment.register()
        toggleSearchButtons(false, false)
    }

    fun onHiding() {
        mFragment.unregister()
        hideKeyboard()
        toggleSearchButtons(false, false)
        mTagView!!.animate().alpha(0f).setDuration(100).start()
    }

    fun onShown() {
        val layoutParams = mSearchBox!!.layoutParams as AppBarLayout.LayoutParams
        layoutParams.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
        mSearchBox!!.layoutParams = layoutParams
    }

    fun reset() {
        val layoutParams = mSearchBox!!.layoutParams as AppBarLayout.LayoutParams
        layoutParams.scrollFlags = 0
        mSearchBox!!.layoutParams = layoutParams
        mFragment.scrollToTop()
        mFragment.clear()
        mEditText!!.setText("")
    }

    fun showKeyboard() {
        mEditText!!.viewTreeObserver.addOnGlobalLayoutListener(this)
    }

    fun hideKeyboard() {
        val imm = mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(mEditText!!.windowToken, 0)
    }

    @OnClick(R.id.detail_search_btn)
    internal fun onClickSearch() {
        hideKeyboard()
        Log.d(TAG, "onClickSearch")
        if (mEditText!!.text.toString() == "") {
            ToastService.sendShortToast("Input the keyword to search.")
            return
        }
        mTagView!!.text = "# " + mEditText!!.text.toString().toUpperCase()
        EventBus.getDefault().post(RequestSearchEvent(mEditText!!.text.toString()))
    }

    @OnClick(R.id.detail_clear_btn)
    internal fun onClickClear() {
        mEditText!!.setText("")
        toggleSearchButtons(false, true)
    }

    fun tryHide(): Boolean {
        return mDetailView!!.tryHide()
    }

    override fun onGlobalLayout() {
        mEditText!!.requestFocus()
        val imm = mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(mEditText, SHOW_IMPLICIT)
        mEditText!!.viewTreeObserver.removeOnGlobalLayoutListener(this)
    }

    fun registerEventBus() {
        mDetailView!!.registerEventBus()
    }

    fun unregisterEventBus() {
        mDetailView!!.unregisterEventBus()
    }

    companion object {
        private val TAG = "SearchView"

        private val sSearchCategory = UnsplashCategory()

        init {
            sSearchCategory.id = UnsplashCategory.SEARCH_ID
        }
    }
}
