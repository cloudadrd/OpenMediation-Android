// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.mobileads;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.inmobi.ads.InMobiAdRequestStatus;
import com.inmobi.ads.InMobiInterstitial;
import com.inmobi.ads.listeners.InterstitialAdEventListener;
import com.inmobi.sdk.InMobiSdk;
import com.nbmediation.sdk.mediation.CustomAdsAdapter;
import com.nbmediation.sdk.mediation.MediationInfo;
import com.nbmediation.sdk.mediation.RewardedVideoCallback;
import com.nbmediation.sdk.mobileads.inmobi.BuildConfig;
import com.nbmediation.sdk.utils.AdLog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMobiAdapter extends CustomAdsAdapter {

    private final static String TAG = "inmobi: ";

    private ConcurrentMap<String, InMobiInterstitial> mTTRvAds;

    private static InMobiInterstitial interstitialAd;

    private static InnerLoadRvAdListener rvAdListener;

    public InMobiAdapter() {
        mTTRvAds = new ConcurrentHashMap<>();
    }

    @Override
    public String getMediationVersion() {
        return InMobiSdk.getVersion();
    }

    @Override
    public String getAdapterVersion() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.MEDIATION_ID_54;
    }

    @Override
    public void initRewardedVideo(Context activity, Map<String, Object> dataMap, RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        final String error = check(activity);
        if (TextUtils.isEmpty(error)) {
            initSdk(activity);
            if (callback != null) {
                callback.onRewardedVideoInitSuccess();
            }
        } else {
            if (callback != null) {
                callback.onRewardedVideoInitFailed(error);
            }
        }
        Log.e(TAG, "initRewardedVideo ,error=" + error + ",mAppKey=" + mAppKey + ",mRate=" + mRate);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    if (interstitialAd != null) {
                        Log.e(TAG, "interstitialAd=" + interstitialAd.hashCode() + ",mAppKey=" + mAppKey + ",mRate=" + mRate);
                    }else{
                        Log.e(TAG, "interstitialAd=null");
                    }
                    if (rvAdListener != null) {
                        Log.e(TAG, "rvAdListener=" + rvAdListener.hashCode() + ",mAppKey=" + mAppKey + ",mRate=" + mRate);
                    }else{
                        Log.e(TAG, "rvAdListener=null");
                    }
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();
    }

    @Override
    public void loadRewardedVideo(Context activity, String adUnitId, RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, callback);
        loadRvAd(activity, adUnitId, callback);
    }

    @Override
    public void loadRewardedVideo(Context activity, String adUnitId, Map<String, Object> extras,
                                  RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, extras, callback);
        loadRvAd(activity, adUnitId, callback);
    }

    private void loadRvAd(Context activity, String adUnitId, RewardedVideoCallback callback) {
        String error = check(activity, adUnitId);
        if (TextUtils.isEmpty(error)) {
            InMobiInterstitial rewardedVideoAd = mTTRvAds.get(adUnitId);
            if (rewardedVideoAd == null) {
                realLoadRvAd(activity, adUnitId, callback);
            } else {
                if (callback != null) {
                    callback.onRewardedVideoLoadSuccess();
                }
            }
            Log.e(TAG, "loadRewardedVideo ok,adUnitId=" + adUnitId + ",mAppKey=" + mAppKey);
        } else {
            if (callback != null) {
                callback.onRewardedVideoLoadFailed(error);
            }
            Log.e(TAG, "loadRewardedVideo error,adUnitId=" + adUnitId + ",mAppKey=" + mAppKey);
        }


    }

    @Override
    public void showRewardedVideo(Context activity, String adUnitId, RewardedVideoCallback callback) {
        super.showRewardedVideo(activity, adUnitId, callback);

        String error = check(activity, adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(error);
            }
            return;
        }
        InMobiInterstitial rewardedVideoAd = mTTRvAds.get(adUnitId);
        if (rewardedVideoAd != null) {
            rewardedVideoAd.show();
            mTTRvAds.remove(adUnitId);
        } else {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed("TikTok RewardedVideo is not ready");
            }
        }
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        InMobiInterstitial video = mTTRvAds.get(adUnitId);
        return video != null && video.isReady();
    }


    private void initSdk(final Context activity) {
        InAdManagerHolder.init(activity.getApplicationContext(), mAppKey);
    }


    private void realLoadRvAd(Context activity, final String adUnitId, final RewardedVideoCallback rvCallback) {
        rvAdListener = new InnerLoadRvAdListener(rvCallback, adUnitId, mTTRvAds);
        interstitialAd = new
                InMobiInterstitial(activity, Long.parseLong(adUnitId), rvAdListener);
        interstitialAd.load();
    }


    private static class InnerLoadRvAdListener extends InterstitialAdEventListener {

        private RewardedVideoCallback mCallback;
        private String mCodeId;
        private ConcurrentMap<String, InMobiInterstitial> mTTRvAds;

        private InnerLoadRvAdListener(RewardedVideoCallback callback, String codeId, ConcurrentMap<String, InMobiInterstitial> tTRvAds) {
            this.mCallback = callback;
            this.mCodeId = codeId;
            this.mTTRvAds = tTRvAds;
        }

        @Override
        public void onAdLoadFailed(InMobiInterstitial ad, InMobiAdRequestStatus status) {
            String code = "";
            String message = "";
            if (status != null) {
                code = status.getStatusCode().name();
                message = status.getMessage();
            }
            AdLog.getSingleton().LogE(TAG + "RewardedVideo  onAdLoadFailed: " + code + ", " + message);
            if (mCallback != null) {
                mCallback.onRewardedVideoLoadFailed(TAG + "RewardedVideo load failed : " + code + ", " + message);
            }
        }


        @Override
        public void onAdLoadSucceeded(InMobiInterstitial ad) {
            if (ad == null) {
                if (mCallback != null) {
                    mCallback.onRewardedVideoLoadFailed(TAG + "RewardedVideo load failed");
                }
                return;
            }
            mTTRvAds.put(mCodeId, ad);
            if (mCallback != null) {
                mCallback.onRewardedVideoLoadSuccess();
            }
            AdLog.getSingleton().LogD(TAG + "rewardedVideo onRewardVideoAdLoad");
        }

        @Override
        public void onAdClicked(InMobiInterstitial inMobiInterstitial, Map<Object, Object> map) {
            AdLog.getSingleton().LogD(TAG + "rewardedVideo onAdClicked");
            if (mCallback != null) {
                mCallback.onRewardedVideoAdClicked();
            }
        }

        @Override
        public void onAdWillDisplay(InMobiInterstitial inMobiInterstitial) {
            super.onAdWillDisplay(inMobiInterstitial);
            if (mCallback != null) {
                mCallback.onRewardedVideoAdShowSuccess();
                mCallback.onRewardedVideoAdStarted();
            }
            AdLog.getSingleton().LogD(TAG + "rewardVideoAd onAdWillDisplay");
        }

        @Override
        public void onAdDisplayed(InMobiInterstitial inMobiInterstitial) {
            AdLog.getSingleton().LogD(TAG + "rewardedVideo  onAdDisplayed");
        }

        @Override
        public void onAdDisplayFailed(InMobiInterstitial inMobiInterstitial) {
            AdLog.getSingleton().LogD(TAG + "rewardedVideo onAdDisplayFailed");
            if (mCallback != null) {
                mCallback.onRewardedVideoAdShowFailed(TAG + "rewardedVideo play failed");
            }
        }

        @Override
        public void onAdDismissed(InMobiInterstitial inMobiInterstitial) {
            AdLog.getSingleton().LogD(TAG + "rewardVideoAd close onAdDismissed");
            if (mCallback != null) {
                mCallback.onRewardedVideoAdClosed();
            }
        }

        @Override
        public void onRewardsUnlocked(InMobiInterstitial inMobiInterstitial, Map<Object, Object> map) {
            AdLog.getSingleton().LogD(TAG + "rewardVideoAd complete onRewardsUnlocked");
            if (mCallback != null) {
                mCallback.onRewardedVideoAdRewarded();
                mCallback.onRewardedVideoAdEnded();
            }
        }

        @Override
        public void onAdReceived(InMobiInterstitial inMobiInterstitial) {
            super.onAdReceived(inMobiInterstitial);
            AdLog.getSingleton().LogD(TAG + "rewardVideoAd onAdReceived");
        }


        @Override
        public void onUserLeftApplication(InMobiInterstitial inMobiInterstitial) {
            super.onUserLeftApplication(inMobiInterstitial);
            AdLog.getSingleton().LogD(TAG + "rewardVideoAd onUserLeftApplication");
        }

        @Override
        public void onRequestPayloadCreated(byte[] bytes) {
            super.onRequestPayloadCreated(bytes);
            AdLog.getSingleton().LogD(TAG + "rewardVideoAd onRequestPayloadCreated");
        }

        @Override
        public void onRequestPayloadCreationFailed(InMobiAdRequestStatus inMobiAdRequestStatus) {
            super.onRequestPayloadCreationFailed(inMobiAdRequestStatus);
            AdLog.getSingleton().LogD(TAG + "rewardVideoAd onRequestPayloadCreationFailed");
        }
    }


}
