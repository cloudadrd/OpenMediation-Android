package com.nbmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.ironsource.mediationsdk.model.Placement;
import com.nbmediation.sdk.mobileads.ironsource.BuildConfig;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.utils.IronSourceUtils;
import com.nbmediation.sdk.mediation.CustomAdsAdapter;
import com.nbmediation.sdk.mediation.InterstitialAdCallback;
import com.nbmediation.sdk.mediation.MediationInfo;
import com.nbmediation.sdk.mediation.RewardedVideoCallback;
import com.nbmediation.sdk.utils.AdLog;
import com.ironsource.mediationsdk.sdk.RewardedVideoListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class IronSourceAdapter extends CustomAdsAdapter implements RewardedVideoListener{

    private static final String TAG = "OM-IronSource";
    private static String SharedInstanceID = "SharedInstance";

    private static AtomicBoolean mDidInitInterstitial = new AtomicBoolean(false);

    private final static List<IronSource.AD_UNIT> mIsAdUnitsToInit =
            new ArrayList<>(Collections.singletonList(IronSource.AD_UNIT.INTERSTITIAL));

    private static AtomicBoolean mDidInitRewardedVideo = new AtomicBoolean(false);

    private final static List<IronSource.AD_UNIT> mRvAdUnitsToInit =
            new ArrayList<>(Collections.singletonList(IronSource.AD_UNIT.REWARDED_VIDEO));

    private ConcurrentMap<String, RewardedVideoCallback> mRvCallbacks;
    private ConcurrentMap<String, InterstitialAdCallback> mIsCallbacks;

    @Override
    public void onRewardedVideoAdOpened() {
        Log.d(TAG, String.format("IronSource Rewarded Video opened ad for instance %s",
                SharedInstanceID));

        RewardedVideoCallback callback = mRvCallbacks.get(SharedInstanceID);
        if (callback != null) {
            callback.onRewardedVideoAdShowSuccess();
            callback.onRewardedVideoAdStarted();
        }
    }

    @Override
    public void onRewardedVideoAdClosed() {
        Log.d(TAG, String.format("IronSource Rewarded Video closed ad for instance %s",
                SharedInstanceID));

        RewardedVideoCallback callback = mRvCallbacks.get(SharedInstanceID);
        if (callback != null) {
            callback.onRewardedVideoAdClosed();
        }
    }

    @Override
    public void onRewardedVideoAvailabilityChanged(boolean b) {
//        if(b == false){
//            final String message = String.format("IronSource Rewarded Video is not ready");
//            Log.w(TAG, message);
//
//            RewardedVideoCallback callback = mRvCallbacks.get(SharedInstanceID);
//            if (callback != null) {
//                callback.onRewardedVideoAdShowFailed(message);
//            }
//        }else{
//            Log.d(TAG, String.format("IronSource load success for instanceId: %s", SharedInstanceID));
//            RewardedVideoCallback callback = mRvCallbacks.get(SharedInstanceID);
//            if (callback != null) {
//                callback.onRewardedVideoLoadSuccess();
//            }
//        }
    }

    @Override
    public void onRewardedVideoAdStarted() {

    }

    @Override
    public void onRewardedVideoAdEnded() {

    }

    @Override
    public void onRewardedVideoAdRewarded(Placement placement) {
        Log.d(TAG, String.format("IronSource Rewarded Video received reward for instance %s", SharedInstanceID));

        RewardedVideoCallback callback = mRvCallbacks.get(SharedInstanceID);
        if (callback != null) {
            callback.onRewardedVideoAdRewarded();
        }
    }

    @Override
    public void onRewardedVideoAdShowFailed(IronSourceError ironSourceError) {
        final String message = String.format("IronSource Rewarded Video failed to show for instance %s with Error: %s",
                SharedInstanceID, ironSourceError.getErrorMessage());
        Log.w(TAG, message);

        RewardedVideoCallback callback = mRvCallbacks.get(SharedInstanceID);
        if (callback != null) {
            callback.onRewardedVideoAdShowFailed(message);
        }
    }

    @Override
    public void onRewardedVideoAdClicked(Placement placement) {
        Log.d(TAG, String.format("IronSource Rewarded Video clicked for instance %s",
                placement));

        RewardedVideoCallback callback = mRvCallbacks.get(SharedInstanceID);
        if (callback != null) {
            callback.onRewardedVideoAdClicked();
        }
    }

    enum INSTANCE_STATE {
        START, //Initial state when instance wasn't loaded yet
        CAN_LOAD, //If load is called on an instance with this state, pass it forward to IronSource SDK
        LOCKED, //if load is called on an instance with this state, report load fail
    }

    public IronSourceAdapter() {
        mRvCallbacks = new ConcurrentHashMap<>();
        mIsCallbacks = new ConcurrentHashMap<>();
    }

    @Override
    public String getMediationVersion() {
        return IronSourceUtils.getSDKVersion();
    }

    @Override
    public String getAdapterVersion() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.MEDIATION_ID_15;
    }

    @Override
    public void onResume(Context activity) {
        super.onResume(activity);
        IronSource.onResume((Activity) activity);
    }

    @Override
    public void onPause(Context activity) {
        IronSource.onPause((Activity) activity);
        super.onPause(activity);
    }

    @Override
    public void initRewardedVideo(Context activity, Map<String, Object> dataMap, RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        String error = check(activity);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onRewardedVideoInitFailed(error);
            }
            return;
        }
        if (!mDidInitRewardedVideo.getAndSet(true)) {
            //IronSourceManager.getInstance().initIronSourceSDK((Activity) activity, mAppKey, mRvAdUnitsToInit);
            IronSource.init((Activity) activity, mAppKey);
        }
        if (callback != null) {
            callback.onRewardedVideoInitSuccess();
        }
    }

    @Override
    public void loadRewardedVideo(Context activity, String adUnitId, RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, callback);
        String checkError = check(activity, adUnitId);
        SharedInstanceID = adUnitId;
        if (callback != null) {
            mRvCallbacks.put(SharedInstanceID, callback);
        }

        if (TextUtils.isEmpty(checkError)) {
            if (isRewardedVideoAvailable(adUnitId) && callback != null) {
                callback.onRewardedVideoLoadSuccess();
                IronSource.setRewardedVideoListener(this);
                return;
            }

            if (callback != null) {
                callback.onRewardedVideoLoadFailed(checkError);
            }
            //Placement placement = IronSource.getRewardedVideoPlacementInfo("DefaultRewardedVideo");
            //IronSourceManager.getInstance().loadRewardedVideo(adUnitId, new WeakReference<>(IronSourceAdapter.this));
        } else {
            if (callback != null) {
                callback.onRewardedVideoLoadFailed(checkError);
            }
        }
    }

    @Override
    public void showRewardedVideo(Context activity, String adUnitId, RewardedVideoCallback callback) {
        super.showRewardedVideo(activity, adUnitId, callback);
        String checkError = check(activity, adUnitId);
        SharedInstanceID = adUnitId;
        if (TextUtils.isEmpty(checkError)) {
            if (callback != null) {
                mRvCallbacks.put(SharedInstanceID, callback);
            }
//            IronSourceManager.getInstance().showRewardedVideo(adUnitId);
            IronSource.showRewardedVideo(adUnitId);
        } else {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(checkError);
            }
        }
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        return IronSource.isRewardedVideoAvailable();
        //return IronSourceManager.getInstance().isRewardedVideoReady(adUnitId);
    }

    @Override
    public void initInterstitialAd(Context activity, Map<String, Object> dataMap, InterstitialAdCallback callback) {
        super.initInterstitialAd(activity, dataMap, callback);
        String error = check(activity);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onInterstitialAdInitFailed(error);
            }
            return;
        }
        if (!mDidInitInterstitial.getAndSet(true)) {
            IronSourceManager.getInstance().initIronSourceSDK((Activity) activity, mAppKey, mIsAdUnitsToInit);
        }
        if (callback != null) {
            callback.onInterstitialAdInitSuccess();
        }
    }

    @Override
    public void loadInterstitialAd(Context activity, String adUnitId, InterstitialAdCallback callback) {
        super.loadInterstitialAd(activity, adUnitId, callback);
        String checkError = check(activity, adUnitId);
        if (TextUtils.isEmpty(checkError)) {
            if (isInterstitialAdAvailable(adUnitId) && callback != null) {
                callback.onInterstitialAdLoadSuccess();
                return;
            }
            if (callback != null) {
                mIsCallbacks.put(adUnitId, callback);
            }
            IronSourceManager.getInstance().loadInterstitial(adUnitId, new WeakReference<>(IronSourceAdapter.this));
        } else {
            if (callback != null) {
                callback.onInterstitialAdLoadFailed(checkError);
            }
        }
    }

    @Override
    public void showInterstitialAd(Context activity, String adUnitId, InterstitialAdCallback callback) {
        super.showInterstitialAd(activity, adUnitId, callback);
        String checkError = check(activity, adUnitId);
        if (TextUtils.isEmpty(checkError)) {
            if (callback != null) {
                mIsCallbacks.put(adUnitId, callback);
            }
            IronSourceManager.getInstance().showInterstitial(adUnitId);
        } else {
            if (callback != null) {
                callback.onInterstitialAdShowFailed(checkError);
            }
        }
    }

    @Override
    public boolean isInterstitialAdAvailable(String adUnitId) {
        return IronSourceManager.getInstance().isInterstitialReady(adUnitId);
    }

    //region ISDemandOnlyInterstitialListener implementation.
    void onInterstitialAdReady(String instanceId) {
        AdLog.getSingleton().LogD(TAG, String.format("IronSource Interstitial loaded successfully for instance %s "
                , instanceId));

        InterstitialAdCallback callback = mIsCallbacks.get(instanceId);
        if (callback != null) {
            callback.onInterstitialAdLoadSuccess();
        }
    }

    void onInterstitialAdLoadFailed(String instanceId, IronSourceError ironSourceError) {
        Log.e(TAG, String.format("IronSource Interstitial failed to load for instance %s  with Error: %s"
                , instanceId, ironSourceError.getErrorMessage()));

        InterstitialAdCallback callback = mIsCallbacks.get(instanceId);
        if (callback != null) {
            callback.onInterstitialAdLoadFailed(ironSourceError.toString());
        }
    }

    void onInterstitialAdOpened(String instanceId) {
        Log.d(TAG, String.format("IronSource Interstitial opened ad for instance %s",
                instanceId));

        InterstitialAdCallback callback = mIsCallbacks.get(instanceId);
        if (callback != null) {
            callback.onInterstitialAdShowSuccess();
        }
    }

    void onInterstitialAdClosed(String instanceId) {
        Log.d(TAG, String.format("IronSource Interstitial closed ad for instance %s",
                instanceId));

        InterstitialAdCallback callback = mIsCallbacks.get(instanceId);
        if (callback != null) {
            callback.onInterstitialAdClosed();
        }
    }

    void onInterstitialAdShowFailed(String instanceId, IronSourceError ironSourceError) {
        Log.e(TAG, String.format("IronSource Interstitial failed to show " +
                "for instance %s with Error: %s", instanceId, ironSourceError.getErrorMessage()));
        InterstitialAdCallback callback = mIsCallbacks.get(instanceId);
        if (callback != null) {
            callback.onInterstitialAdShowFailed(ironSourceError.toString());
        }
    }

    void onInterstitialAdClicked(String instanceId) {
        Log.d(TAG, String.format("IronSource Interstitial ad clicked for instance %s",
                instanceId));

        InterstitialAdCallback callback = mIsCallbacks.get(instanceId);
        if (callback != null) {
            callback.onInterstitialAdClick();
        }
    }

    /**
     * IronSource callbacks for AdMob Mediation.
     */

//    void onRewardedVideoAdLoadSuccess(String instanceId) {
//
//    }
//
//    void onRewardedVideoAdLoadFailed(String instanceId, IronSourceError ironSourceError) {
//
//    }
//
//    void onRewardedVideoAdOpened(final String instanceId) {
//
//    }
//
//    void onRewardedVideoAdClosed(String instanceId) {
//
//    }
//
//    void onRewardedVideoAdRewarded(String instanceId) {
//
//    }
//
//    void onRewardedVideoAdShowFailed(final String instanceId, IronSourceError ironsourceError) {
//
//    }
//
//    void onRewardedVideoAdClicked(String instanceId) {
//
//    }
}
