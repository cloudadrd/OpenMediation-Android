package com.nbmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.adsgreat.base.callback.EmptyAdEventListener;
import com.adsgreat.base.core.AGNative;
import com.adsgreat.base.core.AdsgreatSDK;
import com.adsgreat.base.core.AdsgreatSDKInternal;
import com.adsgreat.base.core.AdvanceNative;
import com.bumptech.glide.Glide;
import com.nbmediation.sdk.mediation.CustomNativeEvent;
import com.nbmediation.sdk.mediation.MediationInfo;
import com.nbmediation.sdk.nativead.AdInfo;
import com.nbmediation.sdk.nativead.NativeAdView;
import com.nbmediation.sdk.utils.AdLog;
import com.nbmediation.sdk.utils.constant.KeyConstants;

import java.lang.ref.WeakReference;
import java.util.Map;

public class Plugin1Native extends CustomNativeEvent {

    private AdvanceNative mNative;
    private Context context;
    private final static String TAG = "OM-AG-Native:";

    @Override
    public void loadAd(final Activity activity, Map<String, String> config) {
        super.loadAd(activity, config);
        if (activity == null || activity.isFinishing()) {
            AdLog.getSingleton().LogD(TAG + "activity null");
            return;
        }
        if (!check(activity, config)) {
            AdLog.getSingleton().LogD(TAG + "check(activity, config) error.");
            return;
        }
        String appKey = config.get("AppKey");
        String instanceKey = config.get("InstanceKey");
        if (null == appKey) {
            AdLog.getSingleton().LogD(TAG + "appKey null.");
            return;
        }
        if (null == instanceKey) {
            AdLog.getSingleton().LogD(TAG + "instanceKey null.");
            return;
        }
        this.context = activity.getApplicationContext();
        String customIdObj = config.get(KeyConstants.CUSTOM_ID_KEY);
        if (customIdObj != null) {
            if (!TextUtils.isEmpty(customIdObj)) {
                AdsgreatSDKInternal.setUserId(customIdObj);
            }
        }
        AdsgreatSDK.initialize(activity, appKey);
        loadNativeAd(activity, instanceKey, config);
    }

    public void registerNativeView(NativeAdView adView) {
        if (isDestroyed || context == null) {
            AdLog.getSingleton().LogD(TAG + "registerNativeView  context null or isDestroyed.");
            return;
        }

        try {
            renderAdUi(adView);
            mNative.registeADClickArea((View) adView.getMediaView().getParent());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getMediation() {
        return MediationInfo.MEDIATION_ID_32;
    }


    private void loadNativeAd(Activity activity, String codeId, Map<String, String> config) {
        AdsgreatSDK.getNativeAd(codeId, activity, new NativeListener());
    }

    public class NativeListener extends EmptyAdEventListener {

        @Override
        public void onReceiveAdSucceed(AGNative agNative) {
            if (!(agNative instanceof AdvanceNative)) {
                AdLog.getSingleton().LogD(TAG + "onReceiveAdSucceed agNative error.");
                return;
            }
            AdLog.getSingleton().LogD(TAG + "onReceiveAdSucceed.");

            mNative = (AdvanceNative) agNative;
            AdInfo info = new AdInfo();
            info.setCallToActionText(mNative.getButtonStr());
            info.setDesc(mNative.getDesc());
            info.setTitle(mNative.getTitle());
            info.setType(mNative.getOfferType());
            mAdInfo.setAdNetWorkId(MediationInfo.MEDIATION_ID_32);

            onInsReady(info);
        }

        @Override
        public void onReceiveAdFailed(AGNative agNative) {
            AdLog.getSingleton().LogD(TAG + "onReceiveAdFailed.");
            String msg = null;
            if (agNative != null) {
                msg = agNative.getErrorsMsg();
            }
            onInsError(msg);
        }

        @Override
        public void onAdClicked(AGNative agNative) {

            AdLog.getSingleton().LogD(TAG + "onAdClicked.");
            onInsClicked();
        }
    }


    private void renderAdUi(final NativeAdView adView) {
        AdLog.getSingleton().LogD(TAG + "renderAdUi in...");
        if (mNative == null) {
            AdLog.getSingleton().LogD(TAG + "renderAdUi fail...");
            onInsError(TAG + " renderAdUi fail..");
            return;
        }
        ImageView imageView;
        if (adView.getMediaView() != null) {
            adView.getMediaView().removeAllViews();
            imageView = new ImageView(adView.getContext());
            adView.getMediaView().addView(imageView);

            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            int viewWidth = displayMetrics.widthPixels;
            float scale = 1.0f / 1.9f;
            imageView.getLayoutParams().width = RelativeLayout.LayoutParams.MATCH_PARENT;
            imageView.getLayoutParams().height = (int) (viewWidth * scale);
            Glide.with(context).load(mNative.getImageUrl()).into(imageView);

        }
        ImageView iconImageView;
        if (adView.getAdIconView() != null) {
            adView.getAdIconView().removeAllViews();
            iconImageView = new ImageView(adView.getContext());
            adView.getAdIconView().addView(iconImageView);
            iconImageView.getLayoutParams().width = RelativeLayout.LayoutParams.MATCH_PARENT;
            iconImageView.getLayoutParams().height = RelativeLayout.LayoutParams.MATCH_PARENT;
            Glide.with(context).load(mNative.getIconUrl()).into(iconImageView);
        }


    }

    @Override
    public void destroy(Activity activity) {
        AdLog.getSingleton().LogD(TAG + "destroy");
        isDestroyed = true;
        if (mNative != null && mNative.getParent() instanceof ViewGroup) {
            ((ViewGroup) mNative.getParent()).removeView(mNative);
        }
        mNative = null;
        context = null;
    }
}
