package com.nbmediation.sdk.mobileads;

import android.app.Activity;
import android.view.View;

import com.adsgreat.base.core.AdsgreatSDK;
import com.nbmediation.sdk.mediation.CustomBannerEvent;
import com.nbmediation.sdk.mediation.MediationInfo;
import com.nbmediation.sdk.utils.AdLog;
import com.adsgreat.base.callback.EmptyAdEventListener;
import com.adsgreat.base.enums.AdSize;
import com.adsgreat.base.core.AGNative;

import java.lang.ref.WeakReference;
import java.util.Map;

public class Plugin1Banner extends CustomBannerEvent {

    private static String TAG = "OM-AG-Banner:";

    @Override
    public void loadAd(Activity activity, Map<String, String> config) {
        super.loadAd(activity, config);
        if (activity == null || activity.isFinishing()) {
            return;
        }
        if (!check(activity, config)) {
            return;
        }

        String appKey = config.get("AppKey");
        String instanceKey = config.get("InstanceKey");
        if (null == appKey || !(appKey instanceof String)) {
            return;
        }
        if (null == instanceKey || !(instanceKey instanceof String)) {
            return;
        }

        AdsgreatSDK.initialize(activity, (String) appKey);
        loadBannerAd(activity, instanceKey, config);
    }


    private void loadBannerAd(Activity activity, String codeId, Map<String, String> config) {
        int[] size = getBannerSize(config);
        AdSize mAdSize = null;
        for (AdSize adSize : AdSize.values()) {
            if (size[0] == adSize.getWidth() && adSize.getHeight() == adSize.getHeight()) {
                mAdSize = adSize;
                break;
            }
        }
        if (mAdSize != null) {
            AdsgreatSDK.getBannerAd(activity, codeId, mAdSize, new BannerListener(this));
        } else {
            throw new RuntimeException("AGSDK error,不支持的广告大小，width=" + size[0]
                    + ",height=" + size[1]
                    + ",具体原因请参考com.adsgreat.base.enums.AdSize类");
        }

    }

    @Override
    public int getMediation() {
        return MediationInfo.MEDIATION_ID_32;
    }

    @Override
    public void destroy(Activity activity) {
        isDestroyed = true;
    }

    public static class BannerListener extends EmptyAdEventListener {

        private WeakReference<Plugin1Banner> mReference;

        BannerListener(Plugin1Banner banner) {
            mReference = new WeakReference<>(banner);
        }


        @Override
        public void onReceiveAdSucceed(AGNative agNative) {
            if (mReference == null || mReference.get() == null) {
                return;
            }
            Plugin1Banner banner = mReference.get();
            if (banner.isDestroyed) {
                return;
            }
            AdLog.getSingleton().LogD(TAG + "onReceiveAdSucceed");
            banner.onInsReady(agNative);
        }

        @Override
        public void onReceiveAdFailed(AGNative agNative) {
            Plugin1Banner banner = mReference.get();
            if (banner.isDestroyed) {
                return;
            }
            String message = "";
            if (agNative != null) {
                message = agNative.getErrorsMsg();
            }
            AdLog.getSingleton().LogD(TAG + "onReceiveAdFailed " + message);
            banner.onInsError(message);
        }

        @Override
        public void onAdClicked(AGNative agNative) {
            if (mReference == null || mReference.get() == null) {
                return;
            }
            Plugin1Banner banner = mReference.get();
            if (banner.isDestroyed) {
                return;
            }
            banner.onInsClicked();
            AdLog.getSingleton().LogD(TAG + "onAdClicked");
        }

        @Override
        public void onAdClosed(AGNative agNative) {
            AdLog.getSingleton().LogD(TAG + "onAdClosed");
        }
    }
}
