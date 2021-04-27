// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.mobileads;

import android.content.Context;

import com.crosspromotion.sdk.bid.BidderTokenProvider;
import com.nbmediation.sdk.bid.BidAdapter;

public class CrossPromotionBidAdapter extends BidAdapter {

    @Override
    public String getBiddingToken(Context context) {
        return BidderTokenProvider.getBidderToken();
    }
}
