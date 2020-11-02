package com.nbmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
import com.nbmediation.sdk.mobileads.plugin6.R;
import com.nbmediation.sdk.utils.AdLog;

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
public class BaiduSplash extends Activity {//extends CustomSplashEvent
//    private SplashAd splashAd;
//    private static String TAG = "OM-Baidu:";
//    // 控制开屏广告在落地页关闭后自动关闭，并进入到媒体的应用主页
////    private boolean mExitAfterLp;
////    private boolean needAppLogo = true;
//    @Override
//    public void loadAd(Activity activity, Map<String, String> config) {
//        if (!check(activity, config)) {
//            return;
//        }
//        loadSplashAd(activity, config);
//    }
//
//    @Override
//    public int getMediation() {
//        return 0;
//    }
//
//    @Override
//    public void destroy(Activity activity) {
//
//    }
//
//
//    private void loadSplashAd(Activity activity, Map<String, String> config) {
//
//        // 1. 设置开屏广告请求参数，图片宽高单位dp 非必选
//        final RequestParameters parameters = new RequestParameters.Builder()
//                .setHeight(640)
//                .setWidth(360)
//                .build();
//
//        // 2. 设置开屏listener
//        final SplashLpCloseListener listener = new SplashLpCloseListener() {
//            @Override
//            public void onLpClosed() {
////                Toast.makeText(activity.this,"lp页面关闭",Toast.LENGTH_SHORT).show();
//                // 落地页关闭后关闭广告，并跳转到应用的主页
//            }
//
//            @Override
//            public void onAdDismissed() {
//                Log.i("RSplashManagerActivity", "onAdDismissed");
//            }
//
//            @Override
//            public void onADLoaded() {
//                Log.i("RSplashManagerActivity", "onADLoaded");
//            }
//
//            @Override
//            public void onAdFailed(String arg0) {
//                Log.i("RSplashManagerActivity", arg0);
//            }
//
//            @Override
//            public void onAdPresent() {
//                Log.i("RSplashManagerActivity", "onAdPresent");
//            }
//
//            @Override
//            public void onAdClick() {
//                Log.i("RSplashManagerActivity", "onAdClick");
//            }
//        };
//         /**
//         * 实例化开屏广告的构造函数
//         * fetchAd：是否自动请求广告, 设置为true则自动loadAndshow，无需再主动load和show
//         * 设置为false则仅初始化开屏广告对象，需要手动调用load请求广告，并调用show展示广告
//         **/
////         splashAd = new SplashAd(activity.getApplicationContext(), activity, listener, adPlaceId, true,
////                        parameters, 4200, false);
////         splashAd.load();
//
//    }
//
//    @Override
//    public void show(ViewGroup container) {
//        if (!isReady()) {
//            onInsShowFailed("SplashAd not ready");
//            return;
//        }
//        try {
////            if (splashAd != null) {
////]                splashAd.show();
//            }
//        } catch (Exception e) {
//            onInsShowFailed("SplashAd not ready");
//        }
//    }
//
//    @Override
//    public boolean isReady() {
//        return false;
//    }
//
//    @Override
//    public void onTimeout() {
//        if (isDestroyed) {
//            return;
//        }
//        AdLog.getSingleton().LogD(TAG + "Splash ad load failed: timeout");
//        onInsError("Splash ad load failed: timeout");
//    }
//
//
//
//
//    @Override
//    protected void onDestroy() {
//
//    }
}