// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.crosspromotion.sdk;

import android.content.Context;

import com.crosspromotion.sdk.core.NmAdNetworkManager;
import com.crosspromotion.sdk.utils.CpConstants;

public final class CrossPromotionAds {

    public static void init(Context context) {
        NmAdNetworkManager.getInstance().init(context);
    }

    /**
     * Returns the SDk version
     *
     * @return the sdk version
     */
    public static String getSDKVersion() {
        return CpConstants.VERSION;
    }
}
