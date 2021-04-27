// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.mediation;

import com.nbmediation.sdk.nativead.AdInfo;
import com.nbmediation.sdk.nativead.NativeAdView;

public abstract class CustomNativeEvent extends CustomAdEvent {
    protected AdInfo mAdInfo = new AdInfo();

    public abstract void registerNativeView(NativeAdView adView);
}
