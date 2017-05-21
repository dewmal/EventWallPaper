package com.juniperphoton.myersplash.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.CompoundButton
import android.widget.FrameLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.juniperphoton.myersplash.R

class SettingsItemLayout(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    @BindView(R.id.settings_item_title)
    @JvmField var titleTextView: TextView? = null

    @BindView(R.id.settings_item_content)
    @JvmField var contentTextView: TextView? = null

    @BindView(R.id.settings_item_switch)
    @JvmField var compoundButton: CompoundButton? = null

    @BindView(R.id.divider_view)
    @JvmField var dividerView: View? = null

    var onCheckedChanged: ((Boolean) -> Unit)? = null

    var checked: Boolean
        get() = compoundButton!!.isChecked
        set(checked) {
            compoundButton!!.isChecked = checked
        }

    var title: String = ""
        set(value) {
            titleTextView?.text = value
        }

    var content: String = ""
        set(value) {
            contentTextView?.text = value
        }

    init {
        LayoutInflater.from(context).inflate(R.layout.row_settings_item, this, true)

        ButterKnife.bind(this)

        val array = context.obtainStyledAttributes(attrs, R.styleable.SettingsItemLayout)
        val title = array.getString(R.styleable.SettingsItemLayout_setting_title)
        val content = array.getString(R.styleable.SettingsItemLayout_setting_content)
        val hasCheckbox = array.getBoolean(R.styleable.SettingsItemLayout_has_checkbox, false)
        val showDivider = array.getBoolean(R.styleable.SettingsItemLayout_show_divider, true)
        array.recycle()

        if (title != null) {
            titleTextView?.text = title
        }

        if (content != null) {
            contentTextView?.text = content
        }

        if (!hasCheckbox) {
            compoundButton?.visibility = View.GONE
        }

        if (!showDivider) {
            dividerView?.visibility = View.GONE
        }

        compoundButton?.setOnCheckedChangeListener { _, isChecked ->
            onCheckedChanged?.invoke(isChecked)
        }
    }
}
