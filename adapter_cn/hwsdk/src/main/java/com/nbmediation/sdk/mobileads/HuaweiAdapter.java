package com.nbmediation.sdk.mobileads;


import android.app.Activity;
import android.text.TextUtils;
import com.huawei.hms.ads.HwAds;
import com.huawei.hms.ads.reward.RewardAd;
import com.huawei.hms.ads.reward.RewardAdLoadListener;
import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.reward.RewardAdStatusListener;
import com.huawei.hms.ads.reward.Reward;
import com.nbmediation.sdk.mediation.MediationInfo;
import com.nbmediation.sdk.mobileads.hwsdk.BuildConfig;
import com.nbmediation.sdk.mediation.CustomAdsAdapter;
import com.nbmediation.sdk.mediation.RewardedVideoCallback;
import java.util.Map;



/**
 * Created by jiantao.tu on 2020/5/14.
 */
public class HuaweiAdapter extends CustomAdsAdapter {

    private RewardAd rewardAd;

    public HuaweiAdapter() {

    }

    @Override
    public String getMediationVersion() {
        return "13.4.30.301";
    }

    @Override
    public String getAdapterVersion() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public int getAdNetworkId() {
        return MediationInfo.MEDIATION_ID_24;
    }

//-- RewardedVideo ads
    @Override
    public void initRewardedVideo(Activity activity, Map<String, Object> dataMap, RewardedVideoCallback callback) {
        super.initRewardedVideo(activity, dataMap, callback);
        String error = check(activity);
        if (!TextUtils.isEmpty(error)) {
            callback.onRewardedVideoInitFailed(error);
            return;
        }
        HwAds.init(activity);

    }
    @Override
    public void loadRewardedVideo(Activity activity, String adUnitId, final RewardedVideoCallback callback) {
        super.loadRewardedVideo(activity, adUnitId, callback);
        String error = check(activity, adUnitId);
        if (!TextUtils.isEmpty(error)) {
            if (callback != null) {
                callback.onRewardedVideoLoadFailed(error);
            }
            return;
        }

        if (rewardAd == null) {
            rewardAd = new RewardAd(activity, adUnitId);
        }
        RewardAdLoadListener listener= new RewardAdLoadListener() {
            @Override
            public void onRewardedLoaded() {
                // 激励广告加载成功
                if (callback != null) {
                    callback.onRewardedVideoLoadSuccess();
                }
            }
            @Override
            public void onRewardAdFailedToLoad(int errorCode) {
                // 激励广告加载失败
                callback.onRewardedVideoLoadFailed("Huawei Rewarded ad load error code: " +errorCode);
            }
        };

        rewardAd.loadAd(new AdParam.Builder().build(), listener);

    }
    @Override
    public void showRewardedVideo(Activity activity, String adUnitId, final RewardedVideoCallback callback) {
        super.showRewardedVideo(activity, adUnitId, callback);
        if (rewardAd.isLoaded()) {
            rewardAd.show(activity, new RewardAdStatusListener() {
                @Override
                public void onRewardAdOpened() {
                    // 激励广告被打开
                    callback.onRewardedVideoAdStarted();
                }
                @Override
                public void onRewardAdFailedToShow(int errorCode) {
                    // 激励广告展示失败
                    callback.onRewardedVideoAdShowFailed("Huawei Rewarded ad show error code: " +errorCode);
                }
                @Override
                public void onRewardAdClosed() {
                    // 激励广告被关闭
                    callback.onRewardedVideoAdClosed();
                }
                @Override
                public void onRewarded(Reward reward){
                    // 激励广告奖励达成
                    // TODO 发放奖励
                    callback.onRewardedVideoAdRewarded();
                }
            });
        }
    }

    @Override
    public boolean isRewardedVideoAvailable(String adUnitId) {
        return rewardAd.isLoaded();
    }

}
