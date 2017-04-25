package com.juniperphoton.myersplash.adapter

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.facebook.drawee.view.SimpleDraweeView
import com.juniperphoton.flipperviewlib.FlipperView
import com.juniperphoton.myersplash.App
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.model.DownloadItem
import com.juniperphoton.myersplash.service.BackgroundDownloadService
import com.juniperphoton.myersplash.utils.DownloadItemTransactionUtil
import com.juniperphoton.myersplash.utils.Params
import com.juniperphoton.myersplash.widget.DownloadCompleteView
import com.juniperphoton.myersplash.widget.DownloadRetryView
import com.juniperphoton.myersplash.widget.DownloadingView

import butterknife.BindView
import butterknife.ButterKnife

class DownloadsListAdapter(private var mData: MutableList<DownloadItem>?, private val mContext: Context) :
        RecyclerView.Adapter<DownloadsListAdapter.DownloadItemViewHolder>() {

    private var mCallback: DownloadStateChangedCallback? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadItemViewHolder? {
        if (viewType == ITEM) {
            val view = LayoutInflater.from(mContext).inflate(R.layout.row_download_item, parent, false)
            val width = mContext.resources.displayMetrics.widthPixels
            val params = view.layoutParams
            params.height = (width / 1.7).toInt()
            view.layoutParams = params
            return DownloadItemViewHolder(view)
        } else if (viewType == FOOTER) {
            val footer = LayoutInflater.from(mContext).inflate(R.layout.row_footer_blank, parent, false)
            return DownloadItemViewHolder(footer)
        }
        return null
    }

    override fun onBindViewHolder(holder: DownloadItemViewHolder, position: Int) {
        if (getItemViewType(position) == FOOTER) {
            return
        }
        val item = mData!![holder.adapterPosition]

        holder.downloadCompleteView?.setFilePath(item.filePath?:"")
        holder.downloadCompleteView?.setThemeBackColor(item.color)

        holder.draweeView?.setImageURI(item.thumbUrl)
        holder.downloadingView?.setProgress(item.progress)

        holder.downloadRetryView?.setThemeBackColor(item.color)
        holder.downloadRetryView?.setOnClickDeleteListener(View.OnClickListener {
            try {
                mData!!.removeAt(holder.adapterPosition)
                notifyItemRemoved(holder.adapterPosition)

                val intent = Intent(App.instance, BackgroundDownloadService::class.java)
                intent.putExtra(Params.CANCELED_KEY, true)
                intent.putExtra(Params.URL_KEY, item.downloadUrl)
                mContext.startService(intent)

                DownloadItemTransactionUtil.delete(item)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
        holder.downloadRetryView?.setOnClickRetryListener(View.OnClickListener {
            DownloadItemTransactionUtil.updateStatus(item, DownloadItem.DOWNLOAD_STATUS_DOWNLOADING)
            holder.flipperView?.next(item.status)

            val intent = Intent(mContext, BackgroundDownloadService::class.java)
            intent.putExtra(Params.NAME_KEY, item.fileName)
            intent.putExtra(Params.URL_KEY, item.downloadUrl)
            mContext.startService(intent)
        })

        holder.downloadingView?.setProgress(item.progress)
        holder.downloadingView?.setThemeBackColor(item.color)
        holder.downloadingView?.setClickCancelListener(View.OnClickListener {
            DownloadItemTransactionUtil.updateStatus(item, DownloadItem.DOWNLOAD_STATUS_FAILED)
            holder.flipperView?.next(item.status)

            val intent = Intent(App.instance, BackgroundDownloadService::class.java)
            intent.putExtra(Params.CANCELED_KEY, true)
            intent.putExtra(Params.URL_KEY, item.downloadUrl)
            mContext.startService(intent)
        })

        val last = item.lastStatus
        if (last != item.status && last != DownloadItem.DISPLAY_STATUS_NOT_SPECIFIED) {
            holder.flipperView?.next(item.status)
            item.syncStatus()
        } else {
            holder.flipperView?.next(item.status, false)
            item.syncStatus()
        }
    }

    fun setCallback(callback: DownloadStateChangedCallback) {
        mCallback = callback
    }

    private fun removeItem(item: DownloadItem) {
        for (downloadItem in mData!!) {
            if (downloadItem.id == item.id) {
                val index = mData!!.indexOf(downloadItem)
                mData!!.removeAt(index)
                break
            }
        }
        if (mCallback != null) {
            mCallback!!.onDataChanged()
        }
    }

    override fun getItemCount(): Int {
        if (mData == null)
            return 0
        else
            return mData!!.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        if (position >= itemCount - 1) {
            return FOOTER
        } else
            return ITEM
    }

    inner class DownloadItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @BindView(R.id.row_download_item_dv)
        @JvmField var draweeView: SimpleDraweeView? = null

        @BindView(R.id.row_download_flipper_view)
        @JvmField var flipperView: FlipperView? = null

        @BindView(R.id.row_downloading_view)
        @JvmField var downloadingView: DownloadingView? = null

        @BindView(R.id.row_download_retry_view)
        @JvmField var downloadRetryView: DownloadRetryView? = null

        @BindView(R.id.row_download_complete_view)
        @JvmField var downloadCompleteView: DownloadCompleteView? = null

        init {
            ButterKnife.bind(this, itemView)
        }
    }

    fun updateItem(item: DownloadItem) {
        val index = mData!!.indexOf(item)
        if (index >= 0 && index <= mData!!.size) {
            Log.d("adapter", "notifyItemChanged:" + index)
            notifyItemChanged(index)
        }
    }

    fun refreshItems(items: MutableList<DownloadItem>) {
        mData = items
        notifyDataSetChanged()
    }

    fun clear() {
        mData!!.clear()
        notifyDataSetChanged()
    }

    val data: List<DownloadItem>?
        get() = mData

    interface DownloadStateChangedCallback {
        fun onDataChanged()

        fun onRetryDownload(id: String)
    }

    companion object {
        private val ITEM = 1
        private val FOOTER = 1 shl 1
    }
}
