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
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.RealmCache
import com.juniperphoton.myersplash.adapter.DownloadsListAdapter
import com.juniperphoton.myersplash.extension.getDimenInPixel
import com.juniperphoton.myersplash.extension.hasNavigationBar
import com.juniperphoton.myersplash.model.DownloadItem
import io.realm.RealmChangeListener
import io.realm.RealmResults
import io.realm.Sort
import java.util.*

@Suppress("UNUSED")
class ManageDownloadActivity : BaseActivity(), DownloadsListAdapter.DownloadStateChangedCallback {
    @BindView(R.id.activity_manage_download_rv)
    @JvmField var recyclerView: RecyclerView? = null

    @BindView(R.id.activity_downloads_no_item_tv)
    @JvmField var noItemView: TextView? = null

    @BindView(R.id.activity_downloads_more_fab)
    @JvmField var moreFAB: FloatingActionButton? = null

    private var adapter: DownloadsListAdapter? = null

    private var itemStatusChangedListener: RealmChangeListener<DownloadItem>? = RealmChangeListener { item ->
        Log.d("manage", "onChange")
        if (item.isValid) {
            adapter?.updateItem(item)
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
        builder.setTitle(R.string.clear_options_title).setItems(R.array.delete_options) { _, i ->
            when (i) {
                0 -> deleteFromRealm(DownloadItem.DOWNLOAD_STATUS_DOWNLOADING)
                1 -> deleteFromRealm(DownloadItem.DOWNLOAD_STATUS_OK)
                2 -> deleteFromRealm(DownloadItem.DOWNLOAD_STATUS_FAILED)
            }
        }.setPositiveButton(R.string.cancel) { dialogInterface, _ -> dialogInterface.dismiss() }
        builder.create().show()
    }

    private fun deleteFromRealm(status: Int) {
        val realm = RealmCache.getInstance()
        realm.beginTransaction()
        val items = realm.where(DownloadItem::class.java)
                .equalTo(DownloadItem.STATUS_KEY, status).findAll()
        items.map {
            it.removeChangeListener(itemStatusChangedListener!!)
            it.deleteFromRealm()
        }
        realm.commitTransaction()
        initViews()
    }

    fun updateNoItemVisibility() {
        if ((adapter?.data?.size ?: 0) > 0) {
            noItemView?.visibility = View.GONE
        } else {
            noItemView?.visibility = View.VISIBLE
        }
    }

    private fun initViews() {
        val downloadItems = ArrayList<DownloadItem>()

        val items = RealmCache.getInstance().where(DownloadItem::class.java).findAllSorted(DownloadItem.POSITION_KEY, Sort.DESCENDING)
        for (item in items) {
            downloadItems.add(item)
            item.addChangeListener(itemStatusChangedListener!!)
        }

        items.addChangeListener(mListener)

        if (adapter == null) {
            adapter = DownloadsListAdapter(this)
            adapter!!.setCallback(this)
        }
        adapter!!.refreshItems(downloadItems)

        val layoutManager = GridLayoutManager(this, 2)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                if (position == adapter!!.itemCount - 1) {
                    return 2
                } else {
                    return 1
                }
            }
        }
        recyclerView!!.layoutManager = layoutManager
        (recyclerView!!.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        recyclerView!!.adapter = adapter
        updateNoItemVisibility()

        if (!hasNavigationBar()) {
            val params = moreFAB!!.layoutParams as ConstraintLayout.LayoutParams
            params.setMargins(0, 0, getDimenInPixel(24), getDimenInPixel(24))
            moreFAB!!.layoutParams = params
        }
    }

    override fun onDataChanged() {
        updateNoItemVisibility()
    }

    override fun onRetryDownload(id: String) {
    }
}
