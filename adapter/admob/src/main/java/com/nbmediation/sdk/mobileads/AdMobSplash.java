// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.mobileads;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.nbmediation.sdk.mediation.CustomSplashEvent;
import com.nbmediation.sdk.mediation.MediationInfo;
import com.nbmediation.sdk.utils.AdLog;

import java.util.Date;
import java.util.Map;

public class AdMobSplash extends CustomSplashEvent {
    private static String TAG = "OM-Admob-splash: ";
    private long mExpireTimestamp;

    private String mAppKey;

    private boolean isTimerOut;

    private final Handler HANDLER = new Handler(Looper.getMainLooper());

    @Override
    public void loadAd(final Activity activity, final Map<String, String> config) {
        if (!check(activity, config)) {
            return;
        }
        String adMobAppKey = null;
        mAppKey = config.get("AppKey");
        try {
            ApplicationInfo appInfo = activity.getPackageManager().getApplicationInfo(activity.getPackageName(),
                    PackageManager.GET_META_DATA);
            Bundle bundle = appInfo.metaData;
            adMobAppKey = bundle.getString("com.google.android.gms.ads.APPLICATION_ID");
        } catch (Exception e) {
            AdLog.getSingleton().LogE(TAG + "can't find APPLICATION_ID in manifest.xml ");
        }

        if (!TextUtils.isEmpty(adMobAppKey)) {
            mAppKey = adMobAppKey;
        }
        if (TextUtils.isEmpty(mAppKey)) {
            MobileAds.initialize(activity.getApplicationContext());
            onInitSuccess(activity, config);
        } else {
            MobileAds.initialize(activity.getApplicationContext(), new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(InitializationStatus initializationStatus) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onInitSuccess(activity, config);
                        }
                    });
                }
            });
        }

    }

    public void onInitSuccess(Activity activity, Map<String, String> config) {
        loadSplashAd(activity, config.get("Timeout"));
    }

    @Override
    public void destroy(Activity activity) {
        isDestroyed = true;
        mExpireTimestamp = 0;
        appOpenAd = null;
        loadCallback = null;
    }


    private void loadSplashAd(Activity activity, String timeout) {
        int fetchDelay = 0;
        try {
            fetchDelay = Integer.parseInt(timeout);
        } catch (Exception e) {
        }
        if (fetchDelay <= 0) {
            fetchDelay = 0;
        }
        fetchAd(activity);
        isTimerOut = false;
        loadTime = 0;
        HANDLER.postDelayed(timeOutRunnable, fetchDelay);
    }


    private Runnable timeOutRunnable = new Runnable() {
        @Override
        public void run() {
            isTimerOut = true;
            if (isDestroyed || appOpenAd != null) {
                return;
            }
            onInsError(TAG + " get splash Ad time out!");
            AdLog.getSingleton().LogE(TAG + " get splash Ad time out!");
        }
    };

    @Override
    public void show(ViewGroup container) {
        if (isDestroyed) {
            return;
        }
        if (appOpenAd == null) {
            onInsShowFailed(TAG + "SplashAd not ready");
            return;
        }
        showAdIfAvailable((Activity) container.getContext());
    }

    @Override
    public boolean isReady() {
        return !isDestroyed && appOpenAd != null;
    }

    @Override
    public int getMediation() {
        return MediationInfo.MEDIATION_ID_6;
    }


    private static final String LOG_TAG = "AppOpenManager";
    private AppOpenAd appOpenAd = null;

    private AppOpenAd.AppOpenAdLoadCallback loadCallback;


    /**
     * Creates and returns ad request.
     */
    private AdRequest getAdRequest() {
        return new AdRequest.Builder().build();
    }

    /**
     * Utility method that checks if ad exists and can be shown.
     */
    public boolean isAdAvailable() {
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo();
    }

    /**
     * Request an ad
     */
    public void fetchAd(Activity activity) {
        // Have unused ad, no need to fetch another.
//        if (isAdAvailable()) {
//            return;
//        }

        loadCallback =
                new AppOpenAd.AppOpenAdLoadCallback() {
                    /**
                     * Called when an app open ad has loaded.
                     *
                     * @param ad the loaded app open ad.
                     */
                    @Override
                    public void onAppOpenAdLoaded(AppOpenAd ad) {
                        AdLog.getSingleton().LogE(TAG + "onAppOpenAdLoaded isTimerOut=" + isTimerOut);
                        HANDLER.removeCallbacks(timeOutRunnable);
                        loadTime = (new Date()).getTime();
                        if (!isTimerOut) {
                            appOpenAd = ad;
                            onInsReady(TAG + "onAppOpenAdLoaded");
                        }

                    }

                    /**
                     * Called when an app open ad has failed to load.
                     *
                     * @param loadAdError the error.
                     */
                    @Override
                    public void onAppOpenAdFailedToLoad(LoadAdError loadAdError) {
                        // Handle the error.
                        AdLog.getSingleton().LogE(TAG + "onAppOpenAdFailedToLoad isTimerOut=" + isTimerOut);
                        HANDLER.removeCallbacks(timeOutRunnable);
                        onInsError(loadAdError.getMessage());
                    }

                };
        AdRequest request = getAdRequest();
        AppOpenAd.load(
                activity.getApplication(), mInstancesKey, request,
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback);
    }

    private long loadTime = 0;

    /**
     * Utility method to check if ad was loaded more than n hours ago.
     */
    private boolean wasLoadTimeLessThanNHoursAgo() {
        long dateDifference = (new Date()).getTime() - this.loadTime;
        long numMilliSecondsPerHour = 3600000;
        return (dateDifference < (numMilliSecondsPerHour * (long) 4));
    }

    private static boolean isShowingAd = false;

    /**
     * Shows the ad if one isn't already showing.
     */
    public void showAdIfAvailable(final Activity activity) {
        // Only show ad if there is not already an app open ad currently showing
        // and an ad is available.
        if (!isShowingAd && isAdAvailable()) {
            Log.d(LOG_TAG, "Will show ad.");

            FullScreenContentCallback fullScreenContentCallback =
                    new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            // Set the reference to null so isAdAvailable() returns false.
                            appOpenAd = null;
                            isShowingAd = false;
//                            fetchAd(activity);
                            onInsDismissed();
                            AdLog.getSingleton().LogE(TAG + "onAdDismissedFullScreenContent isTimerOut=" + isTimerOut);
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(AdError adError) {
                            onInsShowFailed(TAG + "onAdFailedToShowFullScreenContent");
                            AdLog.getSingleton().LogE(TAG + "onAdFailedToShowFullScreenContent isTimerOut=" + isTimerOut);

                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            isShowingAd = true;
                            onInsShowSuccess();
                            AdLog.getSingleton().LogE(TAG + "onAdShowedFullScreenContent isTimerOut=" + isTimerOut);
                        }
                    };
            appOpenAd.show(activity, fullScreenContentCallback);

        } else {
            onInsShowFailed(TAG + "Can not show ad. isShowingAd=" + isShowingAd + ",isAdAvailable=" + isAdAvailable());
//            fetchAd(activity);
        }
    }


}
