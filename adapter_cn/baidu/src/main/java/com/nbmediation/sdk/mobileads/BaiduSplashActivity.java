package com.nbmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mobad.feeds.RequestParameters;
import com.baidu.mobads.AdSettings;
import com.baidu.mobads.BitmapDisplayMode;
import com.baidu.mobads.SplashAd;
import com.baidu.mobads.SplashLpCloseListener;
import com.nbmediation.sdk.mobileads.plugin6.R;

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
public class BaiduSplashActivity extends Activity {
    private SplashAd splashAd;
    private RelativeLayout adsParent;
    private String adPlaceId;
    // 控制开屏广告在落地页关闭后自动关闭，并进入到媒体的应用主页
//    private boolean mExitAfterLp;
//    private boolean needAppLogo = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_manager);
        adsParent = this.findViewById(R.id.adsRl);
        initView();
    }

    private void initView() {

        // 1. 设置开屏广告请求参数，图片宽高单位dp 非必选
        final RequestParameters parameters = new RequestParameters.Builder()
                .setHeight(640)
                .setWidth(360)
                .build();
//        final String adPlaceId = "2058622"; // 重要：请填上您的广告位ID，代码位错误会导致无法请求到广告
//        AdSettings.setSupportHttps(false); // 自由选择使用http或者https请求，默认https
//        SplashAd.setBitmapDisplayMode(BitmapDisplayMode.DISPLAY_MODE_FIT_XY);// 根据需求自由设置开屏图片拉伸方式
        // 设置视频广告最大缓存占用空间(15MB~100MB),默认30MB,单位MB
        // SplashAd.setMaxVideoCacheCapacityMb(30);
        Intent intent = getIntent();

        if (intent != null) {
            adPlaceId = intent.getStringExtra("adPlace_Id");
        }

        // 2. 设置开屏listener
        final SplashLpCloseListener listener = new SplashLpCloseListener() {
            @Override
            public void onLpClosed() {
                Toast.makeText(BaiduSplashActivity.this,"lp页面关闭",Toast.LENGTH_SHORT).show();
                // 落地页关闭后关闭广告，并跳转到应用的主页
                destorySplash();
            }

            @Override
            public void onAdDismissed() {
                Log.i("RSplashManagerActivity", "onAdDismissed");
                destorySplash();
            }

            @Override
            public void onADLoaded() {
                Log.i("RSplashManagerActivity", "onADLoaded");
                splashAdShow();
            }

            @Override
            public void onAdFailed(String arg0) {
                Log.i("RSplashManagerActivity", arg0);
                destorySplash();
            }

            @Override
            public void onAdPresent() {
                adsParent.setVisibility(View.VISIBLE);
                Log.i("RSplashManagerActivity", "onAdPresent");
            }

            @Override
            public void onAdClick() {
                Log.i("RSplashManagerActivity", "onAdClick");
            }
        };
         /**
         * 实例化开屏广告的构造函数
         * fetchAd：是否自动请求广告, 设置为true则自动loadAndshow，无需再主动load和show
         * 设置为false则仅初始化开屏广告对象，需要手动调用load请求广告，并调用show展示广告
         **/
         splashAd = new SplashAd(BaiduSplashActivity.this, adsParent, listener, adPlaceId, true,
                        parameters, 4200, false);
         splashAd.load();

    }

    public void splashAdShow() {
        if (adsParent != null) {
            if (splashAd != null) {
                adsParent.setVisibility(View.VISIBLE);
                splashAd.show();
            } else {
                Log.i("RSplashManagerActivity", "请检查开屏对象是否存在异常");
            }
        }
    }

    private void destorySplash() {
        adsParent.setVisibility(View.INVISIBLE);
        if (splashAd != null) {
            splashAd.destroy();
            splashAd = null;
        }
        adsParent.removeAllViews();
        this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (splashAd != null) {
            splashAd.destroy();
            splashAd = null;
        }
        this.finish();
    }
}
