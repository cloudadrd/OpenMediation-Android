// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.nbmediation.sdk.mediation.CustomAdsAdapter;
import com.nbmediation.sdk.mediation.MediationInfo;
import com.nbmediation.sdk.mediation.RewardedVideoCallback;
import com.nbmediation.sdk.mobileads.plugin3.App;
import com.nbmediation.sdk.mobileads.plugin3.BuildConfig;
import com.nbmediation.sdk.mobileads.plugin3.EmptyActivity;
import com.nbmediation.sdk.utils.AdLog;
import com.nbmediation.sdk.utils.HandlerUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import cn.vlion.ad.core.ADManager;
import cn.vlion.ad.core.Config;
import cn.vlion.ad.moudle.video.VideoManager;
import show.vion.cn.vlion_ad_inter.video.VideoViewListener;

public class Plugin3Adapter extends CustomAdsAdapter {

    private static String TAG = "OM-Alion: ";
    private ConcurrentMap<String, String> mTTRvAds;

    public Plugin3Adapter() {
        mTTRvAds = new ConcurrentHashMap<>();
    }

    @Override
    public String getMediationVersion() {
        return "9.1";
    }

    @Override
    public String getAdapterVersion() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.MEDIATION_ID_34;
    }

    @Override
    public void initRewardedVideo(Context activity, Map<String, Object> dataMap, RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        String error = check(activity);
        if (TextUtils.isEmpty(error)) {
            String[] split = mAppKey.split("#");
            String appId = null;
            if (split.length == 1) {
                appId = split[0];
            }

            boolean mVertical = true;
            if (split.length > 1) {
                mVertical = "0".equals(split[1]);
            }
            initSdk(activity, appId, "", mVertical);
            if (callback != null) {
                callback.onRewardedVideoInitSuccess();
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
//        loadRvAd(activity, adUnitId, callback);
        EmptyActivity.loadRewardedVideo(this, adUnitId, callback);
    }

    @Override
    public void loadRewardedVideo(Context activity, String adUnitId, Map<String, Object> extras,
                                  RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, extras, callback);
//        loadRvAd(activity, adUnitId, callback);
        EmptyActivity.loadRewardedVideo(this, adUnitId, callback);
    }

    protected String check(Context activity, String adUnitId) {
        if (activity == null) {
            return "activity is null";
        }
        if (TextUtils.isEmpty(mAppKey)) {
            return "app key is null";
        }
        if (TextUtils.isEmpty(adUnitId)) {
            return "instanceKey is null";
        }
        return "";
    }

    public void loadRvAd(Context activity, String adUnitId, RewardedVideoCallback callback) {
        String error = check(activity, adUnitId);
        if (TextUtils.isEmpty(error)) {
            String rewardedVideoAd = mTTRvAds.get(adUnitId);
            if (rewardedVideoAd == null) {
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
        String rewardedVideoAd = mTTRvAds.get(adUnitId);
        if (rewardedVideoAd != null) {
            VideoManager.getInstance().showVideo();
            mTTRvAds.remove(adUnitId);
        } else {
            if (callback != null) {
                callback.onRewardedVideoAdShowFailed(TAG + "RewardedVideo is not ready");
            }
        }
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return false;
        }
        return mTTRvAds.get(adUnitId) != null && VideoManager.getInstance().isReady();
    }


    private void initSdk(final Context activity, final String appId, String tid, final boolean vertical) {

        HandlerUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MDIDHandler.init(activity);
                if (vertical) {
                    VideoManager.getInstance().setVideoOrientation(Config.AD_VERTIVAL_SCREEN_DISPLAY);
                } else {
                    VideoManager.getInstance().setVideoOrientation(Config.AD_HORIZONTAL_SCREEN_DISPLAY);
                }
                ADManager.getInstance().init(App.getInstance(), appId).setException(true).setOaid(MDIDHandler.getMdid());//对接加载聚合广告需要申请appid
                // (加载聚合广告和新闻需要appid)
//                .setTtId(tid); //可选。向瑞狮运营申请tid
            }
        });

//        VideoManager.getInstance().setAdScalingModel(Config.AD_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);

    }


    private void realLoadRvAd(final Context activity, final String adUnitId, final RewardedVideoCallback rvCallback) {
        VideoManager.getInstance().setAdScalingModel(Config.AD_SCALING_MODE_SCALE_TO_FIT);
        ADManager.getInstance().setOaid(MDIDHandler.getMdid());
        //视频的尺寸

        VideoManager.getInstance().setImageAcceptedSize(1080, 1920);
        //参数，activity对象，广告位id,视频的回调
        HandlerUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                VideoManager.getInstance().getVLionVideoView((Activity) activity, adUnitId, new InnerLoadRvAdListener(rvCallback, adUnitId,
                        mTTRvAds));
            }
        });
    }

    private static class InnerLoadRvAdListener implements VideoViewListener {

        private RewardedVideoCallback mCallback;
        private String mCodeId;
        private ConcurrentMap<String, String> mTTRvAds;

        private InnerLoadRvAdListener(RewardedVideoCallback callback, String codeId, ConcurrentMap<String, String> tTRvAds) {
            this.mCallback = callback;
            this.mCodeId = codeId;
            this.mTTRvAds = tTRvAds;
        }

        @Override
        public void onLoadVideo(String adId) {
            if (adId == null) {
                if (mCallback != null) {
                    mCallback.onRewardedVideoLoadFailed(TAG + "RewardedVideo load failed");
                }
                return;
            }
            mTTRvAds.put(mCodeId, adId);
            if (mCallback != null) {
                mCallback.onRewardedVideoLoadSuccess();
            }
            AdLog.getSingleton().LogD(TAG + "rewardedVideo  onLoadVideo");
        }

        @Override
        public void onVideoPlayStart(String adId) {
            AdLog.getSingleton().LogD(TAG + "rewardVideoAd show");
            if (mCallback != null) {
                mCallback.onRewardedVideoAdShowSuccess();
                mCallback.onRewardedVideoAdStarted();
            }
        }

        @Override
        public void onVideoPlayFailed(String adId, int code, String msg) {
            AdLog.getSingleton().LogD(TAG + "rewardVideoAd onVideoPlayFailed");
            if (mCallback != null) {
                mCallback.onRewardedVideoAdShowFailed(TAG + "rewardedVideo play failed");
            }
        }

        @Override
        public void onVideoClosed(String adId) {
            AdLog.getSingleton().LogD(TAG + "rewardVideoAd onVideoClosed");
            if (mCallback != null) {
                mCallback.onRewardedVideoAdClosed();
            }
        }

        @Override
        public void onVideoClicked(String adId) {
            AdLog.getSingleton().LogD(TAG + "rewardVideoAd bar click");
            if (mCallback != null) {
                mCallback.onRewardedVideoAdClicked();
            }
        }

        @Override
        public void onVideoFinish(String adId) {
            if (mCallback != null) {
                mCallback.onRewardedVideoAdEnded();
            }
        }

        @Override
        public void onRewardVerify(String s) {
            if (mCallback != null) {
                mCallback.onRewardedVideoAdRewarded();
            }

        }

        @Override
        public void onRequestFailed(String adId, int code, String errorMsg) {
            AdLog.getSingleton().LogD(TAG + "RewardedVideo  onError: adId=" + adId + ", code=" + code + ",errorMsg=" + errorMsg);
            if (mCallback != null) {
                mCallback.onRewardedVideoLoadFailed(TAG + " RewardedVideo load failed : " + code + ", " + errorMsg);
            }
        }
    }

}
