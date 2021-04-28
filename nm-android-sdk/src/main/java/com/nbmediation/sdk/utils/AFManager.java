package com.nbmediation.sdk.utils;

import android.content.Context;

import com.nbmediation.sdk.NmAds;
import com.nbmediation.sdk.utils.helper.AfHelper;

public class AFManager {

    private static Object sConversionData = null;
    private static Object sDeepLinkData = null;
    private static final int TYPE_CONVERSION_DATA = 0;
    private static final int TYPE_LINK_DATA = 1;

    public static String getAfId(Context context) {
        if (context == null) {
            return null;
        }
        try {
            Class.forName("com.appsflyer.AppsFlyerLib");
        } catch (Exception e) {
            return null;
        }
        return com.appsflyer.AppsFlyerLib.getInstance().getAppsFlyerUID(context);
    }

    public static void sendAFConversionData(Object conversionData) {
        if (!NmAds.isInit()) {
            sConversionData = conversionData;
            return;
        }
        sendData(TYPE_CONVERSION_DATA, conversionData);
    }

    public static void sendAFDeepLinkData(Object deepLinkData) {
        if (!NmAds.isInit()) {
            sDeepLinkData = deepLinkData;
            return;
        }
        sendData(TYPE_LINK_DATA, deepLinkData);
    }

    public static void checkAfDataStatus() {
        if (sConversionData != null) {
            sendData(TYPE_CONVERSION_DATA, sConversionData);
            sConversionData = null;
        }
        if (sDeepLinkData != null) {
            sendData(TYPE_LINK_DATA, sDeepLinkData);
            sDeepLinkData = null;
        }
    }

    private static void sendData(int type, Object conversionData) {
        AfHelper.sendAfRequest(type, conversionData);
    }
}
