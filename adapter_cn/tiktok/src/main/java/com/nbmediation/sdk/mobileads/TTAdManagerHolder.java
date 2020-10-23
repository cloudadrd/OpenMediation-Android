// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.mobileads;

import android.content.Context;

import com.bytedance.sdk.openadsdk.TTAdConfig;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdSdk;

public class TTAdManagerHolder {

    private static boolean isInit = false;

    public synchronized static TTAdManager get() {
        if (!isInit) {
            throw new RuntimeException("TTAdSdk is not init, please check.");
        }
        return TTAdSdk.getAdManager();
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

    public synchronized static void init(Context context, String appId, String appName) {
        if (context == null) {
            return;
        }
        if (!isInit) {
            TTAdSdk.init(context, buildConfig(context, appId));
            isInit = true;
        }
    }
}
