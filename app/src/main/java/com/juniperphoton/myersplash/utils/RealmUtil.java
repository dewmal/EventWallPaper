package com.juniperphoton.myersplash.utils;

import io.realm.Realm;

public class RealmUtil {
    public static Realm GetDefaultInstance() {
        return Realm.getDefaultInstance();
    }
}
