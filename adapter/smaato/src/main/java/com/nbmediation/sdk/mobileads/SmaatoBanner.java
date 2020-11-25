// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.mobileads;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.nbmediation.sdk.mediation.CustomBannerEvent;
import com.nbmediation.sdk.mediation.MediationInfo;
import com.nbmediation.sdk.utils.AdLog;
import com.smaato.sdk.banner.ad.BannerAdSize;
import com.smaato.sdk.banner.widget.BannerError;
import com.smaato.sdk.banner.widget.BannerView;

import java.util.Map;

public class SmaatoBanner extends CustomBannerEvent {

    private static String TAG = "OM-Smaato-banner: ";

    private BannerView mBannerView;

    private BannerAdSize mAdSize;

    @Override
    public void loadAd(Activity activity, Map<String, String> config) {
        super.loadAd(activity, config);
        if (!check(activity, config)) {
            return;
        }
        if (mBannerView != null) {
            mBannerView.loadAd(mInstancesKey, mAdSize);
        }
        SmaatoHelper.init(activity.getApplication(), config.get("AppKey"));
        mBannerView = new BannerView(activity);
        int[] size = getBannerSize(config);
        if (size[0] == 320 || size[1] == 50) {
            mAdSize = BannerAdSize.XX_LARGE_320x50;
        } else if (size[0] == 300 || size[1] == 250) {
            mAdSize = BannerAdSize.MEDIUM_RECTANGLE_300x250;
        } else if (size[0] == 728 || size[1] == 90) {
            mAdSize = BannerAdSize.LEADERBOARD_728x90;
        } else {
            mAdSize = BannerAdSize.XX_LARGE_320x50;
        }

        mBannerView.setEventListener(new BannerView.EventListener() {
            @Override
            // banner ad successfully loaded
            public void onAdLoaded(@NonNull BannerView bannerView) {
                AdLog.getSingleton().LogD(TAG + "onAdLoaded isDestroyed=" + isDestroyed);

            }

            @Override
            // banner ad failed to load
            public void onAdFailedToLoad(@NonNull BannerView bannerView, @NonNull BannerError bannerError) {
                if (!isDestroyed) {
                    onInsError(TAG + "onAdFailedToLoad banner ad failed to load");
                }
                AdLog.getSingleton().LogD(TAG + "onAdFailedToLoad bannerError=" + bannerError);

            }

            @Override
            // banner ad was seen by the user
            public void onAdImpression(@NonNull BannerView bannerView) {
                AdLog.getSingleton().LogD(TAG + "onAdImpression, banner ad was seen by the user");
            }

            @Override
            // banner ad was clicked by the user
            public void onAdClicked(@NonNull BannerView bannerView) {
                if (!isDestroyed) {
                    onInsClicked();
                }
                AdLog.getSingleton().LogD(TAG + "onAdClicked");
            }

            @Override
            // banner ad Time to Live expired
            public void onAdTTLExpired(@NonNull BannerView bannerView) {
                if (!isDestroyed) {
                    onInsError(TAG + "onAdTTLExpired banner ad Time to Live expire");
                }
                AdLog.getSingleton().LogD(TAG + "onAdTTLExpired banner ad Time to Live expire");

            }
        });
        onInsReady(mBannerView);
        mBannerView.loadAd(mInstancesKey, mAdSize);
    }

    @Override
    public int getMediation() {
        return MediationInfo.MEDIATION_ID_2;
    }

    @Override
    public void destroy(Activity activity) {
        if (mBannerView != null) {
            mBannerView.destroy();
            mBannerView = null;
        }
        mAdSize = null;
        isDestroyed = true;
    }
}
