package com.nbmediation.sdk.mobileads;
import android.app.Activity;

import android.util.Log;

import com.adsgreat.base.callback.AdEventListener;
import com.adsgreat.base.core.AGNative;
import com.adsgreat.base.core.AdsgreatSDK;
import com.adsgreat.base.vo.AdsVO;

public class Plugin1Splash {
    private final static String TAG = "SplashAd";
    private static String slot;
    static void splashPreload(Activity activity, String appID, String appName, String slotID){
        slot = slotID;
        AdsgreatSDK.preloadSplashAd(activity, slotID, new SplashEventListener() {


            @Override
            public void onReceiveAdSucceed(AGNative result) {
//                Log.d(TAG, "Splash Ad Loaded.");

            }

            @Override
            public void onReceiveAdFailed(AGNative result) {
                if (result != null && result.getErrorsMsg() != null)
                    Log.e(TAG, "onReceiveAdFailed errorMsg=" + result.getErrorsMsg());
            }

        });
    }

    static void splashShow() {
        AdsgreatSDK.showSplashAd(slot, new SplashEventListener() {

            @Override
            public void onReceiveAdFailed(AGNative result) {
                Log.e(TAG, "onReceiveAdFailed errorMsg=" + result.getErrorsMsg());
            }

            @Override
            public void onShowSucceed(AGNative result) {
                Log.d(TAG, "onShowSucceed");
            }

            @Override
            public void onLandPageShown(AGNative result) {
                Log.d(TAG, "onLandPageShown");
            }

            @Override
            public void onAdClicked(AGNative result) {
                Log.d(TAG, "onAdClicked");
            }

            @Override
            public void onAdClosed(AGNative result) {
                Log.d(TAG, "onAdClosed");
            }
        });
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


