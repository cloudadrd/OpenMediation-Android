package com.nbmediation.sdk.mobileads;

import android.app.Activity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
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
import com.nbmediation.sdk.utils.constant.KeyConstants;

import java.util.Map;

public class Plugin1Native extends CustomNativeEvent {

    private AdvanceNative mNative;
    private Activity activity;
    private static String TAG = "OM-AG-Native:";

    @Override
    public void loadAd(final Activity activity, Map<String, String> config) {
        super.loadAd(activity, config);
        if (activity == null || activity.isFinishing()) {
            return;
        }
        if (!check(activity, config)) {
            return;
        }
        String appKey = config.get("AppKey");
        String instanceKey = config.get("InstanceKey");
        if (null == appKey) {
            return;
        }
        if (null == instanceKey) {
            return;
        }
        this.activity = activity;
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
        if (isDestroyed || activity == null) {
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
                return;
            }
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
            String msg = null;
            if (agNative != null) {
                msg = agNative.getErrorsMsg();
            }
            onInsError(msg);
        }

        @Override
        public void onAdClicked(AGNative agNative) {
            onInsClicked();
        }
    }

    @Override
    public void destroy(Activity activity) {
        this.activity = null;
        isDestroyed = true;
    }

    private void renderAdUi(final NativeAdView adView) {
        if (mNative == null) {
            onInsError(TAG + " renderAdUi fail..");
            return;
        }
        ImageView imageView = null;
        if (adView.getMediaView() != null) {
            adView.getMediaView().removeAllViews();
            imageView = new ImageView(adView.getContext());
            adView.getMediaView().addView(imageView);

            DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
            int viewWidth = displayMetrics.widthPixels;
            float scale = 1.0f / 1.9f;
            imageView.getLayoutParams().width = RelativeLayout.LayoutParams.MATCH_PARENT;
            imageView.getLayoutParams().height = (int) (viewWidth * scale);

        }
        ImageView iconImageView = null;
        if (adView.getAdIconView() != null) {
            adView.getAdIconView().removeAllViews();
            iconImageView = new ImageView(adView.getContext());
            adView.getAdIconView().addView(iconImageView);
            iconImageView.getLayoutParams().width = RelativeLayout.LayoutParams.MATCH_PARENT;
            iconImageView.getLayoutParams().height = RelativeLayout.LayoutParams.MATCH_PARENT;
        }

        Glide.with(activity).load(mNative.getIconUrl()).into(iconImageView);
        Glide.with(activity).load(mNative.getImageUrl()).into(imageView);
    }
}
