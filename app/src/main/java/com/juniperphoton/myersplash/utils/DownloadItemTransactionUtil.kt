package com.juniperphoton.myersplash.utils

import com.juniperphoton.myersplash.model.DownloadItem
import io.realm.Realm

object DownloadItemTransactionUtil {
    fun delete(item: DownloadItem) {
        Realm.getDefaultInstance().executeTransaction { realm ->
            var item = realm.where(DownloadItem::class.java).equalTo(DownloadItem.ID_KEY,
                    item.id).findFirst()
            item?.deleteFromRealm()
        }
    }

    fun updateStatus(item: DownloadItem, @DownloadItem.DownloadStatus status: Int) {
        Realm.getDefaultInstance().executeTransaction { realm ->
            var item = realm.where(DownloadItem::class.java).equalTo(DownloadItem.ID_KEY,
                    item.id).findFirst()
            if (item != null) {
                item.status = status
                if (status == DownloadItem.DOWNLOAD_STATUS_FAILED) {
                    item.progress = 0
                }
            }
        }
    }

    fun updateStatus(id: String, @DownloadItem.DownloadStatus status: Int) {
        Realm.getDefaultInstance().executeTransaction { realm ->
            var item = realm.where(DownloadItem::class.java).equalTo(DownloadItem.ID_KEY,
                    id).findFirst()
            if (item != null) {
                item.status = status
                if (status == DownloadItem.DOWNLOAD_STATUS_FAILED) {
                    item.progress = 0
                }
            }
        }
    }
}