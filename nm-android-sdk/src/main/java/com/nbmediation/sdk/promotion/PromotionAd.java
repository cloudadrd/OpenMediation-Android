// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.promotion;

import android.app.Activity;

import com.nbmediation.sdk.core.BaseNmAds;
import com.nbmediation.sdk.core.NmManager;
import com.nbmediation.sdk.utils.constant.CommonConstants;
import com.nbmediation.sdk.utils.model.Scene;

/**
 * AdTiming PromotionAd API
 */
public class PromotionAd extends BaseNmAds {

    /**
     * Returns default placement's availability
     *
     * @return true or false
     */
    public static boolean isReady() {
        return NmManager.getInstance().isPromotionAdReady("");
    }

    /**
     * Returns specific scene's cap status
     *
     * @return true or false
     */
    public static boolean isSceneCapped(String scene) {
        return isSceneCapped(CommonConstants.PROMOTION, scene);
    }

    /**
     * Returns specific scene's info
     *
     * @return {@link Scene}
     */
    public static Scene getSceneInfo(String scene) {
        return getSceneInfo(CommonConstants.PROMOTION, scene);
    }

    /**
     * Loads ad for default Placement
     */
    public static void loadAd() {
        NmManager.getInstance().loadPromotionAd("");
    }

    /**
     * shows ad with default placement and default scene
     */
    public static void showAd(Activity activity, PromotionAdRect rect) {
        showAd(activity, rect, "");
    }

    /**
     * shows ad with default placement and specific scene
     *
     * @param scene optional param ,if null, shows default scene
     */
    public static void showAd(Activity activity, PromotionAdRect rect, String scene) {
        NmManager.getInstance().showPromotionAd(activity, "", rect, scene);
    }

    /**
     * hide ad with default placement and specific scene
     */
    public static void hideAd() {
        NmManager.getInstance().hidePromotionAd("");
    }

    /**
     * Set the {@link PromotionAdListener} to default placement that will receive events from the
     * rewarded video system. Set this to null to stop receiving event callbacks.
     */
    public static void setAdListener(PromotionAdListener listener) {
        NmManager.getInstance().setPromotionAdListener("", listener);
    }

    public static void addAdListener(PromotionAdListener listener) {
        NmManager.getInstance().addPromotionAdListener("", listener);
    }

    public static void removeAdListener(PromotionAdListener listener) {
        NmManager.getInstance().removePromotionAdListener("", listener);
    }
}
