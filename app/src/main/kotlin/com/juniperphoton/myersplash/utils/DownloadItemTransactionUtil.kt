package com.juniperphoton.myersplash.utils

import com.juniperphoton.myersplash.RealmCache
import com.juniperphoton.myersplash.model.DownloadItem

object DownloadItemTransactionUtil {
    fun delete(item: DownloadItem) {
        RealmCache.getInstance().executeTransaction { realm ->
            val managedItem = realm.where(DownloadItem::class.java).equalTo(DownloadItem.ID_KEY,
                    item.id).findFirst()
            managedItem?.deleteFromRealm()
        }
    }

    fun updateStatus(item: DownloadItem, @DownloadItem.DownloadStatus status: Int) {
        RealmCache.getInstance().executeTransaction { realm ->
            val managedItem = realm.where(DownloadItem::class.java).equalTo(DownloadItem.ID_KEY,
                    item.id).findFirst()
            if (managedItem != null) {
                managedItem.status = status
                if (status == DownloadItem.DOWNLOAD_STATUS_FAILED) {
                    managedItem.progress = 0
                }
            }
        }
    }

    fun updateStatus(id: String, @DownloadItem.DownloadStatus status: Int) {
        RealmCache.getInstance().executeTransaction { realm ->
            val managedItem = realm.where(DownloadItem::class.java).equalTo(DownloadItem.ID_KEY,
                    id).findFirst()
            if (managedItem != null) {
                managedItem.status = status
                if (status == DownloadItem.DOWNLOAD_STATUS_FAILED) {
                    managedItem.progress = 0
                }
            }
        }
    }
}