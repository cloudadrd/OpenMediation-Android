//package com.nbmediation.sdk.mobileads;
//
//import android.app.Activity;
//import android.content.Context;
//import android.util.Log;
//import android.view.View;
//import android.view.ViewGroup;
//
//import com.baidu.mobad.feeds.ArticleInfo;
//import com.baidu.mobad.feeds.BaiduNativeManager;
//import com.baidu.mobad.feeds.NativeErrorCode;
//import com.baidu.mobad.feeds.NativeResponse;
//import com.baidu.mobad.feeds.RequestParameters;
//import com.kwad.sdk.api.KsAdSDK;
//import com.kwad.sdk.api.KsAdVideoPlayConfig;
//import com.kwad.sdk.api.KsFeedAd;
//import com.kwad.sdk.api.KsLoadManager;
//import com.kwad.sdk.api.KsScene;
//import com.kwad.sdk.api.SdkConfig;
//import com.nbmediation.sdk.mediation.CustomNativeEvent;
//import com.nbmediation.sdk.mediation.MediationInfo;
//import com.nbmediation.sdk.nativead.AdInfo;
//import com.nbmediation.sdk.nativead.NativeAdView;
//import com.nbmediation.sdk.utils.AdLog;
//
//import java.util.List;
//import java.util.Map;
//
//public class BaiduNative extends CustomNativeEvent {
//
//    private static String TAG = "OM-KSNative: ";
//    private KsFeedAd ksNAd;
//    private View mNativeView;
//    private Activity av;
//
//    private BaiduNativeManager mBaiduNativeManager;
//
//    @Override
//    public void loadAd(final Activity activity, Map<String, String> config) {
//        super.loadAd(activity, config);
//        AdLog.getSingleton().LogD(TAG, "getLoadManager check");
//        if (!check(activity, config)) {
//            AdLog.getSingleton().LogD(TAG,"check(activity, config) error.");
//            onInsError("check(activity, config) error.");
//            return;
//        }
//        if (activity == null || activity.isFinishing()) {
//            AdLog.getSingleton().LogD(TAG,"activity is null.");
//            onInsError("activity is null");
//            return;
//        }
//
//        if(mBaiduNativeManager==null){
//            /*
//             * Step 1. 创建BaiduNativeManager对象，参数分别为： 上下文context，广告位ID
//             * 注意：请将adPlaceId替换为自己的广告位ID
//             * 注意：信息流广告对象，与广告位id一一对应，同一个对象可以多次发起请求
//             */
//            mBaiduNativeManager = new BaiduNativeManager(activity.getApplicationContext(), mInstancesKey);
//        }
//        destroyAd();
//        requestAd(Long.parseLong(mInstancesKey));
//    }
//
//    /**
//     * 请求Feed默认模板广告数据
//     */
//    private void requestAd(long posId) {
//        // 若与百度进行相关合作，可使用如下接口上报广告的上下文
//        RequestParameters requestParameters = new RequestParameters.Builder()
//                .downloadAppConfirmPolicy(RequestParameters.DOWNLOAD_APP_CONFIRM_ONLY_MOBILE)
////                // 用户维度：用户性别，取值：0-unknown，1-male，2-female
////                .addExtra(ArticleInfo.USER_SEX, "1")
////                // 用户维度：收藏的小说ID，最多五个ID，且不同ID用'/分隔'
////                .addExtra(ArticleInfo.FAVORITE_BOOK, "这是小说的名称1/这是小说的名称2/这是小说的名称3")
////                // 内容维度：小说、文章的名称
////                .addExtra(ArticleInfo.PAGE_TITLE, "测试书名")
////                // 内容维度：小说、文章的ID
////                .addExtra(ArticleInfo.PAGE_ID, "10930484090")
////                // 内容维度：小说分类，一级分类和二级分类用'/'分隔
////                .addExtra(ArticleInfo.CONTENT_CATEGORY, "一级分类/二级分类")
////                // 内容维度：小说、文章的标签，最多10个，且不同标签用'/分隔'
////                .addExtra(ArticleInfo.CONTENT_LABEL, "标签1/标签2/标签3")
//                .build();
//        mBaiduNativeManager.loadContentAd(requestParameters, new BaiduNativeManager.FeedAdListener() {
//            @Override
//            public void onNativeLoad(List<NativeResponse> nativeResponses) {
//                Log.i(TAG, "onNativeLoad:" +
//                        (nativeResponses != null ? nativeResponses.size() : null));
//                // 一个广告只允许展现一次，多次展现、点击只会计入一次
//                if (nativeResponses != null && nativeResponses.size() > 0) {
//                    nativeResponses.get(0).
//                }
//            }
//
//            @Override
//            public void onNativeFail(NativeErrorCode errorCode) {
//                Log.w(TAG, "onNativeFail reason:" + errorCode.name());
//
//            }
//
//            @Override
//            public void onVideoDownloadSuccess() {
//                Log.i(TAG, "onVideoDownloadSuccess.");
//            }
//
//            @Override
//            public void onVideoDownloadFailed() {
//                Log.i(TAG, "onVideoDownloadFailed.");
//            }
//
//            @Override
//            public void onLpClosed() {
//                Log.i(TAG, "onLpClosed");
//            }
//        });
//
//
//
//    }
//
//
//    private View getAdView() {
//        if (null == ksNAd) {
//            return null;
//        }
//        // 设置监听
//        ksNAd.setVideoSoundEnable(false);//视频播放是否，默认静音播放
//        ksNAd.setAdInteractionListener(new KsFeedAd.AdInteractionListener() {
//            @Override
//            public void onAdClicked() {
//                if (isDestroyed) {
//                    return;
//                }
//                AdLog.getSingleton().LogD(TAG, "onAdClicked");
//                onInsClicked();
//            }
//
//            @Override
//            public void onAdShow() {
//                AdLog.getSingleton().LogD(TAG, "onAdShow: ");
//            }
//
//            @Override
//            public void onDislikeClicked() {
//                if (isDestroyed) {
//                    return;
//                }
//                AdLog.getSingleton().LogD(TAG, "onDislikeClicked");
//                destroyAd();
//            }
//        });
//        if(av != null)
//            return ksNAd.getFeedView(av.getBaseContext());
//        else
//            return null;
//    }
//
//
//    @Override
//    public void registerNativeView(NativeAdView nativeAdView) {
//        AdLog.getSingleton().LogD(TAG, "registerNativeView");
//        mNativeView = getAdView();
//        if (null == mNativeView) return;
//        if (nativeAdView.getMediaView() != null) {
//            nativeAdView.getMediaView().removeAllViews();
//            nativeAdView.getMediaView().addView(mNativeView);
//        }
//
//    }
//
//    @Override
//    public int getMediation() {
//        return MediationInfo.MEDIATION_ID_22;
//    }
//
//    @Override
//    public void destroy(Activity activity) {
//        destroyAd();
//        if (av != null) {
//            av = null;
//        }
//    }
//
//    private void destroyAd() {
//        //用户选择不喜欢原因后，移除广告展示
//        AdLog.getSingleton().LogD(TAG, "destroyAd.");
//        if (mNativeView != null && mNativeView.getParent() instanceof ViewGroup) {
//            ((ViewGroup) mNativeView.getParent()).removeView(mNativeView);
//            mNativeView = null;
//        }
//        if (ksNAd != null) {
//            ksNAd.setAdInteractionListener(null);
//            ksNAd.setVideoPlayConfig(null);
//            ksNAd = null;
//        }
//    }
//
//
//}
