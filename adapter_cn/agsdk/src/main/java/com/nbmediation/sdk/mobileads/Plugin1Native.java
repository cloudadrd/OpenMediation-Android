package com.nbmediation.sdk.mobileads;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.adsgreat.base.callback.EmptyAdEventListener;
import com.adsgreat.base.core.AGNative;
import com.adsgreat.base.core.AdsgreatSDK;
import com.adsgreat.base.vo.AdsVO;
import com.adsgreat.base.vo.AdsNativeVO;
import com.bumptech.glide.Glide;
import com.nbmediation.sdk.mediation.CustomNativeEvent;
import com.nbmediation.sdk.mediation.MediationInfo;
import com.nbmediation.sdk.nativead.AdIconView;
import com.nbmediation.sdk.nativead.MediaView;
import com.nbmediation.sdk.nativead.NativeAdView;
import com.adsgreat.base.core.AdvanceNative;
import java.util.Map;

public class Plugin1Native extends CustomNativeEvent {
    private AdvanceNative mNative;
    private MediaView mMediaView;
    private AdIconView mAdIconView;
    private NativeAdView adView;
    private Activity activity;
    private String nvIconUrl = null;
    private String nvImageUrl = null;
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
        if (null == appKey || !(appKey instanceof String)) {
            return;
        }
        if (null == instanceKey || !(instanceKey instanceof String)) {
            return;
        }
        this.activity = activity;
        AdsgreatSDK.initialize(activity, (String) appKey);
        loadNativeAd(activity,instanceKey, config);
    }

    public void registerNativeView(NativeAdView adView) {
        if (isDestroyed || activity == null) {
            return;
        }
        this.adView = adView;
        renderAdUi(adView);
//        // 所有广告类型，注册mDownloadButton的点击事件
//        ViewGroup viewGroup=new LinearLayout(activity);
//       // viewGroup.addView(adView.getAdIconView());
//        //viewGroup.addView(adView.getCallToActionView());
//        viewGroup.addView(adView.getDescView());
//        viewGroup.addView(adView.getMediaView());
//        viewGroup.addView(adView.getTitleView());
//        mNative.registeADClickArea(viewGroup);
    }

    @Override
    public int getMediation() {
        return MediationInfo.MEDIATION_ID_32;
    }


    private void loadNativeAd(Activity activity, String codeId, Map<String, String> config) {
        AdsgreatSDK.getNativeAd(codeId, activity,new NativeListener());
    }

    public class NativeListener extends EmptyAdEventListener {
        @Override
        public void onReceiveAdVoSucceed(AdsVO var1) {
            if (var1 == null) {
                return;
            }
            AdsNativeVO an =(AdsNativeVO) var1;
            nvIconUrl = an.iconUrl;
            nvImageUrl = an.imageUrl;
            com.nbmediation.sdk.nativead.AdInfo info = new com.nbmediation.sdk.nativead.AdInfo();
            info.setCallToActionText(an.buttonStr);
            info.setDesc(an.desc);
//            info.setStarRating(Double.parseDouble(an.rate));
            info.setTitle(an.title);
            info.setType(an.offerType);
            onInsReady(info);
        }

        @Override
        public void onReceiveAdFailed(AGNative agNative) {
            onInsError(agNative.getErrorsMsg());
        }

        @Override
        public void onAdClicked(AGNative agNative) {
            onInsClicked();
        }
    }

    @Override
    public void destroy(Activity activity) {
        isDestroyed = true;
    }

    private void renderAdUi(final NativeAdView adView) {
//        mAQuery = new AQuery(adView);
        ImageView imageView = null;
        if (adView.getMediaView() != null) {
            adView.getMediaView().removeAllViews();
            imageView = new ImageView(adView.getContext());
            adView.getMediaView().addView(imageView);

            DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
            int viewWidth = displayMetrics.widthPixels;
            float scale = 1.0f/1.9f;
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

        Glide.with(activity).load(nvIconUrl).into(iconImageView);
        Glide.with(activity).load(nvImageUrl).into(imageView);
    }
}
