package com.nbmediation.sdk.mobileads;
import android.app.Activity;

import android.os.CountDownTimer;
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
    private static final String CONFIG_TIMEOUT = "Timeout";
    private final static String TAG = "AGSplashAd";
    private static String slotID;
    private static boolean isSplashReaday;
    private CountDownTimer timer;
    private int fetchDelay;
    private boolean isTimerOut;

    public void loadAd(Activity activity, Map<String, String> config) {
        if (!check(activity, config)) {
            return;
        }
        slotID= mInstancesKey;
        splashPreload(activity,config);
    }

    @Override
    public int getMediation() {
        return MediationInfo.MEDIATION_ID_32;
    }

    @Override
    public void destroy(Activity activity) {
        if (timer != null) {
            timer.cancel();
        }
        isDestroyed = true;
    }

    public void splashPreload(Activity activity, Map<String, String> config){
        if (isDestroyed) {
            return;
        }
        try {
            fetchDelay = Integer.parseInt(config.get(CONFIG_TIMEOUT));
        } catch (Exception e) {
            fetchDelay = 3000;
        }

        AdsgreatSDK.initialize(activity,slotID);
        AdsgreatSDK.preloadSplashAd(activity, slotID, new SplashEventListener() {

            @Override
            public void onReceiveAdSucceed(AGNative result) {
                if (isDestroyed) {
                    return;
                }

                Log.d(TAG, "Splash Ad Loaded.");
                isSplashReaday = true;
                if (!isTimerOut) {
                    onInsReady(null);
                }
            }

            @Override
            public void onReceiveAdFailed(AGNative result) {
                if (isDestroyed) {
                    return;
                }
                if (result != null && result.getErrorsMsg() != null)
                    Log.e(TAG, "onReceiveAdFailed errorMsg=" + result.getErrorsMsg());
                onInsError(result.getErrorsMsg());
            }

            @Override
            public void onAdTimeOver() {
                if (isDestroyed) {
                    return;
                }
                Log.d(TAG, "onAdTimeOver");
                onInsDismissed();
            }

            @Override
            public void onShowSucceed(AGNative result) {
                if (isDestroyed) {
                    return;
                }
                Log.d(TAG, "onShowSucceed");
                onInsShowSuccess();
            }

            @Override
            public void onLandPageShown(AGNative result) {
                if (isDestroyed) {
                    return;
                }
                Log.d(TAG, "onLandPageShown");
            }

            @Override
            public void onAdClicked(AGNative result) {
                if (isDestroyed) {
                    return;
                }
                Log.d(TAG, "onAdClicked");
                onInsClicked();
            }

            @Override
            public void onAdClosed(AGNative result) {
                if (isDestroyed) {
                    return;
                }
                Log.d(TAG, "onAdClosed");
                onInsDismissed();
            }
        });

        timer = new CountDownTimer(fetchDelay, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (isSplashReaday) {
                    if (timer != null) {
                        timer.cancel();
                    }
                }
            }

            @Override
            public void onFinish() {
                isTimerOut = true;
                if (timer != null) {
                    timer.cancel();
                }
                if (isDestroyed) {
                    return;
                }
                onInsError("AGSDK get splash Ad time out!");

            }
        };
        timer.start();
    }

    @Override
    public void show(ViewGroup viewGroup) {
        AdsgreatSDK.showSplashAd(slotID, viewGroup);

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

        public void onAdTimeOver() {

            showMsg("onAdTimeOver");

        }

        private void showMsg(String msg) {
        }
    }
}


