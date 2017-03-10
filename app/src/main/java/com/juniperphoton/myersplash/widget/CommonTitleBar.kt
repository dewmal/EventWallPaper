package com.juniperphoton.myersplash.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView

import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.utils.ContextUtil

import butterknife.BindView
import butterknife.ButterKnife

class CommonTitleBar(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    @BindView(R.id.back_iv)
    internal var backView: View? = null

    @BindView(R.id.title_tv)
    internal var textView: TextView? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.common_title_bar, this, true)

        ButterKnife.bind(this)

        val array = context.obtainStyledAttributes(attrs, R.styleable.CommonTitleBar)
        val title = array.getString(R.styleable.CommonTitleBar_title)
        textView?.text = title
        array.recycle()

        backView?.setOnClickListener { view ->
            val activity = ContextUtil.getActivity(view)
            activity?.finish()
        }
    }
}
