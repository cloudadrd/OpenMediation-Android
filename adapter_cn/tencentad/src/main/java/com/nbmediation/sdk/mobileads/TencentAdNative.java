package com.nbmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.nbmediation.sdk.mediation.CustomNativeEvent;
import com.nbmediation.sdk.mediation.MediationInfo;
import com.nbmediation.sdk.nativead.NativeAdView;
import com.nbmediation.sdk.utils.AdLog;
import com.qq.e.ads.nativ.ADSize;
import com.qq.e.ads.nativ.NativeExpressADView;
import com.qq.e.ads.nativ.NativeExpressMediaListener;
import com.qq.e.ads.nativ.express2.AdEventListener;
import com.qq.e.ads.nativ.express2.MediaEventListener;
import com.qq.e.ads.nativ.express2.NativeExpressAD2;
import com.qq.e.ads.nativ.express2.NativeExpressADData2;
import com.qq.e.ads.nativ.express2.VideoOption2;
import com.qq.e.comm.managers.GDTADManager;
import com.qq.e.comm.pi.AdData;
import com.qq.e.comm.util.AdError;

import java.util.List;
import java.util.Map;

import static android.widget.ListPopupWindow.MATCH_PARENT;


public class TencentAdNative extends CustomNativeEvent implements NativeExpressAD2.AdLoadListener {

    private static String TAG = "OM-TencentAd: ";

    private NativeExpressAD2 nativeExpressAD;

    private NativeExpressADData2 mNativeExpressADData2;

    @Override
    public void loadAd(final Activity activity, Map<String, String> config) {
        super.loadAd(activity, config);
        if (!check(activity, config)) {
            return;
        }
        String appKey = config.get("AppKey");
        if (!GDTADManager.getInstance().isInitialized()) {
            init(activity, appKey);
        }

        int[] size = getNativeSize(config);
        loadAd(activity, size[0], size[1]);
    }

    // 加载广告，设置条件
    private void loadAd(Context context, int width, int height) {
        nativeExpressAD = new NativeExpressAD2(context, mInstancesKey, this); // 传入Activity
        nativeExpressAD.setAdSize(width, height); // 单位dp

        // 如果您在平台上新建原生模板广告位时，选择了支持视频，那么可以进行个性化设置（可选）
        VideoOption2.Builder builder = new VideoOption2.Builder();

        /**
         * 如果广告位支持视频广告，强烈建议在调用loadData请求广告前设置setAutoPlayPolicy，有助于提高视频广告的eCPM值 <br/>
         * 如果广告位仅支持图文广告，则无需调用
         */
        builder.setAutoPlayPolicy(VideoOption2.AutoPlayPolicy.WIFI) // WIFI 环境下可以自动播放视频
                .setAutoPlayMuted(true) // 自动播放时为静音
                .setDetailPageMuted(false)  // 视频详情页播放时不静音
                .setMaxVideoDuration(0) // 设置返回视频广告的最大视频时长（闭区间，可单独设置），单位:秒，默认为 0 代表无限制，合法输入为：5<=maxVideoDuration<=60. 此设置会影响广告填充，请谨慎设置
                .setMinVideoDuration(0); // 设置返回视频广告的最小视频时长（闭区间，可单独设置），单位:秒，默认为 0 代表无限制， 此设置会影响广告填充，请谨慎设置
        nativeExpressAD.setVideoOption2(builder.build());
        nativeExpressAD.loadAd(1);
        AdLog.getSingleton().LogD(TAG + "ad load mInstancesKey=" + mInstancesKey);
        destroyAd();
    }

    /**
     * 释放前一个 NativeExpressADData2 的资源
     */
    private void destroyAd() {
        if (mNativeExpressADData2 != null) {
            Log.d(TAG, "destroyAD");
            mNativeExpressADData2.destroy();
        }
        nativeExpressAD = null;
    }

    private void init(Activity activity, String appKey) {
        GDTADManager.getInstance().initWith(activity.getApplicationContext(), appKey);
    }


