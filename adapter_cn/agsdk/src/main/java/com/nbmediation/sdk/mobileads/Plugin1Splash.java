package com.nbmediation.sdk.mobileads;
import android.app.Activity;

import android.util.Log;
import android.view.ViewGroup;

import com.adsgreat.base.callback.AdEventListener;
import com.adsgreat.base.core.AGNative;
import com.adsgreat.base.core.AdsgreatSDK;
import com.adsgreat.base.vo.AdsVO;
import com.nbmediation.sdk.mediation.CustomSplashEvent;
import com.nbmediation.sdk.mediation.MediationInfo;
import java.util.Map;

public class Plugin1Splash extends CustomSplashEvent {
    private final static String TAG = "AGSplashAd";
    private static String slotID;
    private static boolean isSplashReaday;

    public void loadAd(Activity activity, Map<String, String> config) {
        if (!check(activity, config)) {
            return;
        }
        slotID= mPlacementId;
        splashPreload(activity,config);
    }

    @Override
    public int getMediation() {
        return MediationInfo.MEDIATION_ID_32;
    }

    @Override
    public void destroy(Activity activity) {
        isDestroyed = true;
    }

    public void splashPreload(Activity activity, Map<String, String> config){
        if (isDestroyed) {
            return;
        }

        AdsgreatSDK.initialize(activity,slotID);
        AdsgreatSDK.preloadSplashAd(activity, slotID, new SplashEventListener() {

            @Override
            public void onReceiveAdSucceed(AGNative result) {
                Log.d(TAG, "Splash Ad Loaded.");
                isSplashReaday = true;
                onInsReady(null);
            }

            @Override
            public void onReceiveAdFailed(AGNative result) {
                if (result != null && result.getErrorsMsg() != null)
                    Log.e(TAG, "onReceiveAdFailed errorMsg=" + result.getErrorsMsg());
                onInsError(result.getErrorsMsg());
            }
        });
    }


    @Override
    public void show(ViewGroup viewGroup) {
        AdsgreatSDK.showSplashAd(slotID, new SplashEventListener() {

            @Override
            public void onReceiveAdFailed(AGNative result) {
                Log.e(TAG, "onReceiveAdFailed errorMsg=" + result.getErrorsMsg());
            }

            @Override
            public void onShowSucceed(AGNative result) {
                Log.d(TAG, "onShowSucceed");
                onInsShowSuccess();
            }

            @Override
            public void onLandPageShown(AGNative result) {
                Log.d(TAG, "onLandPageShown");
            }

            @Override
            public void onAdClicked(AGNative result) {
                Log.d(TAG, "onAdClicked");
                onInsClicked();
            }

            @Override
            public void onAdClosed(AGNative result) {
                Log.d(TAG, "onAdClosed");
                onInsDismissed();
            }
        });
    }

    @Override
    public boolean isReady() {
        return  !isDestroyed && isSplashReaday;
    }

    static class SplashEventListener extends AdEventListener {

        @Override
        public void onReceiveAdSucceed(AGNative result) {
            showMsg("onReceiveAdSucceed");
        }

        @Override
        public void onReceiveAdVoSucceed(AdsVO result) {
            showMsg("onReceiveAdVoSucceed");
        }

        @Override
        public void onShowSucceed(AGNative result) {
            showMsg("onShowSucceed");
        }

        @Override
        public void onReceiveAdFailed(AGNative result) {
            showMsg(result.getErrorsMsg());
            Log.i("sdksample", "==error==" + result.getErrorsMsg());
        }

        @Override
        public void onLandPageShown(AGNative result) {
            showMsg("onLandPageShown");
        }

        @Override
        public void onAdClicked(AGNative result) {
            showMsg("onAdClicked");
        }

        @Override
        public void onAdClosed(AGNative result) {
            showMsg("onAdClosed");
        }

        private void showMsg(String msg) {
        }
    }
}


