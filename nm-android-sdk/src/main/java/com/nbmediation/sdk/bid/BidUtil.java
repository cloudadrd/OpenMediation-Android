// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.bid;

import com.nbmediation.sdk.utils.model.BaseInstance;

import java.util.HashMap;
import java.util.Map;

final class BidUtil {

    static Map<String, Object> makeBidRequestInfo(BaseInstance instance, int adType) {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(BidConstance.BID_APP_KEY, instance.getAppKey());
        configMap.put(BidConstance.BID_PLACEMENT_ID, instance.getKey());
        configMap.put(BidConstance.BID_AD_TYPE, adType);
        return configMap;
    }
}
