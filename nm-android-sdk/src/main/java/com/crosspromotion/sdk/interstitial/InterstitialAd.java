// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.crosspromotion.sdk.interstitial;

import com.crosspromotion.sdk.core.NmAdNetworkManager;

import java.util.Map;

public final class InterstitialAd {

    /**
     * Returns default placement availability
     *
     * @return true or false
     */
    public static boolean isReady(String placementId) {
        return NmAdNetworkManager.getInstance().isInterstitialAdReady(placementId);
    }

    /**
     * Load ad.
     */
    public static void loadAdWithPayload(String placementId, String payload, Map extras) {
        NmAdNetworkManager.getInstance().loadInterstitialAd(placementId, payload, extras);
    }

    /**
     * shows ad with default placement and default scene
     */
    public static void showAd(String placementId) {
        NmAdNetworkManager.getInstance().showInterstitialAd(placementId);
    }

    /**
     * Set the {@link InterstitialAdListener} to default placement that will receive events from the
     * rewarded video system. Set this to null to stop receiving event callbacks.
     *
     * @param listener the listener
     */
    public static void setAdListener(String placementId, InterstitialAdListener listener) {
        NmAdNetworkManager.getInstance().setInterstitialAdListener(placementId, listener);
    }
}
