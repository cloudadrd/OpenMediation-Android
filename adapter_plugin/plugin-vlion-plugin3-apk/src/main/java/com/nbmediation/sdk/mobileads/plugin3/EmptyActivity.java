package com.nbmediation.sdk.mobileads.plugin3;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.nbmediation.sdk.mediation.RewardedVideoCallback;
import com.nbmediation.sdk.mobileads.Plugin3Adapter;
import com.nbmediation.sdk.mobileads.PluginApplication;

import cn.vlion.ad.moudle.video.VideoManager;


/**
 * Created by jiantao.tu on 2020/6/18.
 */
public class EmptyActivity extends Activity {


    private static final String TAG = "EmptyActivity";


//    private static TTFullScreenVideoAd mFullScreenVideoAd;

    public final static int SHOW_REWARD_VIDEO = 1;

    public final static int SHOW_INTERSTITIAL = 2;

    public final static int LOAD_REWARD_VIDEO = 3;

    public final static String SHOW_TYPE = "show_type";

    private static Plugin3Adapter mPlugin3Adapter;

    private static String mAdUnitId;

    private static RewardedVideoCallback mCallback;

    public static void showRewardVideoAd() {
        Intent intent = new Intent(PluginApplication.getInstance(), EmptyActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(SHOW_TYPE, SHOW_REWARD_VIDEO);
        PluginApplication.getInstance().startActivity(intent);
    }

    public static void loadRewardedVideo(Plugin3Adapter plugin3Adapter, String adUnitId, RewardedVideoCallback callback) {
        mPlugin3Adapter = plugin3Adapter;
        mAdUnitId = adUnitId;
        mCallback = callback;
        Intent intent = new Intent(PluginApplication.getInstance(), EmptyActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(SHOW_TYPE, LOAD_REWARD_VIDEO);
        PluginApplication.getInstance().startActivity(intent);
    }

//    static void showInterstitialAd(TTFullScreenVideoAd fullScreenVideoAd) {
//        mFullScreenVideoAd = fullScreenVideoAd;
//        Intent intent = new Intent(PluginApplication.getInstance(), EmptyActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.putExtra(SHOW_TYPE, SHOW_INTERSTITIAL);
//        PluginApplication.getInstance().startActivity(intent);
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Log.i(TAG, "EmptyActivity onCreate");
//        Toast.makeText(this, "EmptyActivity起来了", Toast.LENGTH_SHORT).show();
        int showType = getIntent().getIntExtra(SHOW_TYPE, 0);
        if (showType == SHOW_REWARD_VIDEO) {
            VideoManager.getInstance().showVideo();
//        else if (showType == SHOW_INTERSTITIAL && mFullScreenVideoAd != null) {
//            mFullScreenVideoAd.showFullScreenVideoAd(this);
        } else if (showType == LOAD_REWARD_VIDEO) {
            mPlugin3Adapter.loadRvAd(this, mAdUnitId, mCallback);
        } else {
            finish();
            return;
        }
        PluginApplication.MAIN_HANDLER.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 2000);
    }

    @Override
    protected void onStop() {
        super.onStop();
//        mFullScreenVideoAd = null;
        mPlugin3Adapter = null;
        mAdUnitId = null;
        mCallback = null;
        Log.i(TAG, "EmptyActivity onStop");
        finish();
    }

}