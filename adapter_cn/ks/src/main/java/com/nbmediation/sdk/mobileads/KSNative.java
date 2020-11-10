package com.nbmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.kwad.sdk.api.KsAdSDK;
import com.kwad.sdk.api.KsAdVideoPlayConfig;
import com.kwad.sdk.api.KsFeedAd;
import com.kwad.sdk.api.KsLoadManager;
import com.kwad.sdk.api.KsScene;
import com.kwad.sdk.api.SdkConfig;
import com.nbmediation.sdk.mediation.CustomNativeEvent;
import com.nbmediation.sdk.mediation.MediationInfo;
import com.nbmediation.sdk.nativead.AdInfo;
import com.nbmediation.sdk.nativead.NativeAdView;
import java.util.List;
import java.util.Map;

public class KSNative extends CustomNativeEvent {
    private static String TAG = "OM-KSNative: ";
    private KsFeedAd  ksNAd;
    private View mNativeView;
    private Activity  av;
    @Override
    public void loadAd(final Activity activity, Map<String, String> config) {
        super.loadAd(activity, config);
        String e = "loadAd传入的参数错误.";
        Log.d(TAG, "getLoadManager check");
        if (!check(activity, config)) {
            return;
        }
        if (activity == null || activity.isFinishing()) {
            onInsError("activity is null");
            return;
        }

        String appkeyStr = config.get("AppKey");
        String appID = null;
        String appName = null;
        boolean isDebug = false;
        String[] split = appkeyStr.split("\\|");
        if (split.length > 0) {
            appID = split[0];
        }else {
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
        av = activity;
        initSdk(activity, appID, appName, isDebug);
        requestAd(Long.parseLong(mInstancesKey));
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
                        Log.d(TAG, "error:"+msg);
                    }
                    @Override
                    public void onFeedAdLoad(List<KsFeedAd> adList) {
                        Log.d(TAG, "onFeedAdLoad");
                        int k = 0;
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
                onInsClicked();
            }

            @Override
            public void onAdShow() {
                Log.d(TAG, "onAdShow: ");
            }
            @Override
            public void onDislikeClicked() {
                //用户选择不喜欢原因后，移除广告展示
                if (mNativeView != null && mNativeView.getParent() instanceof ViewGroup) {
                    ((ViewGroup) mNativeView.getParent()).removeView(mNativeView);
                    mNativeView = null;
                }
            }
        });
        return ksNAd.getFeedView(av.getBaseContext());
    }


    @Override
    public void registerNativeView(NativeAdView nativeAdView) {
        mNativeView = getAdView();
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
         if (ksNAd != null) {
             ksNAd = null;
         }

    }


}
