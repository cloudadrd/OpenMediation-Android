package com.nbmediation.sdk.core.imp.splash;

/**
 * Created by jiantao.tu on 2020/10/26.
 */
import android.app.Activity;
import android.text.TextUtils;
import android.view.ViewGroup;


import com.nbmediation.sdk.core.NmManager;
import com.nbmediation.sdk.splash.SplashAdListener;
import com.nbmediation.sdk.utils.ActLifecycle;
import com.nbmediation.sdk.utils.AdsUtil;
import com.nbmediation.sdk.utils.PlacementUtils;
import com.nbmediation.sdk.utils.constant.CommonConstants;
import com.nbmediation.sdk.utils.event.EventId;
import com.nbmediation.sdk.utils.model.Placement;

import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SplashAdManager {

    private ConcurrentMap<String, SplashAdImp> mSplashAds;
    public WeakReference<ViewGroup> mViewGroup;
    public void setSize(int width, int height) {
        setSize("", width, height);
    }

    public void setSize(String placementId, int width, int height) {
        SplashAdImp splashAdImp = getSplashAd(placementId);
        if (splashAdImp == null) {
            return;
        }
        splashAdImp.setSize(width, height);
    }

    private static final class SplashHolder {
        private static final SplashAdManager INSTANCE = new SplashAdManager();
    }

    private SplashAdManager() {
        mSplashAds = new ConcurrentHashMap<>();
    }

    public static SplashAdManager getInstance() {
        return SplashHolder.INSTANCE;
    }

    public void initSplashAd(String placementId) {
        if (mSplashAds != null && !mSplashAds.containsKey(placementId)) {
            Activity activity = ActLifecycle.getInstance().getActivity();
            if (activity == null) {
                return;
            }
            SplashAdImp adImp = new SplashAdImp(activity, placementId);
            mSplashAds.put(placementId, adImp);
        }
    }

    public boolean isEmpty(){
        if(mSplashAds == null)
            return true;
        return mSplashAds.size() == 0;
    }

    public void setLoadTimeout(long timeout) {
        setLoadTimeout("", timeout);
    }

    public void setLoadTimeout(String placementId, long timeout) {
        SplashAdImp splashAdImp = getSplashAd(placementId);
        if (splashAdImp == null) {
            return;
        }
        splashAdImp.setLoadTimeout(timeout);
    }

    public void load() {
        this.load("");
    }

    public void load(String placementId) {
        SplashAdImp splashAdImp = getSplashAd(placementId);
        if (splashAdImp == null) {
            return;
        }
        splashAdImp.setViewGroup(mViewGroup);
        splashAdImp.loadAd(NmManager.LOAD_TYPE.MANUAL);
    }

    public boolean isReady() {
        return isReady("");
    }

    public boolean isReady(String placementId) {
        SplashAdImp splashAdImp = getSplashAd(placementId);
        if (splashAdImp == null) {
            AdsUtil.callActionReport(EventId.CALLED_IS_READY_FALSE, placementId, null, CommonConstants.SPLASH);
            return false;
        }
        boolean result = splashAdImp.isReady();
        if (result) {
            AdsUtil.callActionReport(EventId.CALLED_IS_READY_TRUE, placementId, null, CommonConstants.SPLASH);
        } else {
            AdsUtil.callActionReport(EventId.CALLED_IS_READY_FALSE, placementId, null, CommonConstants.SPLASH);
        }
        return result;
    }

    public void setSplashAdListener(SplashAdListener listener) {
        this.setSplashAdListener("", listener);
    }

    public void setSplashAdListener(String placementId, SplashAdListener listener) {
        SplashAdImp splashAdImp = getSplashAd(placementId);
        if (splashAdImp == null) {
            return;
        }
        splashAdImp.setAdListener(listener);
    }

    public void show(ViewGroup container) {
        this.show(container, "");
    }

    public void show(ViewGroup container, String placementId) {
        AdsUtil.callActionReport(EventId.CALLED_SHOW, placementId, null, CommonConstants.SPLASH);
        SplashAdImp splashAdImp = getSplashAd(placementId);
        if (splashAdImp == null) {
            return;
        }
        splashAdImp.show(container);
    }

    private SplashAdImp getSplashAd(String placementId) {
        if (mSplashAds == null) {
            return null;
        }
        if (TextUtils.isEmpty(placementId)) {
            Placement placement = PlacementUtils.getPlacement(CommonConstants.SPLASH);
            if (placement == null) {
                return null;
            }
            placementId = placement.getId();
        }
        return mSplashAds.get(placementId);
    }

    public void setViewGroup(ViewGroup viewGroup){
        mViewGroup = new WeakReference<>(viewGroup);
    }
}