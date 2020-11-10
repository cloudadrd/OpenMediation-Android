package com.nbmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

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

import static android.widget.ListPopupWindow.MATCH_PARENT;

public class KSNative extends CustomNativeEvent {
    private static String TAG = "OM-KSNative: ";
    private Context mContext;
    private KsFeedAd  ksNAd;
    private View mNativeView;
    private Activity  av;
    @Override
    public void loadAd(final Activity activity, Map<String, String> config) {
        super.loadAd(activity, config);
        Log.d(TAG, "getLoadManager check");
        if (!check(activity, config)) {
            return;
        }

        if (activity == null || activity.isFinishing()) {
            return;
        }
        if (!check(activity, config)) {
            return;
        };
        //暂时获取后面加上保护
        String appkeyStr = config.get("AppKey");
        String[] split = appkeyStr.split("\\|");
//        String appID = split[0];
        String appID ="525400018";
        String appName = split[1];
        boolean isDebug = Boolean.parseBoolean(split[2]);
        //end
        av = activity;
        initSdk(activity, appID, appName, isDebug);
        int[] size = getNativeSize(config);
        requestAd(Long.parseLong(mInstancesKey), size[0], size[1]);
    }

    private void initSdk(final Context activity, String appId, String appName, boolean isDebug) {
        KsAdSDK.init(activity, new SdkConfig.Builder()
                .appId(appId) // 测试aapId，请联系快⼿手平台申请正式AppId，必填
                .appName(appName) // 测试appName，请填写您应⽤用的名称，⾮非必填
                .showNotification(true) // 是否展示下载通知栏 .debug(true)
                .debug(isDebug) // 是否开启sdk 调试⽇日志 可选
                .build());

    }
    /**
     * 请求Feed默认模板广告数据
     */
    private void requestAd(long posId, int width, int height) {
        KsScene scene = new KsScene.Builder(posId)
                .width(width)
                .adNum(1).build(); // 此为测试posId，请联系快手平台申请正式posId
//        if (null== KsAdSDK.getLoadManager()) {
//            Log.d(TAG, "getLoadManager is null");
//        }else {
//            Log.d(TAG, "getLoadManager");
//        }
        KsAdSDK.getLoadManager()
                .loadConfigFeedAd(scene, new KsLoadManager.FeedAdListener() {
                    @Override
                    public void onError(int code, String msg) {
                        Log.d(TAG, "error:"+msg);
                    }
                    @Override
                    public void onFeedAdLoad(List<KsFeedAd> adList) {
                        Log.d(TAG, "onFeedAdLoad");
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


    private View getAdItemView() {
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
        mNativeView = ksNAd.getFeedView(av.getBaseContext());
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

    }


}
