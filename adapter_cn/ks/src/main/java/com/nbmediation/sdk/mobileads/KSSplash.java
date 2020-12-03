package com.nbmediation.sdk.mobileads;

import android.app.Activity;

import android.content.Context;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;


import com.kwad.sdk.api.KsAdSDK;
import com.kwad.sdk.api.KsLoadManager.SplashScreenAdListener;
import com.kwad.sdk.api.KsScene;
import com.kwad.sdk.api.KsSplashScreenAd;
import com.kwad.sdk.api.SdkConfig;
import com.nbmediation.sdk.mediation.CustomSplashEvent;
import com.nbmediation.sdk.mediation.MediationInfo;
import com.nbmediation.sdk.utils.AdLog;

import java.util.Map;

public class KSSplash extends CustomSplashEvent {

    private static final String CONFIG_TIMEOUT = "Timeout";
    private final static String TAG = "OM-KSSplashAd";
    private static boolean isSplashReady;
    private CountDownTimer timer;
    private int fetchDelay;
    private boolean isTimerOut;
    private Fragment fragment;

    public void loadAd(Activity activity, Map<String, String> config) {
        if (isDestroyed) {
            return;
        }

        AdLog.getSingleton().LogD(TAG, "getLoadManager check");
        if (!check(activity, config)) {
            AdLog.getSingleton().LogD(TAG, "check(activity, config) error.");
            onInsError(TAG + "check(activity, config) error.");
            return;
        }
        if (activity == null || activity.isFinishing()) {
            AdLog.getSingleton().LogD(TAG, "activity is null.");
            onInsError(TAG + "activity is null");
            return;
        }

        String appKeyStr = config.get("AppKey");
        String appID;
        String appName = null;
        boolean isDebug = false;
        String[] split = new String[0];
        if (appKeyStr != null) {
            split = appKeyStr.split("\\|");
        }
        if (split.length > 0) {
            appID = split[0];
        } else {
            AdLog.getSingleton().LogD(TAG, "not input AppID.");
            onInsError(TAG + "not input AppID.");
            return;
        }
        if (split.length > 1) {
            appName = split[1];
        }

        if (split.length > 2) {
            try {
                isDebug = Boolean.parseBoolean(split[2]);
            } catch (Exception ignored) {
            }
        }
        initSdk(activity, appID, appName, isDebug);
        splashPreload(activity, config);

    }

    private void initSdk(final Context activity, String appId, String appName, boolean isDebug) {
        KsAdSDK.init(activity, new SdkConfig.Builder()
                .appId(appId) // aapId，必填
                .appName(appName) //appName，非必填
                .showNotification(true) // 是否展示下载通知栏 .debug(true)
                .debug(isDebug) // 是否开启sdk 调试⽇日志 可选
                .build());
    }

    @Override
    public int getMediation() {
        return MediationInfo.MEDIATION_ID_22;
    }

    @Override
    public void destroy(Activity activity) {
        if (timer != null) {
            timer.cancel();
        }
        isDestroyed = true;
    }

    public void splashPreload(final Activity activity, Map<String, String> config) {
        if (isDestroyed) {
            return;
        }
        try {
            fetchDelay = Integer.parseInt(config.get(CONFIG_TIMEOUT));
        } catch (Exception e) {
            fetchDelay = 3000;
        }
        isTimerOut = false;
        KsScene scene = new KsScene.Builder(Long.parseLong(mInstancesKey)).build(); // 此为测试posId，请联系快手平台申请正式posId
        KsAdSDK.getLoadManager().loadSplashScreenAd(scene, createLoadListener());

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
                onInsError(TAG + "KSSDK get splash Ad time out!");
                AdLog.getSingleton().LogD(TAG, "KSSDK get splash Ad time out!");
            }
        };
        timer.start();
    }


    private SplashScreenAdListener createLoadListener() {
        return new SplashScreenAdListener() {
            @Override
            public void onError(int code, String msg) {
                AdLog.getSingleton().LogD(TAG, "Splash Ad onError");
                onInsError(TAG + "开屏广告请求失败" + code + msg);

            }

            @Override
            public void onSplashScreenAdLoad(@NonNull KsSplashScreenAd splashScreenAd) {
                if (isDestroyed) {
                    return;
                }

                AdLog.getSingleton().LogD(TAG, "Splash Ad Loaded");
                isSplashReady = true;
                fragment = splashScreenAd.getFragment(createInteractionListener());
                if (!isTimerOut) {
                    onInsReady(null);
                }
            }
        };
    }


    private KsSplashScreenAd.SplashScreenAdInteractionListener createInteractionListener() {
        return new KsSplashScreenAd.SplashScreenAdInteractionListener() {
            @Override
            public void onAdClicked() {

                if (isDestroyed) {
                    return;
                }
                AdLog.getSingleton().LogD(TAG, "onAdClicked");
                onInsClicked();
            }

            @Override
            public void onAdShowError(int code, String extra) {
                isSplashReady = false;
                if (isDestroyed) {
                    return;
                }
                AdLog.getSingleton().LogD(TAG, "onAdShowError");
            }

            @Override
            public void onAdShowEnd() {
                isSplashReady = false;
                if (isDestroyed) {
                    return;
                }
                AdLog.getSingleton().LogD(TAG, "onAdShowEnd");
                onInsTick(0);
                onInsDismissed();
            }

            @Override
            public void onAdShowStart() {
                if (isDestroyed) {
                    return;
                }
                AdLog.getSingleton().LogD(TAG, "onAdShowStart");
                onInsShowSuccess();
            }

            @Override
            public void onSkippedAd() {
                isSplashReady = false;
                if (isDestroyed) {
                    return;
                }
                AdLog.getSingleton().LogD(TAG, "onSkippedAd");
                onInsDismissed();
            }
        };
    }

    @Override
    public void show(ViewGroup viewGroup) {
        AdLog.getSingleton().LogD(TAG, "show");
        if (isDestroyed || fragment == null) {
            onInsShowFailed(TAG + "fragment null show failed!");
            return;
        }
        Context context = viewGroup.getContext();
        //可能会有坑,需要开传入的active是AppCompatActivity,viewGroup在layout里有id
        if (context instanceof AppCompatActivity) {
            ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction()
                    .replace(viewGroup.getId(), fragment)
                    .commitAllowingStateLoss();
            fragment = null;
        } else {
            onInsShowFailed(TAG + "not AppCompatActivity show failed!");
        }

    }

    @Override
    public boolean isReady() {
        return !isDestroyed && isSplashReady;
    }


}


