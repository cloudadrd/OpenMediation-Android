package com.nbmediation.sdk.mobileads;

import android.app.Activity;
import android.util.TypedValue;

public final class FyberUtil {

    public static int dpToPixels(Activity activity, int dpSize) {
        if (activity == null) {
            return dpSize;
        } else {
            return (int) TypedValue.applyDimension(1, (float) dpSize, activity.getResources().getDisplayMetrics());
        }
    }
}
