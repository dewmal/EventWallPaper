package com.juniperphoton.myersplash.utils;


import android.content.Context;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;

import com.orhanobut.logger.Logger;

public class DeviceUtil {
    public static boolean checkDeviceHasNavigationBar(Context context) {
        boolean hasBackKey = false;
        try {
            hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
        } catch (java.lang.NoSuchMethodError e) {
            e.printStackTrace();
        }
        Logger.d(String.valueOf(hasBackKey));
        return hasBackKey;
    }
}
