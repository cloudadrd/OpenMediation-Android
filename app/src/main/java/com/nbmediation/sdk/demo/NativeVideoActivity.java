package com.nbmediation.sdk.demo;

import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.adsgreat.base.config.Const;
import com.nbmediation.sdk.demo.utils.NewApiUtils;
import com.nbmediation.sdk.mediation.MediationInfo;
import com.nbmediation.sdk.nativead.AdIconView;
import com.nbmediation.sdk.nativead.AdInfo;
import com.nbmediation.sdk.nativead.MediaView;
import com.nbmediation.sdk.nativead.NativeAd;
import com.nbmediation.sdk.nativead.NativeAdListener;
import com.nbmediation.sdk.nativead.NativeAdView;
import android.widget.Toast;


public class NativeVideoActivity extends AppCompatActivity {
    private NativeAd nativeAd;
    private View adView;
    private NativeAdView nativeAdView;
    private LinearLayout adContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_native_video);
        adContainer = findViewById(R.id.ad_container);
        getNativeVideo();
    }

    private void  getNativeVideo(){
        if (nativeAd != null) {
            nativeAd.destroy();
        }
        Toast.makeText(NativeVideoActivity.this,"ad is loading...",Toast.LENGTH_LONG).show();

        nativeAd = new NativeAd(this, NewApiUtils.P_NATIVE, 360, 275, new NativeAdListener() {
            @Override
            public void onAdFailed(String msg) {
                Toast.makeText(NativeVideoActivity.this,"get ad failed!",Toast.LENGTH_LONG).show();
                Const.HANDLER.postDelayed(new Runnable() {
                    @Override
                    public void run() {
//                        finish();
                    }
                },3000);

            }

            @Override
            public void onAdReady(AdInfo info) {
                adContainer.removeAllViews();
                adView = LayoutInflater.from(NativeVideoActivity.this).inflate(R.layout.native_ad_layout, null);
                MediaView mediaView = adView.findViewById(R.id.ad_media);
                nativeAdView = new NativeAdView(NativeVideoActivity.this);
                nativeAdView.addView(adView);
                nativeAdView.setMediaView(mediaView);
                nativeAd.registerNativeAdView(nativeAdView);
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                adContainer.addView(nativeAdView, layoutParams);
            }

            @Override
            public void onAdClicked() {

            }
        });
        nativeAd.loadAd();
    }

    public static int dp2px(int dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, Resources.getSystem().getDisplayMetrics());
    }
}