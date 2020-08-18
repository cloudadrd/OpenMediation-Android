package com.nbmediation.sdk.utils;

import android.content.res.Resources;
import android.util.DisplayMetrics;

public class Dips {

    public static DisplayMetrics getDisplayMetrics() {
        return Resources.getSystem().getDisplayMetrics();
    }


    public static float dipsToFloatPixels(final float dips) {
        return dips * getDensity();
    }


    public static int dpTopx(final float dips) {
        return (int) (dipsToFloatPixels(dips) + 0.5f);
    }


    private static float getDensity() {
        return getDisplayMetrics().density;
    }

}