package com.nbmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Intent;

import com.nbmediation.sdk.mobileads.BaiduSplashActivity;

public class BaiduSplashHelper {

    static void ShowSplash(Activity activity, String appID, String appName, String slotID){
        Intent intent = new Intent(activity, BaiduSplashActivity.class);
        slotID = "2058622";
        intent.putExtra("adPlace_Id", slotID);
        activity.startActivity(intent);
    }
}
