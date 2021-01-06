package com.nbmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.kwad.sdk.api.KsAdSDK;
import com.kwad.sdk.api.KsAdVideoPlayConfig;
import com.kwad.sdk.api.KsFeedAd;
import com.kwad.sdk.api.KsLoadManager;
import com.kwad.sdk.api.KsScene;
import com.kwad.sdk.api.SdkConfig;
import com.kwad.sdk.api.KsDrawAd;



import com.nbmediation.sdk.mediation.CustomNativeEvent;
import com.nbmediation.sdk.mediation.MediationInfo;
import com.nbmediation.sdk.nativead.AdInfo;
import com.nbmediation.sdk.nativead.NativeAdView;
import com.nbmediation.sdk.utils.AdLog;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

public class KSNative extends CustomNativeEvent {

    private static String TAG = "OM-KSNative: ";
    private KsFeedAd ksNAd;
    private View mNativeView;
    private WeakReference<Activity> av;
    private KsDrawAd ksDrawAd;
    private Boolean isDrawAd;

    @Override
    public void loadAd(final Activity activity, Map<String, String> config) {
        super.loadAd(activity, config);
        String e = "loadAd传入的参数错误.";
        AdLog.getSingleton().LogD(TAG, "getLoadManager check");
        if (!check(activity, config)) {
            AdLog.getSingleton().LogD(TAG,"check(activity, config) error.");
            onInsError("check(activity, config) error.");
            return;
        }
        if (activity == null || activity.isFinishing()) {
            AdLog.getSingleton().LogD(TAG,"activity is null.");
            onInsError("activity is null");
            return;
        }

        String appKeyStr = config.get("AppKey");
        String appID;
        String appName = null;
        boolean isDebug = false;
        String[] split = new String[0];
        if (appKeyStr != null) {
            split = appKeyStr.split("\\|");
        }
        if (split.length > 0) {
            appID = split[0];
        } else {
            AdLog.getSingleton().LogD(TAG,"not input AppID.");
            onInsError("not input AppID.");
            return;
        }
        if (split.length > 1) {
            appName = split[1];
        }

        if (split.length > 2) {
            try {
                isDebug = Boolean.parseBoolean(split[2]);
            } catch (Exception ignored) {
            }
        }
        av = new WeakReference<>(activity);
        initSdk(activity, appID, appName, isDebug);
        destroyAd();

        String[] mSplit = new String[0];
        mSplit = mInstancesKey.split("\\|");
        if (mSplit.length > 1 && mSplit[0].equalsIgnoreCase("draw")){
            isDrawAd = true;
            requestDrawAd(Long.parseLong(mSplit[1]));
        }else {
            requestAd(Long.parseLong(mInstancesKey));
        }
    }

    private void initSdk(final Context activity, String appId, String appName, boolean isDebug) {
        KsAdSDK.init(activity, new SdkConfig.Builder()
                .appId(appId) // aapId，必填
                .appName(appName) //appName，非必填
                .showNotification(true) // 是否展示下载通知栏 .debug(true)
                .debug(isDebug) // 是否开启sdk 调试⽇日志 可选
                .build());
    }

    /**
     * 请求Feed默认模板广告数据
     */
    private void requestAd(long posId) {
        KsScene scene = new KsScene.Builder(posId)
                .adNum(1).build();
        KsAdSDK.getLoadManager()
                .loadConfigFeedAd(scene, new KsLoadManager.FeedAdListener() {
                    @Override
                    public void onError(int code, String msg) {
                        AdLog.getSingleton().LogD(TAG, "error:" + msg);
                        if (isDestroyed) {
                            return;
                        }
                        onInsError("error:" + msg);
                    }

                    @Override
                    public void onFeedAdLoad(List<KsFeedAd> adList) {
                        AdLog.getSingleton().LogD(TAG, "onFeedAdLoad");
                        if (isDestroyed) {
                            return;
                        }
                        if (adList == null) return;
                        for (KsFeedAd ksFeedAd : adList) {
                            if (ksFeedAd != null) {
                                ksNAd = ksFeedAd;
                                KsAdVideoPlayConfig videoPlayConfig = new KsAdVideoPlayConfig.Builder()
                                        .videoSoundEnable(false) // 是否有声播放
                                        .dataFlowAutoStart(false) // 是否非WiFi下自动播放
                                        .build();
                                ksNAd.setVideoPlayConfig(videoPlayConfig);
                                break;
                            }
                        }
                        AdInfo mAdInfo = new AdInfo();
                        mAdInfo.setDesc("");
                        mAdInfo.setType(2);
                        mAdInfo.setAdNetWorkId(MediationInfo.MEDIATION_ID_22);
                        mAdInfo.setCallToActionText("");
                        mAdInfo.setTitle("");
                        mAdInfo.setTemplate(true);
                        onInsReady(mAdInfo);
                    }
                });
    }


