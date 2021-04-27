// Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk;

import com.nbmediation.sdk.utils.AdLog;
import com.nbmediation.sdk.utils.Preconditions;
import com.nbmediation.sdk.utils.cache.DataCache;
import com.nbmediation.sdk.utils.cache.LifetimeRevenueData;
import com.nbmediation.sdk.utils.constant.KeyConstants;
import com.nbmediation.sdk.utils.error.Error;
import com.nbmediation.sdk.utils.error.ErrorBuilder;
import com.nbmediation.sdk.utils.error.ErrorCode;
import com.nbmediation.sdk.utils.model.BaseInstance;
import com.nbmediation.sdk.utils.model.Configurations;
import com.nbmediation.sdk.utils.model.Scene;

import java.util.HashSet;
import java.util.Set;

/**
 * Single source of impression level revenue data. Implement interface {@link ImpressionDataListener}
 * and subscribe to events from ImpressionManager to receive detailed impression data.
 * This class is not linked to the activity lifecycle. It is recommended to subscribe to events
 * when application starts even before the first activity created.
 */
public class ImpressionManager {
    private static final HashSet<ImpressionDataListener> mListeners = new HashSet<>();

    /**
     * Call this method to start listening for impression level revenue data events.
     *
     * @param listener - {@link ImpressionDataListener} interface implementation
     */
    public static void addListener(final ImpressionDataListener listener) {
        Preconditions.checkNotNull(listener, true);

        synchronized (ImpressionManager.class) {
            mListeners.add(listener);
        }
    }

    /**
     * Call this method to unsubscribe from impression level revenue data events.
     *
     * @param listener - previously submitted to addListener() {@link ImpressionDataListener}
     */
    public static void removeListener(final ImpressionDataListener listener) {
        Preconditions.checkNotNull(listener, true);

        synchronized (ImpressionManager.class) {
            mListeners.remove(listener);
        }
    }

    /**
     * SDK internal method. Should not be used by publishers.
     */
    public static void onInsShowSuccess(final BaseInstance instance, final Scene scene) {
        if (instance == null) {
            AdLog.getSingleton().LogD("Impressions reportImpressionDataToPublisher error: instance is null");
            return;
        }

        // add Revenue
        LifetimeRevenueData.addRevenue(instance.getShowRevenue());

        // sendImpressionDataToPublisher
        if (!mListeners.isEmpty()) {
            send(instance, scene);
        }
    }

    private static void send(final BaseInstance instance, final Scene scene) {
        Configurations config = DataCache.getInstance().getFromMem(KeyConstants.KEY_CONFIGURATION, Configurations.class);
        if (config == null) {
            return;
        }
        Error error = null;
        ImpressionData data = null;
        if (config.auctionEnabled()) {
            data = ImpressionData.create(instance, scene);
        } else {
            error = ErrorBuilder.build(ErrorCode.CODE_SHOW_IMPRESSION_NOT_ENABLED, ErrorCode.MSG_SHOW_IMPRESSION_NOT_ENABLED, -1);
        }
        Set<ImpressionDataListener> listenerSet = cloneListeners();
        for (ImpressionDataListener listener : listenerSet) {
            listener.onImpression(error, data);
        }
    }

    private static Set<ImpressionDataListener> cloneListeners() {
        synchronized (ImpressionManager.class) {
            return new HashSet<>(mListeners);
        }
    }

    public static void clear() {
        synchronized (ImpressionManager.class) {
            mListeners.clear();
        }
    }
}
