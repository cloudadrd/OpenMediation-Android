// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.mobileads;

import android.content.Context;
import android.text.TextUtils;

import com.mintegral.msdk.MIntegralSDK;
import com.mintegral.msdk.interstitialvideo.out.InterstitialVideoListener;
import com.mintegral.msdk.interstitialvideo.out.MTGInterstitialVideoHandler;
import com.mintegral.msdk.out.MIntegralSDKFactory;
import com.mintegral.msdk.out.MTGRewardVideoHandler;
import com.mintegral.msdk.out.RewardVideoListener;
import com.nbmediation.sdk.mediation.CustomAdsAdapter;
import com.nbmediation.sdk.mediation.InterstitialAdCallback;
import com.nbmediation.sdk.mediation.MediationInfo;
import com.nbmediation.sdk.mediation.RewardedVideoCallback;
import com.nbmediation.sdk.mobileads.mintegral.BuildConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MintegralAdapter extends CustomAdsAdapter {
    private boolean mDidInitSdk;
    private ConcurrentHashMap<String, MTGInterstitialVideoHandler> mInterstitialAds;
    private ConcurrentHashMap<String, MTGRewardVideoHandler> mRvAds;

    public MintegralAdapter() {
        mInterstitialAds = new ConcurrentHashMap<>();
        mRvAds = new ConcurrentHashMap<>();
    }

    @Override
    public String getMediationVersion() {
        return "";
    }

    @Override
    public String getAdapterVersion() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.MEDIATION_ID_14;
    }

    @Override
    public void initRewardedVideo(Context activity, Map<String, Object> dataMap, RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        String error = check(activity);
        if (TextUtils.isEmpty(error)) {
            initSDK(activity.getApplicationContext());
            if (mDidInitSdk) {
                if (callback != null) {
                    callback.onRewardedVideoInitSuccess();
                }
            }
        } else {
            if (callback != null) {
                callback.onRewardedVideoInitFailed(error);
            }
        }
    }

    @Override
    public void loadRewardedVideo(Context activity, String adUnitId, RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, callback);
        String error = check(activity, adUnitId);
        if (TextUtils.isEmpty(error)) {
            String[] ids = adUnitId.split("\\|");
            if (ids.length < 2) {
                callback.onRewardedVideoLoadFailed("adUnitId format error.");
                return;
            }
            MTGRewardVideoHandler rewardVideoHandler = mRvAds.get(adUnitId);
            if (rewardVideoHandler == null) {
                rewardVideoHandler = new MTGRewardVideoHandler(ids[0], ids[1]);
                mRvAds.put(adUnitId, rewardVideoHandler);
            }
            if (rewardVideoHandler.isReady()) {
                if (callback != null) {
                    callback.onRewardedVideoLoadSuccess();
                }
            } else {
                rewardVideoHandler.setRewardVideoListener(new MtgRvAdListener(callback));
                rewardVideoHandler.load();
            }
        } else {
            if (callback != null) {
                callback.onRewardedVideoLoadFailed(error);
            }
        }
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        MTGRewardVideoHandler rewardVideoHandler = mRvAds.get(adUnitId);
        return rewardVideoHandler != null && rewardVideoHandler.isReady();
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
        MTGRewardVideoHandler rewardVideoHandler = mRvAds.get(adUnitId);
        if (rewardVideoHandler == null || !rewardVideoHandler.isReady()) {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed("MTGRewardedVideo ad not ready");
            }
            return;
        }
        rewardVideoHandler.show("1");
    }

    @Override
    public void initInterstitialAd(Context activity, Map<String, Object> dataMap, InterstitialAdCallback callback) {
        super.initInterstitialAd(activity, dataMap, callback);
        String error = check(activity);
        if (TextUtils.isEmpty(error)) {
            initSDK(activity.getApplicationContext());
            if (mDidInitSdk) {
                if (callback != null) {
                    callback.onInterstitialAdInitSuccess();
                }
            }
        } else {
            if (callback != null) {
                callback.onInterstitialAdInitFailed(error);
            }
        }
    }

    @Override
    public void loadInterstitialAd(Context activity, String adUnitId, InterstitialAdCallback callback) {
        super.loadInterstitialAd(activity, adUnitId, callback);
        String error = check(activity, adUnitId);
        if (TextUtils.isEmpty(error)) {
            String[] ids = adUnitId.split("\\|");
            if (ids.length < 2) {
                callback.onInterstitialAdLoadFailed("adUnitId format error.");
                return;
            }
            MTGInterstitialVideoHandler mtgInterstitialVideoHandler = mInterstitialAds.get(adUnitId);
            if (mtgInterstitialVideoHandler == null) {
                mtgInterstitialVideoHandler = new MTGInterstitialVideoHandler(ids[0], ids[1]);
                mInterstitialAds.put(adUnitId, mtgInterstitialVideoHandler);
            }

            if (mtgInterstitialVideoHandler.isReady()) {
                if (callback != null) {
                    callback.onInterstitialAdLoadSuccess();
                }
            } else {
                mtgInterstitialVideoHandler.setInterstitialVideoListener(new MtgInterstitialAdListener(callback));
                mtgInterstitialVideoHandler.load();
            }
        } else {
            if (callback != null) {
                callback.onInterstitialAdLoadFailed(error);
            }
        }
    }

    @Override
    public boolean isInterstitialAdAvailable(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        MTGInterstitialVideoHandler mtgInterstitialVideoHandler = mInterstitialAds.get(adUnitId);
        return mtgInterstitialVideoHandler != null && mtgInterstitialVideoHandler.isReady();
    }

    @Override
    public void showInterstitialAd(Context activity, String adUnitId, InterstitialAdCallback callback) {
        super.showInterstitialAd(activity, adUnitId, callback);
        String error = check(activity, adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onInterstitialAdShowFailed(error);
            }
            return;
        }
        MTGInterstitialVideoHandler mtgInterstitialVideoHandler = mInterstitialAds.get(adUnitId);
        if (mtgInterstitialVideoHandler == null || !mtgInterstitialVideoHandler.isReady()) {
            if (callback != null) {
                callback.onInterstitialAdShowFailed("Mintegral Interstitial Ad Not Ready");
            }
            return;
        }
        mtgInterstitialVideoHandler.show();
    }

    private void initSDK(Context context) {
        if (!mDidInitSdk) {
            mDidInitSdk = true;
            String[] tmp = mAppKey.split("#");
            if(tmp.length < 2)
                return;
            String appId = tmp[0];
            String key = tmp[1];
            MIntegralSDK sdk = MIntegralSDKFactory.getMIntegralSDK();
            Map<String, String> map = sdk.getMTGConfigurationMap(appId, key);
            sdk.init(map, context);
        }
    }

    private static class MtgInterstitialAdListener implements InterstitialVideoListener {

        private InterstitialAdCallback mCallback;

        MtgInterstitialAdListener(InterstitialAdCallback callback) {
            mCallback = callback;
        }

        @Override
        public void onLoadSuccess(String s, String var2) {

        }

        @Override
        public void onVideoLoadSuccess(String s, String var2) {
            if (mCallback != null) {
                mCallback.onInterstitialAdLoadSuccess();
            }
        }

        @Override
        public void onVideoLoadFail(String s) {
            if (mCallback != null) {
                mCallback.onInterstitialAdLoadFailed(s);
            }
        }

        @Override
        public void onAdShow() {
            if (mCallback != null) {
                mCallback.onInterstitialAdShowSuccess();
            }
        }

        @Override
        public void onAdClose(boolean b) {
            if (mCallback != null) {
                mCallback.onInterstitialAdClosed();
            }
        }

        @Override
        public void onShowFail(String s) {
            if (mCallback != null) {
                mCallback.onInterstitialAdShowFailed(s);
            }
        }

        @Override
        public void onVideoAdClicked(String s, String var2) {
            if (mCallback != null) {
                mCallback.onInterstitialAdClick();
            }
        }

        @Override
        public void onVideoComplete(String s, String var2) {
        }

        @Override
        public void onAdCloseWithIVReward(boolean b, int i) {

        }

        @Override
        public void onEndcardShow(String s, String var2) {
        }
    }

    private static class MtgRvAdListener implements RewardVideoListener {

        private RewardedVideoCallback mRvCallback;

        MtgRvAdListener(RewardedVideoCallback callback) {
            mRvCallback = callback;
        }

        @Override
        public void onVideoLoadSuccess(String s, String var2) {
            if (mRvCallback != null) {
                mRvCallback.onRewardedVideoLoadSuccess();
            }
        }

        @Override
        public void onLoadSuccess(String s, String var2) {

        }

        @Override
        public void onVideoLoadFail(String s) {
            if (mRvCallback != null) {
                mRvCallback.onRewardedVideoLoadFailed(s);
            }
        }

        @Override
        public void onAdShow() {
            if (mRvCallback != null) {
                mRvCallback.onRewardedVideoAdShowSuccess();
                mRvCallback.onRewardedVideoAdStarted();
            }
        }

        @Override
        public void onAdClose(boolean b, String s, float v) {
            if (mRvCallback != null) {
                mRvCallback.onRewardedVideoAdClosed();
            }
        }

        @Override
        public void onShowFail(String s) {
            if (mRvCallback != null) {
                mRvCallback.onRewardedVideoAdShowFailed(s);
            }
        }

        @Override
        public void onVideoAdClicked(String s, String var2) {
            if (mRvCallback != null) {
                mRvCallback.onRewardedVideoAdClicked();
            }
        }

        @Override
        public void onVideoComplete(String s, String var2) {
            if (mRvCallback != null) {
                mRvCallback.onRewardedVideoAdEnded();
                mRvCallback.onRewardedVideoAdRewarded();
            }
        }

        @Override
        public void onEndcardShow(String s, String var2) {

        }
    }
}