    private View getAdView() {
        if (null == ksNAd) {
            return null;
        }
        // 设置监听
        ksNAd.setVideoSoundEnable(false);//视频播放是否，默认静音播放
        ksNAd.setAdInteractionListener(new KsFeedAd.AdInteractionListener() {
            @Override
            public void onAdClicked() {
                if (isDestroyed) {
                    return;
                }
                AdLog.getSingleton().LogD(TAG, "onAdClicked");
                onInsClicked();
            }

            @Override
            public void onAdShow() {
                AdLog.getSingleton().LogD(TAG, "onAdShow: ");
            }

            @Override
            public void onDislikeClicked() {
                if (isDestroyed) {
                    return;
                }
                AdLog.getSingleton().LogD(TAG, "onDislikeClicked");
                destroyAd();
            }
        });
        if(av != null){
            Activity act = av.get();
            return ksNAd.getFeedView(act.getBaseContext());
        } else {
            return null;
        }
    }

    private View getDrawAdView() {
        if (null == ksDrawAd){
            return null;
        }
        ksDrawAd.setAdInteractionListener(new KsDrawAd.AdInteractionListener() {
            @Override
            public void onAdClicked() {
                onInsClicked();
                AdLog.getSingleton().LogD(TAG, "drawAd onAdClicked: ");
            }

            @Override
            public void onAdShow() {
                AdLog.getSingleton().LogD(TAG, "drawAd onAdShow: ");
            }

            @Override
            public void onVideoPlayStart() {
                AdLog.getSingleton().LogD(TAG, "drawAd onVideoPlayStart: ");
            }

            @Override
            public void onVideoPlayPause() {
                AdLog.getSingleton().LogD(TAG, "drawAd onVideoPlayPause: ");
            }

            @Override
            public void onVideoPlayResume() {
                AdLog.getSingleton().LogD(TAG, "drawAd onVideoPlayResume: ");
            }

            @Override
            public void onVideoPlayEnd() {
                AdLog.getSingleton().LogD(TAG, "drawAd onVideoPlayEnd: ");
            }

            @Override
            public void onVideoPlayError() {
            }
        });

        if(av != null){
            Activity act = av.get();
            return ksDrawAd.getDrawView(act.getBaseContext());
        } else {
            return null;
        }
    }

    @Override
    public void registerNativeView(NativeAdView nativeAdView) {
        AdLog.getSingleton().LogD(TAG, "registerNativeView");
        if (isDrawAd) {
            mNativeView = getDrawAdView();
        }else {
            mNativeView = getAdView();
        }
        if (null == mNativeView) return;
        if (nativeAdView.getMediaView() != null) {
            nativeAdView.getMediaView().removeAllViews();
            nativeAdView.getMediaView().addView(mNativeView);
        }
    }

    @Override
    public int getMediation() {
        return MediationInfo.MEDIATION_ID_22;
    }

    @Override
    public void destroy(Activity activity) {
        destroyAd();
        if (av != null) {
            av = null;
        }
    }

    private void destroyAd() {
        //用户选择不喜欢原因后，移除广告展示
        AdLog.getSingleton().LogD(TAG, "destroyAd.");
        if (mNativeView != null && mNativeView.getParent() instanceof ViewGroup) {
            ((ViewGroup) mNativeView.getParent()).removeView(mNativeView);
            mNativeView = null;
        }
        if (ksNAd != null) {
            ksNAd.setAdInteractionListener(null);
            ksNAd.setVideoPlayConfig(null);
            ksNAd = null;
        }

        if (ksDrawAd != null) {
            ksDrawAd.setAdInteractionListener(null);
            ksDrawAd = null;
        }

        isDrawAd = false;
    }

//Draw Ad
    private void requestDrawAd(long posId) {
        KsScene scene = new KsScene.Builder(posId).adNum(1).build();
        KsAdSDK.getLoadManager().loadDrawAd(scene, new KsLoadManager.DrawAdListener() {
            @Override
            public void onError(int code, String msg) {
                AdLog.getSingleton().LogD(TAG, msg);
                onInsError(msg);
            }

            @Override
            public void onDrawAdLoad(@Nullable List<KsDrawAd> adList) {
                if (adList == null || adList.isEmpty()) {
                    AdLog.getSingleton().LogD(TAG, "DrawAd adList is Empty");
                    onInsError("DrawAd adList is Empty.");
                    return;
                }
                ksDrawAd = adList.get(0);

                AdInfo mAdInfo = new AdInfo();
                mAdInfo.setDesc("");
                mAdInfo.setType(2);
                mAdInfo.setAdNetWorkId(MediationInfo.MEDIATION_ID_22);
                mAdInfo.setCallToActionText("");
                mAdInfo.setTitle("");
                mAdInfo.setTemplate(true);
                onInsReady(mAdInfo);
            }
        });
    }

}
