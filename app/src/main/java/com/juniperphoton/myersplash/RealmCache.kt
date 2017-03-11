package com.juniperphoton.myersplash

import android.content.Context
import io.realm.DynamicRealm
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmMigration

object RealmCache {
    val SCHEMA_VERSION = 2
    private var configuration: RealmConfiguration? = null

    fun init(context: Context) {
        Realm.init(context)
        configuration = RealmConfiguration.Builder()
                .schemaVersion(SCHEMA_VERSION.toLong())
                .migration(CacheMigration())
                .build()
    }

    fun getInstance() = Realm.getInstance(configuration)!!
}

class CacheMigration : RealmMigration {
    override fun migrate(realm: DynamicRealm?, oldVersion: Long, newVersion: Long) {
        val schema = realm!!.schema

        if (oldVersion.toInt() < RealmCache.SCHEMA_VERSION) {
            schema.get("DownloadItem").renameField("mThumbUrl", "thumbUrl")
                    .renameField("mDownloadUrl", "downloadUrl")
                    .renameField("mId", "id")
                    .renameField("mProgress", "progress")
                    .renameField("mColor", "color")
                    .renameField("mStatus", "status")
                    .renameField("mFilePath", "filePath")
                    .renameField("mFileName", "fileName")
        }
    }
}