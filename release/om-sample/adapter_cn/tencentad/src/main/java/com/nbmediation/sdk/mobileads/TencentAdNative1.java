package com.nbmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.RelativeLayout;

import com.nbmediation.sdk.mediation.CustomNativeEvent;
import com.nbmediation.sdk.mediation.MediationInfo;
import com.nbmediation.sdk.nativead.NativeAdView;
import com.nbmediation.sdk.utils.AdLog;
import com.qq.e.ads.cfg.VideoOption;
import com.qq.e.ads.nativ.ADSize;
import com.qq.e.ads.nativ.NativeExpressAD;
import com.qq.e.ads.nativ.NativeExpressADView;
import com.qq.e.ads.nativ.NativeExpressMediaListener;
import com.qq.e.ads.nativ.NativeUnifiedADData;
import com.qq.e.comm.constants.AdPatternType;
import com.qq.e.comm.managers.GDTADManager;
import com.qq.e.comm.pi.AdData;
import com.qq.e.comm.util.AdError;

import java.util.List;
import java.util.Map;

import static android.widget.ListPopupWindow.MATCH_PARENT;


public class TencentAdNative1 extends CustomNativeEvent implements NativeExpressAD.NativeExpressADListener {

    private static String TAG = "OM-TencentAd: ";

    private NativeExpressAD nativeExpressAD;
    private NativeExpressADView nativeExpressADView;

    private boolean isPreloadVideo;


    private Activity activity;

    @Override
    public void loadAd(final Activity activity, Map<String, String> config) {
        super.loadAd(activity, config);
        this.activity = activity;
        if (!check(activity, config)) {
            return;
        }
        String appKey = config.get("AppKey");
        if (!GDTADManager.getInstance().isInitialized()) {
            init(activity, appKey);
        }

        int[] size = getNativeSize(config);
        refreshAd(activity, size[0], size[1]);
    }

    // 仅展示部分代码，完整代码请参考 GDTUnionDemo 工程
    // 1.加载广告，先设置加载上下文环境和条件
    private void refreshAd(Context context, int width, int height) {
        nativeExpressAD = new NativeExpressAD(context, new ADSize(width, ADSize.AUTO_HEIGHT), mInstancesKey, this); // 传入Activity
        // 注意：如果您在平台上新建原生模板广告位时，选择了支持视频，那么可以进行个性化设置（可选）
        nativeExpressAD.setVideoOption(new VideoOption.Builder()
                .setAutoPlayPolicy(VideoOption.AutoPlayPolicy.WIFI) // WIFI 环境下可以自动播放视频
                .setAutoPlayMuted(true) // 自动播放时为静音
                .build()); //

        /**
         * 如果广告位支持视频广告，强烈建议在调用loadData请求广告前调用setVideoPlayPolicy，有助于提高视频广告的eCPM值 <br/>
         * 如果广告位仅支持图文广告，则无需调用
         */

        nativeExpressAD.loadAD(1);
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
        if (nativeExpressADView != null) {
            nativeExpressADView.destroy();
        }
    }


    @Override
    public void onNoAD(AdError adError) {
        AdLog.getSingleton().LogD(TAG + "Native ad load failed: code " + adError.getErrorCode() + " " + adError.getErrorMsg());
        onInsError(adError.getErrorMsg());
    }


    @Override
    public void onADLoaded(List<NativeExpressADView> adList) {
        Log.i(TAG, "onADLoaded: " + adList.size());
        // 释放前一个展示的NativeExpressADView的资源
        if (nativeExpressADView != null) {
            nativeExpressADView.destroy();
        }

        nativeExpressADView = adList.get(0);
        Log.i(TAG, "onADLoaded, video info: " + getAdInfo(nativeExpressADView));
        if (nativeExpressADView.getBoundData().getAdPatternType() == AdPatternType.NATIVE_VIDEO) {
            nativeExpressADView.setMediaListener(mediaListener);
            // 预加载视频素材，加载成功会回调mediaListener的onVideoCached方法，失败的话回调onVideoError方法errorCode为702。
            nativeExpressADView.preloadVideo();
        } else {
            isPreloadVideo = false;
        }
        if (!isPreloadVideo) {
            // 广告可见才会产生曝光，否则将无法产生收益。
//            container.addView(nativeExpressADView);
            nativeExpressADView.render();
        }
    }

    /**
     * 获取广告数据
     *
     * @param nativeExpressADView
     * @return
     */
    private String getAdInfo(NativeExpressADView nativeExpressADView) {
        AdData adData = nativeExpressADView.getBoundData();
        if (adData != null) {
            StringBuilder infoBuilder = new StringBuilder();
            infoBuilder.append("title:").append(adData.getTitle()).append(",")
                    .append("desc:").append(adData.getDesc()).append(",")
                    .append("patternType:").append(adData.getAdPatternType());
            if (adData.getAdPatternType() == AdPatternType.NATIVE_VIDEO) {
                infoBuilder.append(", video info: ").append(getVideoInfo(adData.getProperty(AdData.VideoPlayer.class)));
            }
            Log.d(TAG, "eCPMLevel = " + adData.getECPMLevel() + " , " +
                    "videoDuration = " + adData.getVideoDuration());
            return infoBuilder.toString();
        }
        return null;
    }

