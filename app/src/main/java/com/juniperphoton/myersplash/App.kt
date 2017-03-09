package com.juniperphoton.myersplash

import android.app.Application
import com.facebook.drawee.backends.pipeline.Fresco
import io.realm.Realm

class App : Application() {
    companion object {
        lateinit var instance: App
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Fresco.initialize(this)
        Realm.init(this)
    }
}