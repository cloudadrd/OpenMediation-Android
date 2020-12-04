package com.nbmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nbmediation.sdk.mediation.CustomSplashEvent;
import com.baidu.mobad.feeds.RequestParameters;
import com.baidu.mobads.AdSettings;
import com.baidu.mobads.BitmapDisplayMode;
import com.baidu.mobads.SplashAd;
import com.baidu.mobads.SplashLpCloseListener;
import com.nbmediation.sdk.mediation.MediationInfo;
import com.nbmediation.sdk.mobileads.plugin6.R;
import com.nbmediation.sdk.utils.AdLog;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 开屏完整示例
 * 1. 快捷接入请参考RSplashActivity，完整示例请参考RSplashManagerActivity
 * 2. 开屏广告需嵌入应用启动页Activity中。
 * 3. 开屏广告支持自定义跳过按钮，需在百青藤平台配置广告位设置。
 * 4. 设置开屏广告请求参数，非必选。
 * 5. 设置开屏listener
 * 6. 实例化开屏广告对象，canClick建议为true，否则影响填充
 * 7. 请求开屏广告，可直接loadAndShow（实时请求并展示），也可拆分load和show，分开延时展示。
 * 根据工信部的规定，不再默认申请权限，而是主动弹框由用户授权使用。
 * 如果是Android6.0以下的机器, 或者targetSDKVersion < 23，默认在安装时获得了所有权限，可以直接调用SDK
 */
public class BaiduSplash extends CustomSplashEvent {//extends CustomSplashEvent slotID = "2058622";
    private SplashAd splashAd;
    private static String TAG = "OM-Baidu-Splash:";
    private static final String CONFIG_TIMEOUT = "Timeout";
    private static final String CONFIG_WIDTH = "Width";
    private static final String CONFIG_HEIGHT = "Height";
    private String adPlaceId;
    private WeakReference<ViewGroup> mViewGroup;
    private Boolean isSplashReady;
    private CountDownTimer timer;
    private boolean isTimerOut;
    private boolean isShowing;
    public void loadAd(Activity activity, Map<String, String> config, WeakReference<ViewGroup> viewGroup) {
        if (!check(activity, config) || isDestroyed) {
            return;
        }

        mViewGroup = viewGroup;
        adPlaceId = mInstancesKey;
        loadSplashAd(activity, config);
    }

    @Override
    public int getMediation() {
        return MediationInfo.MEDIATION_ID_55;
    }

    private static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    private static int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    private void loadSplashAd(Activity activity, Map<String, String> config) {
       //获取内容
        int fetchDelay;
        try {
            fetchDelay = Integer.parseInt(config.get(CONFIG_TIMEOUT));
        } catch (Exception e) {
            fetchDelay = 3000;
        }
        int width = 0;
        try {
            width = Integer.parseInt(config.get(CONFIG_WIDTH));
        } catch (Exception ignored) {
        }
        if (width <= 0) {
            width = getScreenWidth(activity);
        }
        int height = 0;
        try {
            height = Integer.parseInt(config.get(CONFIG_HEIGHT));
        } catch (Exception ignored) {
        }
        if (height <= 0) {
            height = getScreenHeight(activity);
        }

        final RequestParameters parameters = new RequestParameters.Builder()
                .setHeight(height)
                .setWidth(width)
                .build();

        //  设置开屏listener
        final SplashLpCloseListener listener = new SplashLpCloseListener() {
            @Override
            public void onLpClosed() {
                // 落地页关闭后关闭广告，并跳转到应用的主页
//                onInsDismissed();
//                Log.i(TAG, "onLpClosed");
            }

            @Override
            public void onAdDismissed() {
                Log.i(TAG, "onAdDismissed");
                onInsDismissed();
                isSplashReady = false;
                isShowing = false;
            }

            @Override
            public void onADLoaded() {
                if(!isTimerOut){
                    Log.i(TAG, "onADLoaded");
                    isSplashReady = true;
                    onInsReady(null);
                }
            }

            @Override
            public void onAdFailed(String arg0) {
                isSplashReady = false;
                isShowing = false;
                Log.i(TAG, arg0);
                onInsError("Baidu Splash ad load failed " + arg0);
            }

            @Override
            public void onAdPresent() {
                Log.i(TAG, "onAdPresent");
                onInsShowSuccess();
            }

            @Override
            public void onAdClick() {
                Log.i(TAG, "onAdClick");
                onInsClicked();
            }
        };
         /**
         * 实例化开屏广告的构造函数
         * fetchAd：是否自动请求广告, 设置为true则自动loadAndshow，无需再主动load和show
         * 设置为false则仅初始化开屏广告对象，需要手动调用load请求广告，并调用show展示广告
         **/
         splashAd = new SplashAd(activity, mViewGroup.get(), listener, adPlaceId, true,
                        parameters, fetchDelay, false);
         splashAd.load();

        isSplashReady = false;
        isTimerOut = false;
        isShowing = false;
        timer = new CountDownTimer(fetchDelay, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (isSplashReady) {
                    if (timer != null) {
                        timer.cancel();
                        timer = null;
                    }
                }
            }

            @Override
            public void onFinish() {
                isTimerOut = true;
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
                if (isDestroyed || isSplashReady) {
                    return;
                }
                onInsError("Baidu get splash Ad time out!");
                splashAd = null;
                AdLog.getSingleton().LogD(TAG,"Baidu get splash Ad time out!");
            }
        };
        timer.start();
    }

    @Override
    public void show(ViewGroup container) {
        if (!isReady() || isShowing) {
            Log.i(TAG, "SplashAd not ready");
            onInsShowFailed("SplashAd not ready");
            return;
        }
        try {
            if (splashAd != null) {
                Log.i(TAG, "show");
               splashAd.show();
                isShowing = true;
            }
        } catch (Exception e) {
            Log.i(TAG, "SplashAd not ready");
            onInsShowFailed("SplashAd not ready");
        }
    }

    @Override
    public boolean isReady() {
        if(splashAd == null || isDestroyed)
            return false;
        return isSplashReady;
    }

@Override
    public void destroy(Activity activity) {
        mViewGroup = null;
        isDestroyed = true;
        if (timer != null) {
            timer.cancel();
        }
    }
}

