package com.juniperphoton.myersplash.activity

import android.app.AlertDialog
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SimpleItemAnimator
import android.util.Log
import android.view.View
import android.widget.TextView

import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.adapter.DownloadsListAdapter
import com.juniperphoton.myersplash.model.DownloadItem
import com.juniperphoton.myersplash.utils.DeviceUtil
import com.juniperphoton.myersplash.utils.DisplayUtil

import java.util.ArrayList

import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.juniperphoton.myersplash.RealmCache
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmResults

class ManageDownloadActivity : BaseActivity(), DownloadsListAdapter.DownloadStateChangedCallback {
    @BindView(R.id.activity_manage_download_rv)
    @JvmField var mDownloadsRV: RecyclerView? = null

    @BindView(R.id.activity_downloads_no_item_tv)
    @JvmField var mNoItemTV: TextView? = null

    @BindView(R.id.activity_downloads_more_fab)
    @JvmField var mMoreFAB: FloatingActionButton? = null

    private var mAdapter: DownloadsListAdapter? = null
    private var mItemStatusListener: RealmChangeListener<DownloadItem>? = RealmChangeListener { item ->
        Log.d("manage", "onChange")
        if(item.isValid){
            mAdapter?.updateItem(item)
        }
    }

    private val mListener = RealmChangeListener<RealmResults<DownloadItem>> { updateNoItemVisibility() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_managedownload)
        ButterKnife.bind(this)

        initViews()
    }

    override fun onDestroy() {
        super.onDestroy()
        RealmCache.getInstance().removeAllChangeListeners()
        RealmCache.getInstance().close()
    }

    @OnClick(R.id.activity_downloads_more_fab)
    internal fun onClickMore() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.clear_options_title).setItems(R.array.delete_options) { dialogInterface, i ->
            when (i) {
                0 -> {
                    deleteFromRealm(DownloadItem.DOWNLOAD_STATUS_DOWNLOADING)
                }
                1 -> {
                    deleteFromRealm(DownloadItem.DOWNLOAD_STATUS_OK)
                }
                2 -> {
                    deleteFromRealm(DownloadItem.DOWNLOAD_STATUS_FAILED)
                }
            }
        }.setPositiveButton(R.string.cancel) { dialogInterface, i -> dialogInterface.dismiss() }
        builder.create().show()
    }

    private fun deleteFromRealm(status: Int) {
        var realm = RealmCache.getInstance()
        realm.beginTransaction()
        val items = realm.where(DownloadItem::class.java)
                .equalTo(DownloadItem.STATUS_KEY, status).findAll()
        for (item in items) {
            item.removeChangeListener(mItemStatusListener!!)
            item.deleteFromRealm()
        }
        realm.commitTransaction()
        initViews()
    }

    fun updateNoItemVisibility() {
        if ((mAdapter?.data?.size ?: 0) > 0) {
            mNoItemTV?.visibility = View.GONE
        } else {
            mNoItemTV?.visibility = View.VISIBLE
        }
    }

    private fun initViews() {
        val downloadItems = ArrayList<DownloadItem>()

        val realm = RealmCache.getInstance()
        realm.beginTransaction()

        val items = realm.where(DownloadItem::class.java).findAll()
        for (item in items) {
            downloadItems.add(item)
            item.addChangeListener(mItemStatusListener!!)
        }
        realm.commitTransaction()

        items.addChangeListener(mListener)

        if (mAdapter == null) {
            mAdapter = DownloadsListAdapter(downloadItems, this)
            mAdapter!!.setCallback(this)
        } else {
            mAdapter!!.refreshItems(downloadItems)
        }

        val layoutManager = GridLayoutManager(this, 2)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                if (position == mAdapter!!.itemCount - 1) {
                    return 2
                } else {
                    return 1
                }
            }
        }
        mDownloadsRV!!.layoutManager = layoutManager
        (mDownloadsRV!!.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        mDownloadsRV!!.adapter = mAdapter
        updateNoItemVisibility()

        if (!DeviceUtil.hasNavigationBar(this)) {
            val params = mMoreFAB!!.layoutParams as ConstraintLayout.LayoutParams
            params.setMargins(0, 0, DisplayUtil.getDimenInPixel(24, this),
                    DisplayUtil.getDimenInPixel(24, this))
            mMoreFAB!!.layoutParams = params
        }
    }

    override fun onDataChanged() {
        updateNoItemVisibility()
    }

    override fun onRetryDownload(id: String) {
        //DownloadUtil.checkAndDownload(this,m);
    }
}
