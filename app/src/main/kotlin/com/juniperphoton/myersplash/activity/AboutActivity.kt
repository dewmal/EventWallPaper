package com.juniperphoton.myersplash.activity

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Rect
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
import com.juniperphoton.myersplash.extension.getVersionName
import com.juniperphoton.myersplash.extension.hasNavigationBar
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
    private val marginLeft by lazy {
        resources.getDimensionPixelSize(R.dimen.about_item_margin_left)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        ButterKnife.bind(this)

        updateVersion()
        initThanks()
        if (!hasNavigationBar()) {
            blank?.visibility = View.GONE
        }
    }

    private fun updateVersion() {
        versionTextView?.text = getVersionName()
    }

    private fun initThanks() {
        adapter = ThanksToAdapter(this)
        val strs = resources.getStringArray(R.array.thanks_array)
        val list = strs.toList()
        adapter?.refresh(list)
        recyclerView?.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect?, view: View?, parent: RecyclerView?, state: RecyclerView.State?) {
                super.getItemOffsets(outRect, view, parent, state)
                val pos = parent?.getChildAdapterPosition(view)
                if (pos == 0) {
                    outRect?.left = marginLeft
                } else if (pos == (parent?.adapter?.itemCount?.minus(1))) {
                    outRect?.right = marginLeft
                }
            }
        })
        recyclerView?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView?.adapter = adapter
    }

    @OnClick(R.id.email_rl)
    internal fun onClickEmail() {
        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.type = "message/rfc822"
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.email_url)))

        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "MyerSplash for Android ${getVersionName()} feedback")
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
            AlipayZeroSdk.startAlipayClient(this, getString(R.string.alipay_url_code))
        }
    }

    @OnClick(R.id.github_layout)
    internal fun onClickGitHub() {
        val uri = Uri.parse(getString(R.string.github_url))
        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    @OnClick(R.id.twitter_layout)
    internal fun onClickTwitter() {
        val uri = Uri.parse(getString(R.string.twitter_url))
        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }
}