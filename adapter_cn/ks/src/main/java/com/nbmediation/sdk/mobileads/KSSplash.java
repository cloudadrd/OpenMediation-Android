package com.nbmediation.sdk.mobileads;
import android.app.Activity;

import android.os.CountDownTimer;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;


import com.kwad.sdk.api.KsAdSDK;
import com.kwad.sdk.api.KsLoadManager.SplashScreenAdListener;
import com.kwad.sdk.api.KsScene;
import com.kwad.sdk.api.KsSplashScreenAd;
import com.nbmediation.sdk.mediation.CustomSplashEvent;
import com.nbmediation.sdk.mediation.MediationInfo;
import com.nbmediation.sdk.utils.AdLog;

import java.util.Map;

public class KSSplash extends CustomSplashEvent {
    private static final String CONFIG_TIMEOUT = "Timeout";
    private final static String TAG = "KSSplashAd";
    private static String slotID;
    private static boolean isSplashReaday;
    private CountDownTimer timer;
    private int fetchDelay;
    private boolean isTimerOut;
    private Activity actv;
    private Fragment fragment;

    public void loadAd(Activity activity, Map<String, String> config) {
        if (isDestroyed) {
            return;
        }

        if (!check(activity, config)) {
            return;
        }
        try {
            fetchDelay = Integer.parseInt(config.get(CONFIG_TIMEOUT));
        } catch (Exception e) {
            fetchDelay = 3000;
        }
        splashPreload(activity,config);
        actv = activity;
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

    public void splashPreload(final Activity activity, Map<String, String> config){
        if (isDestroyed) {
            return;
        }
        try {
            fetchDelay = Integer.parseInt(config.get(CONFIG_TIMEOUT));
        } catch (Exception e) {
            fetchDelay = 3000;
        }

        KsScene scene = new KsScene.Builder(Long.parseLong(mInstancesKey)).build(); // 此为测试posId，请联系快手平台申请正式posId
        KsAdSDK.getLoadManager().loadSplashScreenAd(scene, new SplashScreenAdListener() {
            @Override
            public void onError(int code, String msg) {
                AdLog.getSingleton().LogD("开屏广告请求失败" + code + msg);
                onInsError("开屏广告请求失败" + code + msg);

            }

            @Override
            public void onSplashScreenAdLoad(@NonNull KsSplashScreenAd splashScreenAd) {
                if (isDestroyed) {
                    return;
                }

                Log.d(TAG, "Splash Ad Loaded.");
                isSplashReaday = true;
                if (!isTimerOut) {
                    onInsReady(null);
                }

                 fragment =
                        splashScreenAd.getFragment(new KsSplashScreenAd.SplashScreenAdInteractionListener() {
                            @Override
                            public void onAdClicked() {

                                if (isDestroyed) {
                                    return;
                                }
                                Log.d(TAG, "onAdClicked");
                                onInsClicked();
                            }

                            @Override
                            public void onAdShowError(int code, String extra) {
                                if (isDestroyed) {
                                    return;
                                }
                                Log.d(TAG, "onAdShowError");
                            }

                            @Override
                            public void onAdShowEnd() {
                                if (isDestroyed) {
                                    return;
                                }
                                Log.d(TAG, "onAdShowEnd");
                                onInsTick(0);

                            }

                            @Override
                            public void onAdShowStart()
                            {
                                if (isDestroyed) {
                                    return;
                                }
                                Log.d(TAG, "onAdShowStart");
                                onInsShowSuccess();
                            }

                            @Override
                            public void onSkippedAd() {
                                if (isDestroyed) {
                                    return;
                                }
                                Log.d(TAG, "onSkippedAd");
                                onInsDismissed();
                            }
                        });
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
                Log.d(TAG, "AGSDK get splash Ad time out!");
            }
        };
        timer.start();
    }


    @Override
    public void show(ViewGroup viewGroup) {
        if (isDestroyed) {
            return;
        }

        //可能会有坑,需要开传入的active是AppCompatActivity,viewGroup在layout里有id
        if(actv instanceof AppCompatActivity){
            ((AppCompatActivity)actv).getSupportFragmentManager().beginTransaction()
                    .replace(viewGroup.getId(), fragment)
                    .commitAllowingStateLoss();
        }

    }

    @Override
    public boolean isReady() {
        return  !isDestroyed && isSplashReaday;
    }


}


