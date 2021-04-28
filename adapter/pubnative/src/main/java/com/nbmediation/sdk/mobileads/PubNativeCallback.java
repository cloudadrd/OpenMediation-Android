// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.mobileads;

import java.util.Map;

public interface PubNativeCallback {
    void onBidSuccess(String placementId, Map<String, String> map);

    void onBidFailed(String placementId, String error);
}