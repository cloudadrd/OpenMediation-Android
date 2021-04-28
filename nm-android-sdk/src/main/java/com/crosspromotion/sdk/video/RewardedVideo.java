package com.crosspromotion.sdk.video;

import com.crosspromotion.sdk.core.NmAdNetworkManager;

import java.util.Map;

public final class RewardedVideo {
    /**
     * Returns default placement availability
     *
     * @return true or false
     */
    public static boolean isReady(String placementId) {
        return NmAdNetworkManager.getInstance().isRewardedVideoReady(placementId);
    }

    /**
     * Loads ads with default placement
     */
    public static void loadAdWithPayload(String placementId, String payload, Map extras) {
        NmAdNetworkManager.getInstance().loadRewardedVideo(placementId, payload, extras);
    }

    /**
     * showAd ads with default placement and default scene
     */
    public static void showAd(String placementId) {
        NmAdNetworkManager.getInstance().showRewardedVideo(placementId);
    }

    public static void setAdListener(String placementId, RewardedVideoListener listener) {
        NmAdNetworkManager.getInstance().setRewardedVideoListener(placementId, listener);
    }
}