    @Override
    public void onRenderFail(NativeExpressADView nativeExpressADView) {
        AdLog.getSingleton().LogD(TAG + "onRenderFail ");
        onInsError("onRenderFail");
    }

    @Override
    public void onRenderSuccess(NativeExpressADView nativeExpressADView) {
        if (isDestroyed) {
            return;
        }
        AdLog.getSingleton().LogD(TAG + "onRenderSuccess");
        insReady();
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
    public void onADExposure(NativeExpressADView nativeExpressADView) {
        AdLog.getSingleton().LogD(TAG + "onADExposure");
    }

    @Override
    public void onADClicked(NativeExpressADView nativeExpressADView) {
        if (isDestroyed) {
            return;
        }
        AdLog.getSingleton().LogD(TAG + "onADClicked");
        onInsClicked();
    }

    @Override
    public void onADClosed(NativeExpressADView nativeExpressADView) {
        AdLog.getSingleton().LogD(TAG + "onADClosed");
    }

    @Override
    public void onADLeftApplication(NativeExpressADView nativeExpressADView) {
        AdLog.getSingleton().LogD(TAG + "onADLeftApplication");
    }

    @Override
    public void onADOpenOverlay(NativeExpressADView nativeExpressADView) {
        AdLog.getSingleton().LogD(TAG + "onADOpenOverlay");
    }

    @Override
    public void onADCloseOverlay(NativeExpressADView nativeExpressADView) {
        AdLog.getSingleton().LogD(TAG + "onADCloseOverlay");
    }


    private NativeExpressMediaListener mediaListener = new NativeExpressMediaListener() {
        @Override
        public void onVideoInit(NativeExpressADView nativeExpressADView) {
            AdLog.getSingleton().LogD(TAG, "onVideoInit: "
                    + getVideoInfo(nativeExpressADView.getBoundData().getProperty(AdData.VideoPlayer.class)));
        }

        @Override
        public void onVideoLoading(NativeExpressADView nativeExpressADView) {
            AdLog.getSingleton().LogD(TAG, "onVideoLoading");
        }

        @Override
        public void onVideoCached(NativeExpressADView nativeExpressADView) {
            AdLog.getSingleton().LogD(TAG, "onVideoCached");
            isPreloadVideo = true;
            // 视频素材加载完成，此时展示视频广告不会有进度条。
            if (nativeExpressADView != null) {
                // 广告可见才会产生曝光，否则将无法产生收益。
//                container.addView(nativeExpressADView);
                insReady();
                nativeExpressADView.render();
            }
        }

        @Override
        public void onVideoReady(NativeExpressADView nativeExpressADView, long l) {
            AdLog.getSingleton().LogD(TAG, "onVideoReady");
        }

        @Override
        public void onVideoStart(NativeExpressADView nativeExpressADView) {
            AdLog.getSingleton().LogD(TAG, "onVideoStart: "
                    + getVideoInfo(nativeExpressADView.getBoundData().getProperty(AdData.VideoPlayer.class)));
        }

        @Override
        public void onVideoPause(NativeExpressADView nativeExpressADView) {
            AdLog.getSingleton().LogD(TAG, "onVideoPause: "
                    + getVideoInfo(nativeExpressADView.getBoundData().getProperty(AdData.VideoPlayer.class)));
        }

        @Override
        public void onVideoComplete(NativeExpressADView nativeExpressADView) {
            AdLog.getSingleton().LogD(TAG, "onVideoComplete: "
                    + getVideoInfo(nativeExpressADView.getBoundData().getProperty(AdData.VideoPlayer.class)));
        }

        @Override
        public void onVideoError(NativeExpressADView nativeExpressADView, AdError adError) {
            AdLog.getSingleton().LogD(TAG, "onVideoError");
        }

        @Override
        public void onVideoPageOpen(NativeExpressADView nativeExpressADView) {
            AdLog.getSingleton().LogD(TAG, "onVideoPageOpen");
        }

        @Override
        public void onVideoPageClose(NativeExpressADView nativeExpressADView) {
            AdLog.getSingleton().LogD(TAG, "onVideoPageClose");
        }
    };

    /**
     * 获取播放器实例
     * <p>
     * 仅当视频回调{@link NativeExpressMediaListener#onVideoInit(NativeExpressADView)}调用后才会有返回值
     *
     * @param videoPlayer
     * @return
     */
    private String getVideoInfo(AdData.VideoPlayer videoPlayer) {
        if (videoPlayer != null) {
            StringBuilder videoBuilder = new StringBuilder();
            videoBuilder.append("{state:").append(videoPlayer.getVideoState()).append(",")
                    .append("duration:").append(videoPlayer.getDuration()).append(",")
                    .append("position:").append(videoPlayer.getCurrentPosition()).append("}");
            return videoBuilder.toString();
        }
        return null;
    }

    @Override
    public void registerNativeView(NativeAdView nativeAdView) {
        if (nativeExpressADView == null) return;
        if (nativeAdView.getMediaView() != null) {
            nativeAdView.getMediaView().removeAllViews();
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
            nativeAdView.getMediaView().addView(nativeExpressADView, lp);

        }
    }
}
