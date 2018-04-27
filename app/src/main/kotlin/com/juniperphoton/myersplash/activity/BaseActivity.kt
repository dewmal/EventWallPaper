package com.juniperphoton.myersplash.activity

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.extension.getStatusBarHeight
import com.juniperphoton.myersplash.extension.hasNavigationBar

open class BaseActivity : AppCompatActivity() {
    private val isChrome: Boolean
        get() = Build.BRAND == "chromium" || Build.BRAND == "chrome"

    private var navigationBarConfigured = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= 19 && !isChrome) {
            window.decorView.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }

        if (Build.VERSION.SDK_INT >= 21) {
            window.statusBarColor = Color.TRANSPARENT
        }
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        configureStatusBar()
    }

    override fun onStart() {
        super.onStart()
        if (!navigationBarConfigured) {
            navigationBarConfigured = true
            onConfigNavigationBar(hasNavigationBar())
        }
    }

    open fun onConfigNavigationBar(hasNavigationBar: Boolean) = Unit

    private fun configureStatusBar() {
        val window = window
        val decorView = window.decorView ?: return
        val placeholder = createStatusBarPlaceholder()
        when (decorView) {
            is LinearLayout -> {
                decorView.addView(placeholder, 0)
            }
            else -> {
                (decorView as ViewGroup).addView(placeholder)
            }
        }
    }

    private fun createStatusBarPlaceholder(): View {
        return View(this, null).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    getStatusBarHeight())
            background = ColorDrawable(ContextCompat.getColor(context, R.color.StatusBarColor))
        }
    }
}
