// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.util.Log;

import com.nbmediation.sdk.BuildConfig;

/**
 *
 */
public class AdLog {
    private static class AdLogHolder {
        private static final AdLog INSTANCE = new AdLog();
    }

    public static AdLog getSingleton() {
        return AdLogHolder.INSTANCE;
    }

    private boolean isDebug = BuildConfig.DEBUG;
    private static final String TAG = "Om_" + BuildConfig.VERSION_NAME;

    private AdLog() {

    }

    public void init(Context context) {
        if (isDebug) {
            return;
        }
        isDebug = context != null && (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }

    public void isDebug(boolean isDebug) {
        this.isDebug = isDebug;
    }

    public void LogD(String info) {
        if (isDebug) {
            Log.d(TAG, info);
        }
    }

    public void LogD(String tag, String info) {
        if (isDebug) {
            Log.d(TAG + ":" + tag, info);
        }
    }

    public void LogD(String info, Throwable t) {
        if (isDebug) {
            Log.d(TAG, info, t);
        }
    }

    public void LogE(String info) {
        Log.e(TAG, info);
    }

    public void LogE(String info, Throwable throwable) {
        Log.e(TAG, info, throwable);
    }
}