    @Override
    public int getMediation() {
        return MediationInfo.MEDIATION_ID_6;
    }

    @Override
    public void destroy(Activity activity) {
        if (mNativeExpressADData2 != null) {
            Log.d(TAG, "destroyAD");
            mNativeExpressADData2.destroy();
        }
        nativeExpressAD = null;
    }


    @Override
    public void onNoAD(AdError adError) {
        AdLog.getSingleton().LogD(TAG + "Native ad load failed: code " + adError.getErrorCode() + " " + adError.getErrorMsg());
        onInsError(adError.getErrorMsg());
    }

    private void insReady() {
        mAdInfo.setDesc("");
        mAdInfo.setType(2);
        mAdInfo.setCallToActionText("");
        mAdInfo.setTitle("");
        mAdInfo.setTemplate(true);
        onInsReady(mAdInfo);
    }


    @Override
    public void registerNativeView(NativeAdView nativeAdView) {
        if (mNativeExpressADData2 == null || mNativeExpressADData2.getAdView() == null) {
            return;
        }
        if (nativeAdView.getMediaView() != null) {
            nativeAdView.getMediaView().removeAllViews();
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, MATCH_PARENT);
            nativeAdView.getMediaView().addView(mNativeExpressADData2.getAdView(), lp);
        }
    }

    @Override
    public void onLoadSuccess(List<NativeExpressADData2> adDataList) {
        Log.i(TAG, "onLoadSuccess: size " + adDataList.size());
        // 渲染广告
        renderAd(adDataList);
    }

    /**
     * 渲染广告
     *
     * @param adDataList
     */
    private void renderAd(List<NativeExpressADData2> adDataList) {
        if (adDataList == null || adDataList.size() == 0) {
            return;
        }
        mNativeExpressADData2 = adDataList.get(0);
        Log.i(TAG, "renderAd: " + "  eCPM level = " +
                mNativeExpressADData2.getECPMLevel() + "  Video duration: " + mNativeExpressADData2.getVideoDuration());
        mNativeExpressADData2.setAdEventListener(new AdEventListener() {
            @Override
            public void onClick() {

                if (isDestroyed) {
                    AdLog.getSingleton().LogD(TAG, "onClick: is destroyed");
                    return;
                }
                AdLog.getSingleton().LogD(TAG, "onClick: " + mNativeExpressADData2);
                onInsClicked();
            }

            @Override
            public void onExposed() {
                Log.i(TAG, "onExposed: " + mNativeExpressADData2);
            }

            @Override
            public void onRenderSuccess() {
                if (isDestroyed) {
                    return;
                }
                AdLog.getSingleton().LogD(TAG + "onRenderSuccess");
                insReady();
            }

            @Override
            public void onRenderFail() {
                AdLog.getSingleton().LogD(TAG, "onRenderFail: " + mNativeExpressADData2);
                onInsError("onRenderFail");
            }

            @Override
            public void onAdClosed() {
                mNativeExpressADData2.destroy();
            }
        });

        mNativeExpressADData2.setMediaListener(new MediaEventListener() {
            @Override
            public void onVideoCache() {
                AdLog.getSingleton().LogD(TAG, "onVideoCache: " + mNativeExpressADData2);
            }

            @Override
            public void onVideoStart() {
                AdLog.getSingleton().LogD(TAG, "onVideoStart: " + mNativeExpressADData2);
            }

            @Override
            public void onVideoResume() {
                AdLog.getSingleton().LogD(TAG, "onVideoResume: " + mNativeExpressADData2);
            }

            @Override
            public void onVideoPause() {
                AdLog.getSingleton().LogD(TAG, "onVideoPause: " + mNativeExpressADData2);
            }

            @Override
            public void onVideoComplete() {
                AdLog.getSingleton().LogD(TAG, "onVideoComplete: " + mNativeExpressADData2);
            }

            @Override
            public void onVideoError() {
                AdLog.getSingleton().LogD(TAG, "onVideoError: " + mNativeExpressADData2);
            }
        });

        mNativeExpressADData2.render();
    }
}
