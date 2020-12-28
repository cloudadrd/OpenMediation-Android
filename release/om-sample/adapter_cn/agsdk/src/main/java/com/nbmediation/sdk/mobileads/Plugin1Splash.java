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
import com.nbmediation.sdk.utils.AdLog;

import java.util.Map;

public class Plugin1Splash extends CustomSplashEvent {
    private static final String CONFIG_TIMEOUT = "Timeout";
    private static String slotID;
    private static boolean isSplashReady;
    private CountDownTimer timer;
    private int fetchDelay;
    private boolean isTimerOut;
    private static String TAG = "OM-AG-Splash:";

    public void loadAd(Activity activity, Map<String, String> config) {
        if (!check(activity, config)) {
            AdLog.getSingleton().LogD(TAG, "check(activity, config) error");
            return;
        }
        slotID = mInstancesKey;
        splashPreload(activity, config);
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

    public void splashPreload(Activity activity, Map<String, String> config) {
        AdLog.getSingleton().LogD(TAG, "splashPreload");
        if (isDestroyed) {
            return;
        }
        try {
            fetchDelay = Integer.parseInt(config.get(CONFIG_TIMEOUT));
        } catch (Exception e) {
            fetchDelay = 3000;
        }

        AdsgreatSDK.initialize(activity, slotID);
        isTimerOut = false;
        AdsgreatSDK.preloadSplashAd(activity, slotID, new AdEventListener() {

            @Override
            public void onReceiveAdSucceed(AGNative result) {
                if (isDestroyed) {
                    return;
                }

                AdLog.getSingleton().LogD(TAG, "Splash Ad Loaded.");
                isSplashReady = true;
                if (!isTimerOut) {
                    onInsReady(null);
                }
            }

            @Override
            public void onReceiveAdVoSucceed(AdsVO adsVO) {
                AdLog.getSingleton().LogD(TAG, "onReceiveAdVoSucceed");
            }

            @Override
            public void onReceiveAdFailed(AGNative result) {
                if (isDestroyed) {
                    return;
                }
                if (result != null && result.getErrorsMsg() != null)
                    AdLog.getSingleton().LogD(TAG, "onReceiveAdFailed errorMsg=" + result.getErrorsMsg());
                onInsError(result.getErrorsMsg());
            }

            @Override
            public void onAdTimeOver() {
                isSplashReady = false;
                if (isDestroyed) {
                    return;
                }
                AdLog.getSingleton().LogD(TAG, "onAdTimeOver");
                onInsTick(0);
            }

            @Override
            public void onShowSucceed(AGNative result) {
                if (isDestroyed) {
                    return;
                }
                AdLog.getSingleton().LogD(TAG, "onShowSucceed");
                onInsShowSuccess();
            }

            @Override
            public void onLandPageShown(AGNative result) {
                if (isDestroyed) {
                    return;
                }
                AdLog.getSingleton().LogD(TAG, "onLandPageShown");
            }

            @Override
            public void onAdClicked(AGNative result) {
                if (isDestroyed) {
                    return;
                }
                AdLog.getSingleton().LogD(TAG, "onAdClicked");
                onInsClicked();
            }

            @Override
            public void onAdClosed(AGNative result) {
                isSplashReady = false;
                if (isDestroyed) {
                    return;
                }
                AdLog.getSingleton().LogD(TAG, "onAdClosed");
                onInsDismissed();
            }
        });

        timer = new CountDownTimer(fetchDelay, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (isSplashReady) {
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
                if (isDestroyed || isSplashReady) {
                    return;
                }
                onInsError(TAG + "get splash Ad time out!");
                AdLog.getSingleton().LogD(TAG, "AGSDK get splash Ad time out!");
            }
        };
        timer.start();
    }

    @Override
    public void show(ViewGroup viewGroup) {
        if (isDestroyed) {
            return;
        }
        AdLog.getSingleton().LogD(TAG, "show");

        AdsgreatSDK.showSplashAd(slotID, viewGroup);

    }

    @Override
    public boolean isReady() {
        return !isDestroyed && isSplashReady;
    }

}


