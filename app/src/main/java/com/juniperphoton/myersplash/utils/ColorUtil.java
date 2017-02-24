package com.juniperphoton.myersplash.utils;

import android.graphics.Color;

public class ColorUtil {
    public static boolean isColorLight(int color) {
        double luma = getLumaFromColor(color);
        return luma >= 120;
    }

    private static double getLumaFromColor(int color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        return 0.299 * r + 0.587 * g + 0.114 * b;
    }

    public static int getDarkerColor(int color, float alpha) {
        return Color.argb((int) (255 * alpha), Color.red(color), Color.green(color), Color.blue(color));
    }
}
