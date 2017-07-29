package com.juniperphoton.myersplash.adapter

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.BindView
import butterknife.ButterKnife
import com.facebook.drawee.view.SimpleDraweeView
import com.juniperphoton.flipperview.FlipperView
import com.juniperphoton.myersplash.App
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.model.DownloadItem
import com.juniperphoton.myersplash.service.DownloadService
import com.juniperphoton.myersplash.utils.DownloadItemTransactionUtil
import com.juniperphoton.myersplash.utils.Params
import com.juniperphoton.myersplash.widget.DownloadCompleteView
import com.juniperphoton.myersplash.widget.DownloadRetryView
import com.juniperphoton.myersplash.widget.DownloadingView

class DownloadsListAdapter(private val context: Context) :
        RecyclerView.Adapter<DownloadsListAdapter.DownloadItemViewHolder>() {
    companion object {
        private val ITEM = 1
        private val FOOTER = 1 shl 1
        private val ASPECT_RATIO = 1.7f
    }

    var data: MutableList<DownloadItem>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadItemViewHolder? {
        if (viewType == ITEM) {
            val view = LayoutInflater.from(context).inflate(R.layout.row_download_item, parent, false)
            val width = context.resources.displayMetrics.widthPixels
            val params = view.layoutParams
            params.height = (width / ASPECT_RATIO).toInt()
            view.layoutParams = params
            return DownloadItemViewHolder(view)
        } else if (viewType == FOOTER) {
            val footer = LayoutInflater.from(context).inflate(R.layout.row_footer_blank, parent, false)
            return DownloadItemViewHolder(footer)
        }
        return null
    }

    override fun onBindViewHolder(holder: DownloadItemViewHolder, position: Int) {
        if (getItemViewType(position) == FOOTER) {
            return
        }
        val item = data!![holder.adapterPosition]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        if (data == null)
            return 0
        else
            return data!!.size + 1
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

        internal fun bind(item: DownloadItem) {
            draweeView?.setImageURI(item.thumbUrl)
            downloadingView?.progress = item.progress

            downloadCompleteView?.let {
                it.filePath = item.filePath
                it.setThemeBackColor(item.color)
            }

            downloadRetryView?.let {
                it.themeColor = item.color
                it.onClickDelete = {
                    try {
                        data!!.removeAt(adapterPosition)
                        notifyItemRemoved(adapterPosition)

                        val intent = Intent(App.instance, DownloadService::class.java)
                        intent.putExtra(Params.CANCELED_KEY, true)
                        intent.putExtra(Params.URL_KEY, item.downloadUrl)
                        context.startService(intent)

                        DownloadItemTransactionUtil.delete(item)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                it.onClickRetry = {
                    DownloadItemTransactionUtil.updateStatus(item, DownloadItem.DOWNLOAD_STATUS_DOWNLOADING)
                    flipperView?.next(item.status)

                    val intent = Intent(context, DownloadService::class.java)
                    intent.putExtra(Params.NAME_KEY, item.fileName)
                    intent.putExtra(Params.URL_KEY, item.downloadUrl)
                    context.startService(intent)
                }
            }

            downloadingView?.let {
                it.progress = item.progress
                it.themeColor = item.color
                it.onClickCancel = {
                    DownloadItemTransactionUtil.updateStatus(item, DownloadItem.DOWNLOAD_STATUS_FAILED)
                    flipperView?.next(item.status)

                    val intent = Intent(App.instance, DownloadService::class.java)
                    intent.putExtra(Params.CANCELED_KEY, true)
                    intent.putExtra(Params.URL_KEY, item.downloadUrl)
                    context.startService(intent)
                }
            }

            val last = item.lastStatus
            if (last != item.status && last != DownloadItem.DISPLAY_STATUS_NOT_SPECIFIED) {
                flipperView?.next(item.status)
                item.syncStatus()
            } else {
                flipperView?.next(item.status, false)
                item.syncStatus()
            }
        }
    }

    fun updateItem(item: DownloadItem) {
        val index = data!!.indexOf(item)
        if (index >= 0 && index <= data!!.size) {
            Log.d("adapter", "notifyItemChanged:" + index)
            notifyItemChanged(index)
        }
    }

    fun refreshItems(items: MutableList<DownloadItem>) {
        data = items
        notifyDataSetChanged()
    }
}
