// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.mobileads;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.nbmediation.sdk.mediation.CustomAdsAdapter;
import com.nbmediation.sdk.mediation.InterstitialAdCallback;
import com.nbmediation.sdk.mediation.MediationInfo;
import com.nbmediation.sdk.mobileads.smaato.BuildConfig;
import com.nbmediation.sdk.utils.AdLog;
import com.smaato.sdk.core.SmaatoSdk;
import com.smaato.sdk.interstitial.EventListener;
import com.smaato.sdk.interstitial.Interstitial;
import com.smaato.sdk.interstitial.InterstitialAd;
import com.smaato.sdk.interstitial.InterstitialError;
import com.smaato.sdk.interstitial.InterstitialRequestError;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SmaatoAdapter extends CustomAdsAdapter {

    //    private ConcurrentMap<String, RewardedAd> mRewardedAds;
    private ConcurrentMap<String, InterstitialAd> mInterstitialAds;

    private static String TAG = "OM-Smaato-adapter: ";


    public SmaatoAdapter() {
        mInterstitialAds = new ConcurrentHashMap<>();
    }

    @Override
    public String getMediationVersion() {
        return SmaatoSdk.getVersion();
    }

    @Override
    public String getAdapterVersion() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.MEDIATION_ID_56;
    }


//    /*********************************RewardedVideoAd***********************************/
//    @Override
//    public void initRewardedVideo(Context activity, Map<String, Object> dataMap, RewardedVideoCallback callback) {
//        super.initRewardedVideo(activity, dataMap, callback);
//        try {
//            if (Looper.myLooper() != Looper.getMainLooper()) {
//                if (callback != null) {
//                    callback.onRewardedVideoInitFailed("Must be called on the main UI thread. ");
//                }
//                return;
//            }
//            String error = check(activity);
//            if (TextUtils.isEmpty(error)) {
//
//            } else {
//                if (callback != null) {
//                    callback.onRewardedVideoInitFailed(error);
//                }
//            }
//        } catch (Exception e) {
//            if (callback != null) {
//                callback.onRewardedVideoInitFailed("Init Failed: Unknown Error");
//            }
//        }
//    }
//
//    @Override
//    public void loadRewardedVideo(Context activity, String adUnitId, RewardedVideoCallback callback) {
//        try {
//            if (Looper.myLooper() != Looper.getMainLooper()) {
//                if (callback != null) {
//                    callback.onRewardedVideoLoadFailed("Must be called on the main UI thread. ");
//                }
//                return;
//            }
//
//            String error = check(activity, adUnitId);
//            if (!TextUtils.isEmpty(error)) {
//                if (callback != null) {
//                    callback.onRewardedVideoLoadFailed(error);
//                }
//                return;
//            }
//
//        } catch (Exception e) {
//            if (callback != null) {
//                callback.onRewardedVideoLoadFailed("RewardedVideoAd Load Failed: Unknown Error");
//            }
//        }
//    }
//
//    @Override
//    public void showRewardedVideo(final Context activity, final String adUnitId
//            , final RewardedVideoCallback callback) {
//        new Handler(Looper.getMainLooper()).post(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    String error = check(activity, adUnitId);
//                    if (!TextUtils.isEmpty(error)) {
//                        if (callback != null) {
//                            callback.onRewardedVideoAdShowFailed(error);
//                        }
//                        return;
//                    }
//
//                } catch (Exception e) {
//                    if (callback != null) {
//                        callback.onRewardedVideoAdShowFailed("RewardedVideoAd Show Failed: Unknown Error");
//                    }
//                }
//            }
//        });
//    }
//
//    @Override
//    public boolean isRewardedVideoAvailable(String adUnitId) {
//        if (TextUtils.isEmpty(adUnitId)) {
//            return false;
//        }
//        return false;
//    }


    /*********************************Interstitial***********************************/
    @Override
    public void initInterstitialAd(Context activity, Map<String, Object> dataMap, InterstitialAdCallback callback) {
        super.initInterstitialAd(activity, dataMap, callback);
        String appKey = (String) dataMap.get("AppKey");
        try {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                if (callback != null) {
                    callback.onInterstitialAdInitFailed("Must be called on the main UI thread. appKey=" + appKey);
                }
                return;
            }
            String error = check(activity);
            if (TextUtils.isEmpty(error)) {
                SmaatoHelper.init((Application) activity.getApplicationContext(), appKey);
                if (callback != null) {
                    callback.onInterstitialAdInitSuccess();
                }
                AdLog.getSingleton().LogD(TAG + "onInterstitialAdInitSuccess appKey=" + appKey);
            } else {
                if (callback != null) {
                    callback.onInterstitialAdInitFailed(error);
                }
            }
        } catch (Exception e) {
            if (callback != null) {
                callback.onInterstitialAdInitFailed("Init Failed: Unknown Error appKey=" + appKey);
            }
        }
    }

    @Override
    public void loadInterstitialAd(Context activity, String adUnitId, InterstitialAdCallback callback) {
        super.loadInterstitialAd(activity, adUnitId, callback);
        try {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                if (callback != null) {
                    callback.onInterstitialAdLoadFailed("Must be called on the main UI thread. ");
                }
                return;
            }

            String error = check(activity, adUnitId);
            if (!TextUtils.isEmpty(error)) {
                if (callback != null) {
                    callback.onInterstitialAdLoadFailed(error);
                }
                return;
            }
            Interstitial.loadAd(adUnitId, createInterstitialListener(adUnitId, callback));
        } catch (Exception e) {
            if (callback != null) {
                callback.onInterstitialAdLoadFailed("InterstitialAd Load Failed: Unknown Error");
            }
        }
    }

    @Override
    public void showInterstitialAd(final Context activity, final String adUnitId, final InterstitialAdCallback callback) {
        super.showInterstitialAd(activity, adUnitId, callback);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    String error = check(activity, adUnitId);
                    if (!TextUtils.isEmpty(error)) {
                        if (callback != null) {
                            callback.onInterstitialAdShowFailed(error);
                        }
                        return;
                    }
                    if (!isInterstitialAdAvailable(adUnitId)) {
                        if (callback != null) {
                            callback.onInterstitialAdShowFailed("ad not ready");
                        }
                        return;
                    }
                    InterstitialAd ad = mInterstitialAds.get(adUnitId);
                    if (ad != null) {
                        ad.setBackgroundColor(0xff123456);
                        ad.showAd((Activity) activity);
                    } else {
                        if (callback != null) {
                            callback.onInterstitialAdShowFailed("ad not ready");
                        }
                    }
                } catch (Exception e) {
                    if (callback != null) {
                        callback.onInterstitialAdShowFailed("InterstitialAd Show Failed: Unknown Error");
                    }
                }
            }
        });
    }

    @Override
    public boolean isInterstitialAdAvailable(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        return mInterstitialAds.containsKey(adUnitId);
    }

    private EventListener createInterstitialListener(final String adUnitId, final InterstitialAdCallback callback) {
        return new EventListener() {
            @Override
            //show interstitial ad when it loaded successfully
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                mInterstitialAds.put(adUnitId, interstitialAd);
                if (callback != null) {
                    callback.onInterstitialAdLoadSuccess();
                }
                AdLog.getSingleton().LogD(TAG + "onAdLoaded adUnitId=" + adUnitId);
            }

            @Override
            // interstitial ad failed to load
            public void onAdFailedToLoad(@NonNull InterstitialRequestError interstitialRequestError) {
                AdLog.getSingleton().LogD(TAG + "adUnitId=" + adUnitId + " onAdFailedToLoad " + interstitialRequestError.getInterstitialError());
                mInterstitialAds.remove(adUnitId);
                if (callback != null) {
                    callback.onInterstitialAdLoadFailed(TAG + "onAdFailedToLoad " + interstitialRequestError.getInterstitialError());
                }
            }

            @Override
            // interstitial ad had an unexpected error
            public void onAdError(@NonNull InterstitialAd interstitialAd, @NonNull InterstitialError interstitialError) {
                mInterstitialAds.remove(adUnitId);
                AdLog.getSingleton().LogD(TAG + "onAdFailedToLoad adUnitId=" + adUnitId + " errorMsg=" + interstitialError);
                if (callback != null) {
                    callback.onInterstitialAdShowFailed(TAG + "onAdFailedToLoad " + interstitialError);
                }
            }

            @Override
            // interstitial ad opened and was shown successfully
            public void onAdOpened(@NonNull InterstitialAd interstitialAd) {
                AdLog.getSingleton().LogD(TAG + "onAdOpened ");
                if (callback != null) {
                    callback.onInterstitialAdShowSuccess();
                }
            }

            @Override
            // interstitial ad was closed by the user
            public void onAdClosed(@NonNull InterstitialAd interstitialAd) {
                AdLog.getSingleton().LogD(TAG + "onAdClosed ");
                mInterstitialAds.remove(adUnitId);
                if (callback != null) {
                    callback.onInterstitialAdClosed();
                }
            }

            @Override
            // interstitial ad was clicked by the user
            public void onAdClicked(@NonNull InterstitialAd interstitialAd) {
                AdLog.getSingleton().LogD(TAG + "onAdClicked ");
                if (callback != null) {
                    callback.onInterstitialAdClick();
                }
            }

            @Override
            // interstitial ad was viewed by the user
            public void onAdImpression(@NonNull InterstitialAd interstitialAd) {
                AdLog.getSingleton().LogD(TAG + "onAdImpression ");

            }

            @Override
            // interstitial ad Time to Live expired
            public void onAdTTLExpired(@NonNull InterstitialAd interstitialAd) {
                AdLog.getSingleton().LogD(TAG + "onAdTTLExpired ");
            }
        };
    }


}
