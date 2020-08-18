package com.nbmediation.sdk.mobileads.tiktok;

import android.app.Activity;
import android.content.Intent;

import com.nbmediation.sdk.mobileads.TTAdManagerHolder;
import com.nbmediation.sdk.mobileads.tiktok.TikTokSplash;

public class TikTokSplashHelper {
    static void ShowSplash(Activity activity, String appID, String appName, String slotID){
        TTAdManagerHolder.init(activity.getApplicationContext(), appID, appName);
        Intent intent = new Intent(activity, TikTokSplash.class);
        intent.putExtra("is_express", false);
        intent.putExtra("splash_rit", slotID);
        activity.startActivity(intent);
    }
}
