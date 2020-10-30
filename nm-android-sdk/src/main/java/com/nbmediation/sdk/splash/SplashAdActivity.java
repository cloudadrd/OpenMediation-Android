// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.splash;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

import com.nbmediation.sdk.R;
import com.nbmediation.sdk.utils.AdLog;


public class SplashAdActivity extends Activity implements SplashAdListener {

    ViewGroup mSplashContainer;

    private long loadTimeout;

    private static String SPLASH_LOAD_TIMEOUT = "splash_load_timeout";

    private final static String TAG = "Om_SplashAdActivity";

//    private SplashAdListener mSplashAdListener;
//
//    public static class SplashHolder {
//        private static SplashHolder INSTANCE = new SplashHolder();
//
//        private SplashAdListener splashAdListener = null;
//
//        private SplashHolder() {
//        }
//
//        public static SplashHolder getInstance() {
//            return INSTANCE;
//        }
//
//    }

    public static void showMe(Context context, long loadTimeOut) {
        Intent intent = new Intent(context, SplashAdActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS |
                Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.putExtra(SPLASH_LOAD_TIMEOUT, loadTimeOut);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_splash);
        loadTimeout = getIntent().getLongExtra(SPLASH_LOAD_TIMEOUT, -1);
        mSplashContainer = findViewById(R.id.splash_container);
        loadSplash();
    }


    public void loadSplash() {
        SplashAd.setSplashAdListener(this);
        int width = mSplashContainer.getWidth();
        int height = mSplashContainer.getHeight();
        SplashAd.setSize(width, height);
        if (loadTimeout != -1) {
            SplashAd.setLoadTimeout(loadTimeout);
        } else {
            SplashAd.setLoadTimeout(3000);
        }

        SplashAd.loadAd();
    }

    @Override
    public void onSplashAdLoad() {
        AdLog.getSingleton().LogD(TAG, "----------- onSplashAdLoad ----------");
        SplashAd.showAd(mSplashContainer);
    }

    @Override
    public void onSplashAdFailed(String error) {
        AdLog.getSingleton().LogD(TAG, "----------- onSplashAdFailed ----------" + error);
        finish();
    }

    @Override
    public void onSplashAdClicked() {
        AdLog.getSingleton().LogD(TAG, "----------- onSplashAdClicked ----------");
    }

    @Override
    public void onSplashAdShowed() {
        AdLog.getSingleton().LogD(TAG, "----------- onSplashAdShowed ----------");
    }

    @Override
    public void onSplashAdShowFailed(String error) {
        AdLog.getSingleton().LogD(TAG, "----------- onSplashAdShowFailed ----------" + error);
        finish();
    }

    @Override
    public void onSplashAdTick(long millisUntilFinished) {
        AdLog.getSingleton().LogD(TAG, "----------- onSplashAdTick ----------" + millisUntilFinished);
    }

    @Override
    public void onSplashAdDismissed() {
        AdLog.getSingleton().LogD(TAG, "----------- onSplashAdDismissed ----------");
        finish();
    }

    @Override
    protected void onDestroy() {
        SplashAd.setSplashAdListener(null);
        if (mSplashContainer != null) {
            mSplashContainer.removeAllViews();
        }
        super.onDestroy();
    }
}
