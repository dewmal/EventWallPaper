package com.juniperphoton.myersplash.activity

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.adapter.ThanksToAdapter
import com.juniperphoton.myersplash.utils.DeviceUtil
import com.juniperphoton.myersplash.utils.PackageUtil
import moe.feng.alipay.zerosdk.AlipayZeroSdk

@Suppress("UNUSED")
class AboutActivity : BaseActivity() {
    @BindView(R.id.version_tv)
    @JvmField var versionTextView: TextView? = null

    @BindView(R.id.thanks_to_list)
    @JvmField var recyclerView: RecyclerView? = null

    @BindView(R.id.blank_footer)
    @JvmField var blank: View? = null

    private var adapter: ThanksToAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        ButterKnife.bind(this)

        updateVersion()
        initThanks()
        if (!DeviceUtil.hasNavigationBar(this)) {
            blank?.visibility = View.GONE
        }
    }

    private fun updateVersion() {
        versionTextView?.text = PackageUtil.getVersionName(this)
    }

    private fun initThanks() {
        adapter = ThanksToAdapter(this)
        var strs = resources.getStringArray(R.array.thanks_array)
        var list = strs.toList()
        adapter?.refresh(list)
        recyclerView?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView?.adapter = adapter
    }

    @OnClick(R.id.email_rl)
    internal fun onClickEmail() {
        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.type = "message/rfc822"
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("dengweichao@hotmail.com"))

        val SHARE_SUBJECT = "MyerSplash for Android %s feedback"
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, String.format(SHARE_SUBJECT,
                PackageUtil.getVersionName(this)))
        emailIntent.putExtra(Intent.EXTRA_TEXT, "")

        try {
            startActivity(emailIntent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    @OnClick(R.id.activity_about_rate_rl)
    internal fun onClickRate() {
        val uri = Uri.parse("market://details?id=" + packageName)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    @OnClick(R.id.activity_about_donate_rl)
    internal fun onClickDonate() {
        if (AlipayZeroSdk.hasInstalledAlipayClient(this)) {
            AlipayZeroSdk.startAlipayClient(this, "aex09127b4dbo4o7fbvcyb0")
        }
    }

    @OnClick(R.id.github_layout)
    internal fun onClickGitHub() {
        val uri = Uri.parse("https://github.com/JuniperPhoton/MyerSplashAndroid")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    @OnClick(R.id.twitter_layout)
    internal fun onClickTwitter() {
        val uri = Uri.parse("https://twitter.com/juniperphoton")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }
}