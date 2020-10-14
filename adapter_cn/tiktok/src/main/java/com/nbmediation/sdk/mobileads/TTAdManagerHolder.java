// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.mobileads;

import android.content.Context;

import com.bytedance.sdk.openadsdk.TTAdConfig;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdSdk;

import java.util.concurrent.atomic.AtomicBoolean;

public class TTAdManagerHolder {

    private static AtomicBoolean sInit = new AtomicBoolean(false);

    public static TTAdManager get() {
        if (!sInit.get()) {
            throw new RuntimeException("TTAdSdk is not init, please check.");
        }
        return TTAdSdk.getAdManager();
    }

    public static void init(Context context, String appId) {
        if (context == null) {
            return;
        }
        doInit(context, appId);
    }

    private static void doInit(Context context, String appId) {
        if (sInit.compareAndSet(false, true)) {
            TTAdSdk.init(context, buildConfig(context, appId));
        }
    }

    private static TTAdConfig buildConfig(Context context, String appId) {
        return new TTAdConfig.Builder()
                .appId(appId)
                .useTextureView(true)
                .appName(context.getApplicationInfo().loadLabel(context.getPackageManager()).toString())
                .allowShowPageWhenScreenLock(false)
                .supportMultiProcess(false)
                .build();
    }

    public static void init(Context context, String appId, String appName) {
        if (context == null) {
            return;
        }
        if (sInit.compareAndSet(false, true)) {
            TTAdSdk.init(context, buildConfig(context, appId));
        }
    }
}
