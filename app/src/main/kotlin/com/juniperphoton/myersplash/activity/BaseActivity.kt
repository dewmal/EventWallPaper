package com.juniperphoton.myersplash.activity

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
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
    }

    override fun onStart() {
        super.onStart()
        if (!navigationBarConfigured) {
            navigationBarConfigured = true
            onConfigNavigationBar(hasNavigationBar())
        }
    }

    open fun onConfigNavigationBar(hasNavigationBar: Boolean) = Unit
}
