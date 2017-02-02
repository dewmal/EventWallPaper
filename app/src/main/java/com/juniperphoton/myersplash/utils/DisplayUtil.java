package com.juniperphoton.myersplash.utils;

import android.content.Context;

public class DisplayUtil {

    public static float getDpi(Context context) {
        return context.getResources().getDisplayMetrics().density;
    }

    public static int getDimenInPixel(int valueInDP, Context context) {
        return (int) (valueInDP * getDpi(context));
    }
}
