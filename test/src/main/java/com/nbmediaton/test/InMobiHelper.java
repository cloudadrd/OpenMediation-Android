// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediaton.test;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.inmobi.ads.InMobiAdRequestStatus;
import com.inmobi.ads.InMobiInterstitial;
import com.inmobi.ads.listeners.InterstitialAdEventListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMobiHelper {

    public final static String TAG = "inmobi: ";

    private ConcurrentMap<String, InMobiInterstitial> mTTRvAds;

    private InMobiInterstitial interstitialAd;

    private String appKey;

    private final static InMobiHelper IN_MOBI_HELPER = new InMobiHelper();

    public static InMobiHelper getInstance() {
        return IN_MOBI_HELPER;
    }


    public InMobiHelper() {
        mTTRvAds = new ConcurrentHashMap<>();
    }

    public void initRewardedVideo(Context activity, String appKey) {
        this.appKey = appKey;
        initSdk(activity);
    }

    public void loadRewardedVideo(Context activity, String adUnitId, RewardedVideoCallback callback) {
        loadRvAd(activity, adUnitId, callback);
    }


    private void loadRvAd(Context activity, String adUnitId, RewardedVideoCallback callback) {
        InMobiInterstitial rewardedVideoAd = mTTRvAds.get(adUnitId);
        if (rewardedVideoAd == null) {
            interstitialAd = new
                    InMobiInterstitial(activity, Long.parseLong(adUnitId), new InnerLoadRvAdListener(adUnitId, mTTRvAds, callback));
            interstitialAd.load();
        }
    }

    public void showRewardedVideo(String adUnitId) {
        InMobiInterstitial rewardedVideoAd = mTTRvAds.get(adUnitId);
        if (rewardedVideoAd != null) {
            rewardedVideoAd.show();
            mTTRvAds.remove(adUnitId);
        } else {
            Log.i(TAG, "TikTok RewardedVideo is not ready");
        }
    }

    public boolean isRewardedVideoAvailable(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        InMobiInterstitial video = mTTRvAds.get(adUnitId);
        return video != null && video.isReady();
    }


    private void initSdk(final Context activity) {
        InAdManagerHolder.init(activity.getApplicationContext(), appKey);
    }

    public interface RewardedVideoCallback {
        void onAdLoadFailed();

        void onAdLoadSucceeded();
    }

    public static class InnerLoadRvAdListener extends InterstitialAdEventListener {

        private String mCodeId;


        private ConcurrentMap<String, InMobiInterstitial> mTTRvAds;

        private RewardedVideoCallback mCallback;


        private InnerLoadRvAdListener(String codeId, ConcurrentMap<String, InMobiInterstitial> mTTRvAds, RewardedVideoCallback callback) {
            this.mCodeId = codeId;
            this.mTTRvAds = mTTRvAds;
            this.mCallback = callback;
        }


        @Override
        public void onAdLoadFailed(InMobiInterstitial ad, InMobiAdRequestStatus status) {
            String code = "";
            String message = "";
            if (status != null) {
                code = status.getStatusCode().name();
                message = status.getMessage();
            }
            if (mCallback != null) {
                mCallback.onAdLoadFailed();
            }
            Log.w(TAG, "RewardedVideo  onAdLoadFailed: " + code + ", " + message);
        }


        @Override
        public void onAdLoadSucceeded(InMobiInterstitial ad) {
            if (ad == null) {
                return;
            }
            mTTRvAds.put(mCodeId, ad);
            if (InMobiHelper.getInstance().isRewardedVideoAvailable(mCodeId)) {
                if (mCallback != null) {
                    mCallback.onAdLoadSucceeded();
                }
            } else {
                if (mCallback != null) {
                    mCallback.onAdLoadFailed();
                }
            }
            Log.w(TAG, "rewardedVideo  onRewardVideoAdLoad");
        }

        @Override
        public void onAdClicked(InMobiInterstitial inMobiInterstitial, Map<Object, Object> map) {
            Log.w(InMobiHelper.TAG, "rewardedVideo  onAdClicked");
        }

        @Override
        public void onAdDisplayed(InMobiInterstitial inMobiInterstitial) {
            Log.w(InMobiHelper.TAG, "rewardedVideo  onAdDisplayed");
        }

        @Override
        public void onAdDisplayFailed(InMobiInterstitial inMobiInterstitial) {
            Log.w(InMobiHelper.TAG, "rewardedVideo  onAdDisplayFailed");
        }

        @Override
        public void onAdDismissed(InMobiInterstitial inMobiInterstitial) {
            Log.w(InMobiHelper.TAG, "rewardVideoAd close");
        }

        @Override
        public void onRewardsUnlocked(InMobiInterstitial inMobiInterstitial, Map<Object, Object> map) {
            Log.w(InMobiHelper.TAG, "rewardVideoAd complete onRewardsUnlocked");
        }


        @Override
        public void onAdWillDisplay(@NonNull InMobiInterstitial inMobiInterstitial) {
            super.onAdWillDisplay(inMobiInterstitial);
            Log.w(InMobiHelper.TAG, "rewardVideoAd onAdWillDisplay");
        }

        @Override
        public void onUserLeftApplication(@NonNull InMobiInterstitial inMobiInterstitial) {
            super.onUserLeftApplication(inMobiInterstitial);
            Log.w(InMobiHelper.TAG, "rewardVideoAd onUserLeftApplication");
        }

        @Override
        public void onRequestPayloadCreated(byte[] bytes) {
            super.onRequestPayloadCreated(bytes);
            Log.w(InMobiHelper.TAG, "rewardVideoAd onRequestPayloadCreated");
        }

        @Override
        public void onRequestPayloadCreationFailed(@NonNull InMobiAdRequestStatus inMobiAdRequestStatus) {
            super.onRequestPayloadCreationFailed(inMobiAdRequestStatus);
            Log.w(InMobiHelper.TAG, "rewardVideoAd onRequestPayloadCreationFailed");
        }

        @Override
        public void onAdReceived(InMobiInterstitial inMobiInterstitial) {
            super.onAdReceived(inMobiInterstitial);
            Log.w(InMobiHelper.TAG, "rewardVideoAd onAdReceived");
        }


    }


}
