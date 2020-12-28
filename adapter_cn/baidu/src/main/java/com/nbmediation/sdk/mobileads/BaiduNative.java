package com.nbmediation.sdk.mobileads;

import android.app.Activity;
import android.view.View;

import com.nbmediation.sdk.mediation.CustomNativeEvent;
import com.nbmediation.sdk.mediation.MediationInfo;
import com.nbmediation.sdk.nativead.AdInfo;
import com.nbmediation.sdk.nativead.NativeAdView;
import com.nbmediation.sdk.utils.AdLog;

import com.baidu.mobads.rewardvideo.RewardVideoAd;
import com.baidu.mobad.feeds.BaiduNativeManager;
import com.baidu.mobad.feeds.NativeErrorCode;
import com.baidu.mobad.feeds.NativeResponse;
import com.baidu.mobad.feeds.RequestParameters;
import com.baidu.mobad.feeds.XAdNativeResponse;
import com.baidu.mobads.component.FeedNativeView;
import com.baidu.mobads.component.StyleParams;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

public class BaiduNative extends CustomNativeEvent {

//    private static final String FEED_SMART_OPT_AD_PLACE_ID = "7355507";//"6481012"; // 信息流智能优选
    private static String TAG = "OM-BaiduNative: ";
    private BaiduNativeManager mBaiduNativeManager;
//    private View mNativeView;
    private  NativeResponse nativeAd;
    private WeakReference<Activity> mRefAct;

    @Override
    public void loadAd(final Activity activity, Map<String, String> config) {
        super.loadAd(activity, config);
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
        mRefAct = new WeakReference<>(activity);

        String appKeyStr = config.get("AppKey");
        if (null == appKeyStr || appKeyStr.isEmpty()) {
            AdLog.getSingleton().LogD(TAG , "appKeyStr is null!");
            onInsError("appKeyStr is null");
            return;
        }

        RewardVideoAd.setAppSid(appKeyStr);
        AdLog.getSingleton().LogD(TAG ,"setAppsID " + appKeyStr);
        if(mBaiduNativeManager==null){
            AdLog.getSingleton().LogD(TAG ,"insk = "+ mInstancesKey);
            mBaiduNativeManager = new BaiduNativeManager(activity.getApplicationContext(), mInstancesKey);//mInstancesKey
        }
        requestAd(Long.parseLong(mInstancesKey));
    }

    /**
     * 请求Feed默认模板广告数据
     */
    private void requestAd(long posId) {
        // 若与百度进行相关合作，可使用如下接口上报广告的上下文
        RequestParameters requestParameters = new RequestParameters.Builder()
                .downloadAppConfirmPolicy(RequestParameters.DOWNLOAD_APP_CONFIRM_ONLY_MOBILE)
                .build();
        mBaiduNativeManager.loadFeedAd(requestParameters, new BaiduNativeManager.NativeLoadListener() {
            @Override
            public void onNativeLoad(List<NativeResponse> nativeResponses) {
                AdLog.getSingleton().LogD(TAG , "onNativeLoad");

                if (nativeResponses != null && nativeResponses.size() > 0) {
                    nativeAd = nativeResponses.get(0);

                    AdInfo mAdInfo = new AdInfo();
                    mAdInfo.setDesc("");
                    mAdInfo.setType(2);
                    mAdInfo.setAdNetWorkId(MediationInfo.MEDIATION_ID_55);
                    mAdInfo.setCallToActionText("");
                    mAdInfo.setTitle("");
                    mAdInfo.setTemplate(true);
                    onInsReady(mAdInfo);
                }

            }

            @Override
            public void onLoadFail(String message, String errorCode) {
                AdLog.getSingleton().LogD(TAG , "onLoadFail reason:" + message + "errorCode:" + errorCode );
            }

            @Override
            public void onNativeFail(NativeErrorCode errorCode) {
                // 建议使用onLoadFail回调获取详细的请求失败的原因
                AdLog.getSingleton().LogD(TAG , "onNativeFail reason:"+ errorCode.name());
                onInsError("error:" + errorCode.name());
            }

            @Override
            public void onVideoDownloadSuccess() {
                AdLog.getSingleton().LogD(TAG , "onVideoDownloadSuccess");
            }

            @Override
            public void onVideoDownloadFailed() {
                AdLog.getSingleton().LogD(TAG , "onVideoDownloadFailed");
            }

            @Override
            public void onLpClosed() {
                AdLog.getSingleton().LogD(TAG , "onLpClosed");
            }
        });
    }


    @Override
    public void registerNativeView(NativeAdView nativeAdView) {
        AdLog.getSingleton().LogD(TAG, "registerNativeView");

        if ( null == nativeAd) {
            AdLog.getSingleton().LogD(TAG, "registerNativeView error: nativeAd is null.");
            onInsError("registerNativeView error: nativeAd is null.");
            return;
        }

        if (nativeAdView.getMediaView() != null) {
            nativeAdView.getMediaView().removeAllViews();

            if (null != mRefAct.get()){
                FeedNativeView newAdView = new FeedNativeView(mRefAct.get());
                NativeResponse ad = nativeAd;
                newAdView.setAdData((XAdNativeResponse) ad);
                StyleParams params = new StyleParams.Builder()
                        .build();
                newAdView.changeViewLayoutParams(params);
                nativeAdView.getMediaView().addView(newAdView);

                nativeAdView.getMediaView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AdLog.getSingleton().LogD(TAG, "handleClick");
                        nativeAd.handleClick(view);
                    }
                });
            }

            final View adView = nativeAdView.getMediaView();
            nativeAd.registerViewForInteraction(adView, new NativeResponse.AdInteractionListener() {
                @Override
                public void onAdClick() {
                    AdLog.getSingleton().LogD(TAG , "onAdClick");
                    onInsClicked();
                }

                @Override
                public void onADExposed() {
                    AdLog.getSingleton().LogD(TAG , "onADExposed");
                    if (null != nativeAd) {
                        nativeAd.recordImpression(adView);
                    }
                }

                @Override
                public void onADStatusChanged() {
                    AdLog.getSingleton().LogD(TAG , "onADStatusChanged");
                }
            });

            nativeAd.setAdPrivacyListener(new NativeResponse.AdPrivacyListener() {
                @Override
                public void onADPermissionShow() {
                    AdLog.getSingleton().LogD(TAG , "onADPermissionShow");
                }

                @Override
                public void onADPermissionClose() {
                    AdLog.getSingleton().LogD(TAG , "onADPermissionClose");
                }

                @Override
                public void onADPrivacyClose() {
                    AdLog.getSingleton().LogD(TAG , "onADPrivacyClose");
                }
            });
        }
    }


    @Override
    public int getMediation() {
        return MediationInfo.MEDIATION_ID_55;
    }

    @Override
    public void destroy(Activity activity) {
        if (mRefAct != null) {
            mRefAct = null;
        }
        if (null != nativeAd){
            nativeAd = null;
        }
    }


}
