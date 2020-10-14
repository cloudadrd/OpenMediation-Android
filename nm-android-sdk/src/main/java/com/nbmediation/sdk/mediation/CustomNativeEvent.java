// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.mediation;

import com.nbmediation.sdk.nativead.AdInfo;
import com.nbmediation.sdk.nativead.NativeAdView;

import java.util.Map;

public abstract class CustomNativeEvent extends CustomAdEvent {
    protected AdInfo mAdInfo = new AdInfo();


    protected int[] getNativeSize(Map<String, String> config) {
        int width = 640, height = 320;
        if (config != null && config.containsKey("width") && config.containsKey("height")) {
            try {
                int w = Integer.parseInt(config.get("width"));
                int h = Integer.parseInt(config.get("height"));
                if (w != 0 && h != 0){
                    width = w;
                    height = h;
                }
            } catch (Exception e) {
                width = 640;
                height = 320;
            }
        }
        return new int[]{width, height};
    }

    public abstract void registerNativeView(NativeAdView adView);
}
