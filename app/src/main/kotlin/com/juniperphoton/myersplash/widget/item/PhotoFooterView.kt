package com.juniperphoton.myersplash.widget.item

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.LinearLayout
import butterknife.BindView
import butterknife.ButterKnife
import com.juniperphoton.myersplash.R

class PhotoFooterView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    @BindView(R.id.row_footer_rl)
    lateinit var footerRL: ViewGroup

    override fun onFinishInflate() {
        super.onFinishInflate()
        ButterKnife.bind(this, this)
    }
}