// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.nbmediation.sdk.mediation.AdapterErrorBuilder;
import com.nbmediation.sdk.mediation.CustomBannerEvent;
import com.nbmediation.sdk.mediation.MediationInfo;

import net.pubnative.lite.sdk.HyBid;
import net.pubnative.lite.sdk.views.HyBidAdView;

import java.util.Map;


public class PubNativeBanner extends CustomBannerEvent implements PubNativeBannerListener {

    private HyBidAdView mBannerView;

    @Override
    public void setAgeRestricted(Context context, boolean restricted) {
        super.setAgeRestricted(context, restricted);
        HyBid.setCoppaEnabled(restricted);
    }

    @Override
    public void setUserAge(Context context, int age) {
        super.setUserAge(context, age);
        HyBid.setAge(String.valueOf(age));
    }

    @Override
    public void setUserGender(Context context, String gender) {
        super.setUserGender(context, gender);
        HyBid.setGender(gender);
    }

    @Override
    public void loadAd(final Activity activity, final Map<String, String> config) throws Throwable {
        super.loadAd(activity, config);

        if (!check(activity, config)) {
            return;
        }
        mBannerView = PubNativeSingleTon.getInstance().getBannerAd(mInstancesKey);
        if (mBannerView == null) {
            String error = PubNativeSingleTon.getInstance().getError(mInstancesKey);
            if (TextUtils.isEmpty(error)) {
                error = "No Fill";
            }
            onInsError(AdapterErrorBuilder.buildLoadError(
                    AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, error));
            return;
        }
        PubNativeSingleTon.getInstance().addBannerListener(mInstancesKey, this);
        mBannerView.show();
        onInsReady(mBannerView);
    }

    @Override
    public int getMediation() {
        return MediationInfo.MEDIATION_ID_23;
    }

    @Override
    public void destroy(Activity activity) {
        PubNativeSingleTon.getInstance().removeBannerListener(mInstancesKey);
        if (mBannerView != null) {
            mBannerView.destroy();
            mBannerView = null;
        }
        isDestroyed = true;
    }

    @Override
    public void onAdImpression(String placementId) {

    }

    @Override
    public void onAdClick(String placementId) {
        if (isDestroyed) {
            return;
        }
        onInsClicked();
    }
}
