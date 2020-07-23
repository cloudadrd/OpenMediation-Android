package com.nbmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.adsgreat.base.callback.VideoAdLoadListener;
import com.adsgreat.base.config.Const;
import com.adsgreat.base.core.AGError;
import com.adsgreat.base.core.AGNative;
import com.adsgreat.base.core.AGVideo;
import com.adsgreat.base.core.AdsgreatSDK;
import com.adsgreat.video.core.RewardedVideoAdListener;
import com.adsgreat.base.callback.EmptyAdEventListener;
import com.adsgreat.video.core.ZcoupVideo;
import com.nbmediation.sdk.mediation.CustomAdsAdapter;
import com.nbmediation.sdk.mediation.InterstitialAdCallback;
import com.nbmediation.sdk.mediation.MediationInfo;
import com.nbmediation.sdk.mediation.RewardedVideoCallback;
import com.nbmediation.sdk.mobileads.plugin1.BuildConfig;
import com.nbmediation.sdk.utils.AdLog;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Created by jiantao.tu on 2020/5/14.
 */
public class Plugin1Adapter extends CustomAdsAdapter {


    private static String TAG = "OM-Cloudmobi-Plugin1: ";
    private ConcurrentMap<String, AGVideo> mRvAds;
    private AtomicBoolean isPreload = new AtomicBoolean();
    public Plugin1Adapter() {
        mRvAds = new ConcurrentHashMap<>();
    }
    private InterstitialAdCallback  loadCallback = null;
    private AGNative agnv = null;
    private boolean isDestroyed = false;
    @Override
    public String getMediationVersion() {
        return Const.getVersionNumber();
    }

    @Override
    public String getAdapterVersion() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.MEDIATION_ID_32;
    }

    @Override
    public void initRewardedVideo(Activity activity, Map<String, Object> dataMap, RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        Object appKey = dataMap.get("AppKey");
        String error = check(activity);
        if (TextUtils.isEmpty(error)) {
            if (appKey instanceof String) {
                AdsgreatSDK.initialize(activity, (String) appKey);
                if (callback != null) {
                    callback.onRewardedVideoInitSuccess();
                }
                return;
            }
        }
        if (callback != null) {
            callback.onRewardedVideoInitFailed(error);
        }

    }

    @Override
    public void loadRewardedVideo(Activity activity, String adUnitId, RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, callback);
        loadRvAd(activity, adUnitId, callback);

    }

    @Override
    public void loadRewardedVideo(Activity activity, String adUnitId, Map<String, Object> extras, RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, extras, callback);
        loadRvAd(activity, adUnitId, callback);
    }

    @Override
    public void showRewardedVideo(Activity activity, String adUnitId, RewardedVideoCallback callback) {
        super.showRewardedVideo(activity, adUnitId, callback);
        String error = check(activity, adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(error);
            }
            return;
        }
        AGVideo agVideo = mRvAds.get(adUnitId);
        if (agVideo != null) {
            ZcoupVideo.showRewardedVideo(agVideo, new VideoAdListenerImpl(callback));
            isPreload.set(false);
            mRvAds.remove(adUnitId);
        } else {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(TAG + "RewardedVideo is not ready");
            }
        }
    }


    public static class VideoAdListenerImpl extends RewardedVideoAdListener {

        RewardedVideoCallback mCallback;

        VideoAdListenerImpl(RewardedVideoCallback callback) {
            mCallback = callback;
        }

        @Override
        public void videoStart() {
            AdLog.getSingleton().LogD(TAG + "videoStart: ");
            if (mCallback != null) {
                mCallback.onRewardedVideoAdStarted();
                mCallback.onRewardedVideoAdShowSuccess();
            }
        }

        @Override
        public void videoFinish() {
            AdLog.getSingleton().LogD(TAG + "videoFinish: ");
            if (mCallback != null) {
                mCallback.onRewardedVideoAdEnded();
            }
        }

        @Override
        public void videoError(Exception e) {
            AdLog.getSingleton().LogD(TAG + "onAdFailed: " + e.getMessage());
            if (mCallback != null) {
                mCallback.onRewardedVideoLoadFailed(TAG + " RewardedVideo load failed : " + e.getMessage());
            }
        }

        @Override
        public void videoClosed() {
            AdLog.getSingleton().LogD(TAG + "videoClosed: ");
            if (mCallback != null) {
                mCallback.onRewardedVideoAdClosed();
            }
        }

        @Override
        public void videoClicked() {
            AdLog.getSingleton().LogD(TAG + "videoClicked: ");
            if (mCallback != null) {
                mCallback.onRewardedVideoAdClicked();
            }
        }

        @Override
        public void videoRewarded(String rewardName, String rewardAmount) {
            AdLog.getSingleton().LogD(TAG + "videoRewarded: rewardName=" + rewardName + ",rewardAmount=" + rewardAmount);
            if (mCallback != null) {
                mCallback.onRewardedVideoAdRewarded();
            }

        }
    }

    private void loadRvAd(Activity activity, String adUnitId, RewardedVideoCallback callback) {
        String error = check(activity, adUnitId);
        if (TextUtils.isEmpty(error)) {
            if (!isPreload.compareAndSet(false, true)) {
                callback.onRewardedVideoLoadFailed(TAG + "ad loading, no need to load repeatedly");
                return;
            }
            AGVideo zcVideo = mRvAds.get(adUnitId);
            if (zcVideo == null) {
                realLoadRvAd(activity, adUnitId, callback);
            } else {
                if (callback != null) {
                    callback.onRewardedVideoLoadSuccess();
                }
            }
        } else {
            if (callback != null) {
                callback.onRewardedVideoLoadFailed(error);
            }
        }
    }

    private void realLoadRvAd(Context activity, final String adUnitId, RewardedVideoCallback callback) {
        VideoAdLoadListener videoAdLoadListener = create(adUnitId, callback);
        ZcoupVideo.preloadRewardedVideo(activity, adUnitId, videoAdLoadListener);
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }

        AGVideo video = mRvAds.get(adUnitId);
        if (video == null) return false;

        return ZcoupVideo.isRewardedVideoAvailable(video);
    }

    private VideoAdLoadListener create(final String adUnitId, final RewardedVideoCallback callback) {

        return new VideoAdLoadListener() {

            @Override
            public void onVideoAdLoadSucceed(AGVideo zcVideo) {
                AdLog.getSingleton().LogD(TAG + "onVideoAdLoadSucceed: ");
                mRvAds.put(adUnitId, zcVideo);
                if (callback != null) {
                    callback.onRewardedVideoLoadSuccess();
                }
            }

            @Override
            public void onVideoAdLoadFailed(AGError zcError) {
                isPreload.set(false);
                String message = "";
                if (zcError != null) {
                    message = zcError.getMsg();
                }
                AdLog.getSingleton().LogD(TAG + "onAdFailed: " + message);
                if (callback != null) {
                    callback.onRewardedVideoLoadFailed(TAG + "RewardedVideo load failed : " + message);
                }
            }
        };
    }

    /*********************************Interstitial***********************************/
    @Override
    public void initInterstitialAd(Activity activity, Map<String, Object> dataMap, InterstitialAdCallback callback) {
        super.initInterstitialAd(activity, dataMap, callback);
        Object appKey = dataMap.get("AppKey");
        String error = check(activity);
        if (TextUtils.isEmpty(error)) {
            if (appKey instanceof String) {
                AdsgreatSDK.initialize(activity, (String) appKey);
                if (callback != null) {
                    callback.onInterstitialAdInitSuccess();
                }
                return;
            }
        }
        if (callback != null) {
            callback.onInterstitialAdInitFailed(error);
        }
    }

    @Override
    public void loadInterstitialAd(Activity activity, String adUnitId, InterstitialAdCallback callback) {
        super.loadInterstitialAd(activity, adUnitId, callback);
        loadCallback = callback;
        AdsgreatSDK.preloadInterstitialAd(activity,adUnitId,new InterstitialAdListener(this));
    }

    @Override
    public void showInterstitialAd(final Activity activity, final String adUnitId, final InterstitialAdCallback callback) {
        super.showInterstitialAd(activity, adUnitId, callback);
         if (AdsgreatSDK.isInterstitialAvailable(agnv)) {
             AdsgreatSDK.showInterstitialAd(agnv);
             callback.onInterstitialAdShowSuccess();
         }else {
             callback.onInterstitialAdShowFailed("ad not ready.");
         }
    }

    @Override
    public boolean isInterstitialAdAvailable(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        return AdsgreatSDK.isInterstitialAvailable(agnv);
    }
