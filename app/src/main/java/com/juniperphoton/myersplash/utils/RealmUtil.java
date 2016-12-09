package com.juniperphoton.myersplash.utils;


import io.realm.Realm;
import io.realm.RealmConfiguration;

public class RealmUtil {
    public static Realm GetDefaultInstance() {
        return Realm.getDefaultInstance();
    }
}
