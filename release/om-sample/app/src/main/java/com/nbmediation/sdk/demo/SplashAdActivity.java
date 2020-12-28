// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewGroup;

import com.nbmediation.sdk.InitCallback;
import com.nbmediation.sdk.NmAds;
import com.nbmediation.sdk.demo.utils.NewApiUtils;
import com.nbmediation.sdk.splash.SplashAd;
import com.nbmediation.sdk.splash.SplashAdListener;
import com.nbmediation.sdk.utils.error.Error;


public class SplashAdActivity extends AppCompatActivity implements SplashAdListener {

    ViewGroup mSplashContainer;

    private boolean isClick = false;

    private boolean isLoad = false;

    public static boolean isSdkInit = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_splash);
        Log.i("time_log", "Splash onCreate into time=" + System.currentTimeMillis());
        mSplashContainer = findViewById(R.id.splash_container);
        init();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isLoad) {
                toMainPage();
            }
        }, 5000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isClick) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    public void init() {
        Log.i("time_log", "init start time=" + System.currentTimeMillis());
        NewApiUtils.printLog("start init sdk");
        NmAds.setCustomId("test123");
        NmAds.init(this, NewApiUtils.APPKEY, new InitCallback() {
            @Override
            public void onSuccess() {
                isSdkInit = true;
                NewApiUtils.printLog("init success");
                Log.i("time_log", "init end time=" + System.currentTimeMillis());
                loadSplash();
            }

            @Override
            public void onError(Error result) {
                NewApiUtils.printLog("init failed " + result.toString());
                toMainPage();
            }
        });
    }

    public void loadSplash() {
        Log.i("time_log", "loadSplash start time=" + System.currentTimeMillis());
        SplashAd.setSplashAdListener(this);
        int width = mSplashContainer.getWidth();
        int height = mSplashContainer.getHeight();
        SplashAd.setSize(width, height);
        SplashAd.setLoadTimeout(6000);
        SplashAd.setViewGroup(mSplashContainer);
        SplashAd.loadAd();
    }

    @Override
    public void onSplashAdLoad() {
        isLoad = true;
        Log.i("time_log", "loadSplash success time=" + System.currentTimeMillis());
        Log.e("SplashAdActivity", "----------- onSplashAdLoad ----------");
        SplashAd.showAd(mSplashContainer);
    }

    @Override
    public void onSplashAdFailed(String error) {
        Log.e("SplashAdActivity", "----------- onSplashAdFailed ----------" + error);
        toMainPage();
    }

    @Override
    public void onSplashAdClicked() {
        isClick = true;
        Log.e("SplashAdActivity", "----------- onSplashAdClicked ----------");
    }

    @Override
    public void onSplashAdShowed() {
        Log.e("SplashAdActivity", "----------- onSplashAdShowed ----------");
    }

    @Override
    public void onSplashAdShowFailed(String error) {
        Log.e("SplashAdActivity", "----------- onSplashAdShowFailed ----------" + error);
        toMainPage();
    }

    @Override
    public void onSplashAdTick(long millisUntilFinished) {
        Log.e("SplashAdActivity", "----------- onSplashAdTick ----------" + millisUntilFinished);
        if (millisUntilFinished <= 0) {
            if (!isClick) {
                toMainPage();
            }
        }
    }

    @Override
    public void onSplashAdDismissed() {
        Log.e("SplashAdActivity", "----------- onSplashAdDismissed ----------");
        toMainPage();
    }

    public void toMainPage() {
        Log.e("SplashAdActivity", "----------- toMainPage ----------");
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        SplashAd.setSplashAdListener(null);
        super.onDestroy();
    }
}