//
//    @Override
//    public void destroy(Activity activity) {
//        isDestroyed = true;
//    }

    public static class InterstitialAdListener extends EmptyAdEventListener {
        private InterstitialAdCallback  loadCallbackInListener = null;
        private WeakReference<Plugin1Adapter> mReference;
        InterstitialAdListener(Plugin1Adapter interstitial) {
            mReference = new WeakReference<>(interstitial);
            loadCallbackInListener = interstitial.loadCallback;
        }

        @Override
        public void onReceiveAdSucceed(AGNative agNative) {
            super.onReceiveAdSucceed(agNative);
            loadCallbackInListener.onInterstitialAdLoadSuccess();
            if (agNative != null && agNative.isLoaded()) {
                if (null != loadCallbackInListener) {
                    if (mReference == null || mReference.get() == null) {
                        return;
                    }
                    mReference.get().agnv = agNative;
                }

            }else {
                if (null != loadCallbackInListener) {
                    loadCallbackInListener.onInterstitialAdLoadFailed("ad load failed.");
                }
            }

        }

        @Override
        public void onLandPageShown(AGNative var1) {
            super.onLandPageShown(var1);
        }

        @Override
        public void onAdClicked(AGNative var1) {
            loadCallbackInListener.onInterstitialAdClick();
        }

        @Override
        public void onReceiveAdFailed(AGNative var1) {
            super.onReceiveAdFailed(var1);
            loadCallbackInListener.onInterstitialAdLoadFailed(var1.getErrorsMsg());
        }


        @Override
        public void onAdClosed(AGNative var1) {
            super.onAdClosed(var1);
            loadCallbackInListener.onInterstitialAdClosed();
        }
    }
}


