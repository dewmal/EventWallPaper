package com.juniperphoton.myersplash.utils;

import com.juniperphoton.myersplash.model.DownloadItem;

import io.realm.Realm;

public class DownloadItemTransactionHelper {
    public static void delete(final DownloadItem item) {
        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                DownloadItem realmItem = realm.where(DownloadItem.class).equalTo(DownloadItem.ID_KEY,
                        item.getId()).findFirst();
                if (realmItem != null) {
                    realmItem.deleteFromRealm();
                }
            }
        });
    }

    public static void updateStatus(final DownloadItem item, @DownloadItem.DownloadStatus final int status) {
        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                DownloadItem realmItem = realm.where(DownloadItem.class).equalTo(DownloadItem.ID_KEY,
                        item.getId()).findFirst();
                if (realmItem != null) {
                    realmItem.setStatus(status);
                    if (status == DownloadItem.DOWNLOAD_STATUS_FAILED) {
                        realmItem.setProgress(0);
                    }
                }
            }
        });
    }
}
