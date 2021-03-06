// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.bid;

import android.util.SparseArray;

import com.nbmediation.sdk.mediation.MediationInfo;
import com.nbmediation.sdk.utils.AdapterUtil;
import com.nbmediation.sdk.utils.DeveloperLog;
import com.nbmediation.sdk.utils.crash.CrashUtil;

final class BidAdapterUtil extends AdapterUtil {
    private static final String BID_ADAPTER = "BidAdapter";

    private static SparseArray<BidAdapter> mBidAdapters = new SparseArray<>();
    private static SparseArray<String> mBidAdapterPaths;

    static {
        mBidAdapterPaths = new SparseArray<>();
        mBidAdapterPaths.put(MediationInfo.MEDIATION_ID_3, getBidAdapterPath(MediationInfo.MEDIATION_ID_3));
    }

    static BidAdapter getBidAdapter(int mediationId) {
        try {
            if (mBidAdapters == null) {
                mBidAdapters = new SparseArray<>();
            }

            if (mBidAdapters.get(mediationId) != null) {
                return mBidAdapters.get(mediationId);
            } else {
                BidAdapter bidAdapter = createAdapter(BidAdapter.class, mBidAdapterPaths.get(mediationId));
                mBidAdapters.put(mediationId, bidAdapter);
                return bidAdapter;
            }
        } catch (Exception e) {
            CrashUtil.getSingleton().saveException(e);
        }
        return null;
    }

    static boolean hasBidAdapter(int mediationId) {
        return mBidAdapters != null && mBidAdapters.get(mediationId) != null;
    }

    private static String getBidAdapterPath(int mediation) {
        String path = "";
        switch (mediation) {
            case MediationInfo.MEDIATION_ID_3:
                path = MEDIATION_ADAPTER_BASE_PATH.concat(getAdapterName(MediationInfo.MEDIATION_NAME_3)).concat(BID_ADAPTER);
                break;
            default:
                break;
        }
        DeveloperLog.LogD("adapter path is : " + path);
        return path;
    }
}
