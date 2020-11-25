// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.mobileads;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.nbmediation.sdk.mediation.CustomNativeEvent;
import com.nbmediation.sdk.mediation.MediationInfo;
import com.nbmediation.sdk.nativead.AdIconView;
import com.nbmediation.sdk.nativead.MediaView;
import com.nbmediation.sdk.nativead.NativeAdView;
import com.nbmediation.sdk.utils.AdLog;
import com.smaato.sdk.nativead.NativeAd;
import com.smaato.sdk.nativead.NativeAdAssets;
import com.smaato.sdk.nativead.NativeAdError;
import com.smaato.sdk.nativead.NativeAdRenderer;
import com.smaato.sdk.nativead.NativeAdRequest;

import java.util.Map;

public class SmaatoNative extends CustomNativeEvent {

    private static String TAG = "OM-Smaato-native: ";
    private NativeAdRequest request;
    private MediaView mMediaView;
    private AdIconView mAdIconView;
    private NativeAdRenderer mRenderer;

    @Override
    public void loadAd(Activity activity, Map<String, String> config) {
        super.loadAd(activity, config);

        if (!check(activity, config)) {
            return;
        }

        if (request != null) {
            NativeAd.loadAd(com.smaato.sdk.sys.Lifecycling.of(activity), request, listener);
            return;
        }
        SmaatoHelper.init(activity.getApplication(), config.get("AppKey"));
        request = NativeAdRequest.builder().adSpaceId(mInstancesKey)
                .shouldReturnUrlsForImageAssets(false).build();
        NativeAd.loadAd(com.smaato.sdk.sys.Lifecycling.of(activity), request, listener);

    }

    private NativeAd.Listener listener = new NativeAd.Listener() {

        @Override
        public void onAdLoaded(@NonNull NativeAd nativeAd, @NonNull NativeAdRenderer renderer) {
            if (!isDestroyed) {
                mRenderer = renderer;
                NativeAdAssets assets = renderer.getAssets();
                mAdInfo.setType(1);
                mAdInfo.setTitle(assets.title());
                mAdInfo.setDesc(assets.text());
                mAdInfo.setCallToActionText(assets.cta());
                mAdInfo.setAdNetWorkId(MediationInfo.MEDIATION_ID_55);
                onInsReady(mAdInfo);
            }
            AdLog.getSingleton().LogD(TAG + "onAdLoaded isDestroyed=" + isDestroyed);
        }

        @Override
        public void onAdFailedToLoad(@NonNull NativeAd nativeAd, @NonNull NativeAdError nativeAdError) {
            AdLog.getSingleton().LogE(TAG + "Native ad failed to load, error code is : " + nativeAdError.toString());
            if (!isDestroyed) {
                onInsError(TAG + "onAdFailedToLoad:" + nativeAdError);
            }
        }

        @Override
        public void onAdImpressed(@NonNull NativeAd nativeAd) {
            AdLog.getSingleton().LogD(TAG + "onAdImpressed isDestroyed=" + isDestroyed);
        }

        @Override
        public void onAdClicked(@NonNull NativeAd nativeAd) {
            if (!isDestroyed) {
                onInsClicked();
            }
            AdLog.getSingleton().LogD(TAG + "onAdClicked isDestroyed=" + isDestroyed);
        }

        @Override
        public void onTtlExpired(@NonNull NativeAd nativeAd) {
            AdLog.getSingleton().LogD(TAG + "onTtlExpired isDestroyed=" + isDestroyed);

        }
    };

    @Override
    public void registerNativeView(NativeAdView adView) {
        if (isDestroyed) {
            return;
        }

        RelativeLayout relativeLayout = new RelativeLayout(adView.getContext());
        if (mRenderer == null) {
            return;
        }

        if (adView.getMediaView() != null) {
            mMediaView = adView.getMediaView();
            adView.setMediaView(mMediaView);
        }

        if (adView.getAdIconView() != null) {
            mAdIconView = adView.getAdIconView();
            adView.setAdIconView(mAdIconView);
        }
        mRenderer.registerForImpression(adView);
        View[] clicks = new View[]{adView.getAdIconView(), adView.getMediaView(), adView.getTitleView(), adView.getDescView(),
                adView.getCallToActionView()};
        mRenderer.registerForClicks(clicks);

        NativeAdAssets assets = mRenderer.getAssets();

        if (adView.getMediaView() != null && assets.images().size() > 0) {
            adView.getMediaView().removeAllViews();
            ImageView imageView = new ImageView(adView.getContext());
            NativeAdAssets.Image image = assets.images().get(0);
            imageView.setImageDrawable(image.drawable());

            adView.getMediaView().addView(imageView, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
//            DisplayMetrics displayMetrics = adView.getContext().getResources().getDisplayMetrics();
//            int viewWidth = displayMetrics.widthPixels;
//
//
//            imageView.getLayoutParams().width = image.width();
//            imageView.getLayoutParams().height = image.height();
//            Glide.with(adView.getContext()).load(assets.images().get(0).uri()).into(imageView);
        }

        ImageView iconImageView = null;
        if (adView.getAdIconView() != null && assets.icon() != null) {
            adView.getAdIconView().removeAllViews();
            iconImageView = new ImageView(adView.getContext());
            iconImageView.setImageDrawable(assets.icon().drawable());
            adView.getAdIconView().addView(iconImageView, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
//            iconImageView.getLayoutParams().width = RelativeLayout.LayoutParams.MATCH_PARENT;
//            iconImageView.getLayoutParams().height = RelativeLayout.LayoutParams.MATCH_PARENT;
//            if (assets.icon() != null)
//                Glide.with(adView.getContext()).load(assets.icon().uri()).into(iconImageView);
        }

        adView.addView(relativeLayout);
    }

    @Override
    public void destroy(Activity activity) {
        if (mRenderer != null) {
            mRenderer = null;
        }
        isDestroyed = true;
        mMediaView = null;
        mAdIconView = null;
    }

    @Override
    public int getMediation() {
        return MediationInfo.MEDIATION_ID_56;
    }
}
