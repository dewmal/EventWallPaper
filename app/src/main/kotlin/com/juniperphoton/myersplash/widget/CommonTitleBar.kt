package com.juniperphoton.myersplash.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.extension.getActivity

class CommonTitleBar(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    @BindView(R.id.back_iv)
    lateinit var backView: View

    @BindView(R.id.title_tv)
    lateinit var textView: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.common_title_bar, this, true)

        ButterKnife.bind(this)

        val array = context.obtainStyledAttributes(attrs, R.styleable.CommonTitleBar)
        val title = array.getString(R.styleable.CommonTitleBar_title)
        array.recycle()

        textView.text = title
        backView.setOnClickListener { view ->
            val activity = view.getActivity()
            activity?.finish()
        }
    }
}
