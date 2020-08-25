// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.mobileads;

import android.content.Context;

import com.inmobi.sdk.InMobiSdk;

import org.json.JSONException;
import org.json.JSONObject;

public class InAdManagerHolder {

    private static boolean sInit;

    public static void init(Context context, String appId) {
        if (context == null) {
            return;
        }
        doInit(context, appId);
    }

    private static void doInit(Context context, String appId) {
        if (!sInit) {
            InMobiSdk.init(context, appId);
            sInit = true;
        }
    }

    private static JSONObject buildConfig() {
        JSONObject consentObject = new JSONObject();
        try {
            // Provide correct consent value to sdk which is obtained by User
            consentObject.put(InMobiSdk.IM_GDPR_CONSENT_AVAILABLE, true);
            // Provide 0 if GDPR is not applicable and 1 if applicable
            consentObject.put("gdpr", "0");
            // Provide user consent in IAB format
//            consentObject.put(InMobiSdk.IM_GDPR_CONSENT_IAB, true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return consentObject;
    }
}
