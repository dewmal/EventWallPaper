package com.juniperphoton.myersplash.utils;


import com.juniperphoton.myersplash.model.DownloadItem;

import io.realm.Realm;

public class DownloadItemTransactionHelper {
    public static void delete(final DownloadItem item) {
        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                DownloadItem realmItem = realm.where(DownloadItem.class).equalTo("mId", item.getId()).findFirst();
                if (realmItem != null) {
                    realmItem.deleteFromRealm();
                }
            }
        });
    }

    public static void updateStatus(final DownloadItem item, final DownloadItem.DownloadStatus status) {
        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                DownloadItem realmItem = realm.where(DownloadItem.class).equalTo("mId", item.getId()).findFirst();
                if (realmItem != null) {
                    realmItem.setStatus(status);
                    if (status == DownloadItem.DownloadStatus.Failed) {
                        realmItem.setProgress(0);
                    }
                }
            }
        });
    }
}
