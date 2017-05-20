package com.juniperphoton.myersplash.activity

import android.app.AlertDialog
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.SimpleItemAnimator
import android.util.Log
import android.view.View
import butterknife.ButterKnife
import butterknife.OnClick
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.RealmCache
import com.juniperphoton.myersplash.adapter.DownloadsListAdapter
import com.juniperphoton.myersplash.extension.getDimenInPixel
import com.juniperphoton.myersplash.extension.hasNavigationBar
import com.juniperphoton.myersplash.model.DownloadItem
import io.realm.RealmChangeListener
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_managedownload.*
import java.util.*

@Suppress("UNUSED")
class ManageDownloadActivity : BaseActivity() {
    private var adapter: DownloadsListAdapter? = null

    private var itemStatusChangedListener: RealmChangeListener<DownloadItem>? = RealmChangeListener { item ->
        Log.d("manage", "onChange")
        if (item.isValid) {
            adapter?.updateItem(item)
        }
    }

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

    @OnClick(R.id.downloadsMoreFab)
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
        RealmCache.getInstance().executeTransaction {
            val result = it.where(DownloadItem::class.java)
                    .equalTo(DownloadItem.STATUS_KEY, status).findAll()
            result.toList().forEach {
                it.removeAllChangeListeners()
                it.deleteFromRealm()
            }
            it.commitTransaction()
            initViews()
        }
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
        items.forEach {
            downloadItems.add(it)
            it.addChangeListener(itemStatusChangedListener!!)
        }

        if (adapter == null) {
            adapter = DownloadsListAdapter(this)
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
        downloadsList.layoutManager = layoutManager
        (downloadsList.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        downloadsList.adapter = adapter
        updateNoItemVisibility()

        if (!hasNavigationBar()) {
            val params = downloadsMoreFab.layoutParams as ConstraintLayout.LayoutParams
            params.setMargins(0, 0, getDimenInPixel(24), getDimenInPixel(24))
            downloadsMoreFab.layoutParams = params
        }
    }
}
