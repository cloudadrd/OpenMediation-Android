package com.nbmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.nbmediation.sdk.mediation.CustomAdsAdapter;
import com.nbmediation.sdk.mediation.InterstitialAdCallback;
import com.nbmediation.sdk.mediation.MediationInfo;
import com.nbmediation.sdk.mobileads.plugin6.BuildConfig;


import com.baidu.mobads.AdSize;
import com.baidu.mobads.InterstitialAd;
import com.baidu.mobads.InterstitialAdListener;
import com.nbmediation.sdk.utils.AdLog;

import java.util.Map;

public class BaiduAdapter extends CustomAdsAdapter implements InterstitialAdListener{

    private InterstitialAd mInterAd;            // 插屏广告实例，支持单例模式
    private InterstitialAdCallback mCallback;
    private static String TAG = "Baidu-Interstitial: ";

    @Override
    public String getMediationVersion() {
        return null;
    }

    @Override
    public String getAdapterVersion() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.MEDIATION_ID_55;
    }

    @Override
    public void initInterstitialAd(Context activity, Map<String, Object> dataMap, InterstitialAdCallback callback) {
        super.initInterstitialAd(activity, dataMap, callback);
        String error = check(activity);
        if (TextUtils.isEmpty(error)) {
            mCallback = callback;
            if (callback != null) {
                callback.onInterstitialAdInitSuccess();
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
        loadInterstitial(activity, adUnitId, callback);
    }

    @Override
    public void loadInterstitialAd(Context activity, String adUnitId, Map<String, Object> extras,
                                   InterstitialAdCallback callback) {
        super.loadInterstitialAd(activity, adUnitId, extras, callback);
        loadInterstitial(activity, adUnitId, callback);
    }

    private void loadInterstitial(Context activity, String adUnitId, InterstitialAdCallback callback) {
        String error = check(activity, adUnitId);
        if (TextUtils.isEmpty(error)) {
            mInterAd = new InterstitialAd(activity, adUnitId);
            mInterAd.setListener(this);
            mInterAd.loadAd();
            mCallback = callback;
        } else {
            if (callback != null) {
                callback.onInterstitialAdLoadFailed(error);
            }
        }
    }

    public void showInterstitialAd(Context activity, String adUnitId, InterstitialAdCallback callback) {
        super.showInterstitialAd(activity, adUnitId, callback);
        String error = check(activity, adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onInterstitialAdShowFailed(error);
            }
            return;
        }
        mInterAd.showAd((Activity)activity);
        mCallback = callback;
    }

    @Override
    public boolean isInterstitialAdAvailable(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        return mInterAd.isAdReady();
    }

    @Override
    public void onAdClick(InterstitialAd arg0) {
        AdLog.getSingleton().LogD(TAG + "InterstitialAd onAdClicked");
        if (mCallback != null) {
            mCallback.onInterstitialAdClick();
        }
    }


    @Override
    public void onAdDismissed() {
        AdLog.getSingleton().LogD(TAG + "InterstitialAd onAdClose");
        if (mCallback != null) {
            mCallback.onInterstitialAdClosed();
        }
    }

    @Override
    public void onAdFailed(String arg0) {
        AdLog.getSingleton().LogD(TAG + "loadInterstitialAd onError " + arg0);
        if (mCallback != null) {
            mCallback.onInterstitialAdLoadFailed("Baidu loadInterstitialAd ad load failed : " + arg0);
        }
    }

    @Override
    public void onAdPresent() {
        AdLog.getSingleton().LogD(TAG + "InterstitialAd onAdShow");
        if (mCallback != null) {
            mCallback.onInterstitialAdShowSuccess();
        }
    }

    @Override
    public void onAdReady() {
        AdLog.getSingleton().LogD(TAG + "loadInterstitialAd AdLoaded");
        if (mCallback != null) {
            mCallback.onInterstitialAdLoadSuccess();
        }
    }

}
