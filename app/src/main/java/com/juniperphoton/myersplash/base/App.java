package com.juniperphoton.myersplash.base;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;

import io.realm.Realm;

public class App extends Application {
    private static App instance;

    public static App getInstance() {
        return instance;
    }

    /**
     * 初始化数据
     */
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Fresco.initialize(this);
        Realm.init(this);
    }
}
