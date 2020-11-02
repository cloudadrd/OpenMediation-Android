// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

import com.nbmediation.sdk.InitCallback;
import com.nbmediation.sdk.NmAds;
import com.nbmediation.sdk.demo.utils.NewApiUtils;
import com.nbmediation.sdk.splash.SplashAd;
import com.nbmediation.sdk.splash.SplashAdListener;
import com.nbmediation.sdk.utils.error.Error;


public class SplashAdActivity extends Activity implements SplashAdListener {

    ViewGroup mSplashContainer;

    public boolean isLoad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_splash);
        mSplashContainer = findViewById(R.id.splash_container);
        init();
//        mSplashContainer.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                if (!isLoad) finish();
//            }
//        }, 5300);
    }

    public void init() {
        NewApiUtils.printLog("start init sdk");
        NmAds.setCustomId("test123");
        NmAds.init(this, NewApiUtils.APPKEY, new InitCallback() {
            @Override
            public void onSuccess() {
                NewApiUtils.printLog("init success");
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
        SplashAd.setSplashAdListener(this);
        int width = mSplashContainer.getWidth();
        int height = mSplashContainer.getHeight();
        SplashAd.setSize(width, height);
        SplashAd.setLoadTimeout(3000);
        SplashAd.loadAd();
    }

    @Override
    public void onSplashAdLoad() {
        Log.e("SplashAdActivity", "----------- onSplashAdLoad ----------");
        isLoad = true;
        SplashAd.showAd(mSplashContainer);
    }

    @Override
    public void onSplashAdFailed(String error) {
        Log.e("SplashAdActivity", "----------- onSplashAdFailed ----------" + error);
        toMainPage();
    }

    @Override
    public void onSplashAdClicked() {
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
    }

    @Override
    public void onSplashAdDismissed() {
        Log.e("SplashAdActivity", "----------- onSplashAdDismissed ----------");
        toMainPage();
    }

    public void toMainPage(){
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
    @Override
    protected void onDestroy() {
        SplashAd.setSplashAdListener(null);
        super.onDestroy();
    }
}