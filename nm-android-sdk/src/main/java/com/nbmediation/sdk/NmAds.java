// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk;

import android.app.Activity;

import com.nbmediation.sdk.core.NmManager;
import com.nbmediation.sdk.utils.cache.GlobalVariable;
import com.nbmediation.sdk.utils.constant.CommonConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The type Om ads.
 */
public abstract class NmAds {


    public static Map<String, ClassLoader> PLUGIN_LOADERS = new ConcurrentHashMap<>();

    /**
     * mediation SDK init method
     *
     * @param activity required param
     * @param appKey   required param: current app's identifier
     * @param callback the callback
     * @param types    optional param: ad types to be preloaded; null means preload all
     */
    public static void init(Activity activity, String appKey, InitCallback callback, AD_TYPE... types) {
        init(activity, appKey, "", callback, types);
    }


    /**
     * mediation SDK init method
     *
     * @param activity required param
     * @param appKey   required param: current app's identifier
     * @param callback the callback
     * @param channel the Channel of App Store
     * @param types    optional param: ad types to be preloaded; null means preload all
     */
    public static void init(Activity activity, String appKey, String channel, InitCallback callback, AD_TYPE... types) {
        NmManager.getInstance().init(activity, appKey, channel, callback, types);
    }

    /**
     * Sets an activity that is resumed
     *
     * @param activity current resume activity
     */
    public static void onResume(Activity activity) {
        NmManager.getInstance().onResume(activity);
    }

    /**
     * Sets an activity that is on pause
     *
     * @param activity currently paused activity
     */
    public static void onPause(Activity activity) {
        NmManager.getInstance().onPause(activity);
    }

    /**
     * Returns SDK init status
     *
     * @return true : init OK; or false: init wrong
     */
    public static boolean isInit() {
        return NmManager.getInstance().isInit();
    }

    /**
     * Sets In-App-Purchase
     *
     * @param iapCount the IAP count
     * @param currency the IAP currency unit
     */
    public static void setIAP(float iapCount, String currency) {
        NmManager.getInstance().setIAP(iapCount, currency);
    }

    /**
     * set customId
     *
     * @param customId value
     */
    public static void setCustomId(String customId) {
        GlobalVariable.CUSTOM_ID = customId;
    }

    /**
     * Returns the SDk version
     *
     * @return the sdk version
     */
    public static String getSDKVersion() {
        return NmManager.getInstance().getSDKVersion();
    }

    /**
     * SDK supported preloadable Ad types
     */
    public enum AD_TYPE {
        /**
         * The Rewarded video.
         */
        /*Ad type Rewarded_Video*/
        REWARDED_VIDEO("rewardedVideo"),
        /**
         * The Interstitial.
         */
        /*Ad type Interstitial*/
        INTERSTITIAL("interstitial");

        private String mValue;

        AD_TYPE(String value) {
            this.mValue = value;
        }

        @Override
        public String toString() {
            return this.mValue;
        }

    }
}
