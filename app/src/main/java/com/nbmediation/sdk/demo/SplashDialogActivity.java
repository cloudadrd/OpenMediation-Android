// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.demo;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.nbmediation.sdk.demo.utils.NewApiUtils;
import com.nbmediation.sdk.splash.SplashAd;
import com.nbmediation.sdk.splash.SplashAdListener;

public class SplashDialogActivity extends AppCompatActivity implements SplashAdListener {

    private static final String TAG = "SplashDialogActivity";


    private boolean isClick = false;

    private boolean isLoad = false;

    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NewApiUtils.ENABLE_LOG = true;
        setContentView(R.layout.splash_dialog_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorStatuBar));
        }
    }

    public void splashLoad(View view) {
        loadSplash();
    }

    public void splashShow(View view) {
        if (!isLoad) {
            Log.e(TAG, "not load ok.");
            return;
        }
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        dialog = new Dialog(this, R.style.Dialog);
        dialog.show();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        View viewDialog = LayoutInflater.from(this).inflate(R.layout.activity_ad_splash, null);
        ViewGroup mSplashContainer = viewDialog.findViewById(R.id.splash_container);
        android.view.WindowManager.LayoutParams p = this.getWindow().getAttributes();
        p.width = WindowManager.LayoutParams.MATCH_PARENT;
        p.height = WindowManager.LayoutParams.WRAP_CONTENT;
        p.gravity = Gravity.CENTER;
        p.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        p.dimAmount = 0.0f;
        dialog.setContentView(viewDialog);
        SplashAd.showAd(mSplashContainer);

    }


    public void loadSplash() {
        Log.i("time_log", "loadSplash start time=" + System.currentTimeMillis());
        SplashAd.setSplashAdListener(this);
        int width = getWindow().getWindowManager().getDefaultDisplay().getWidth();
        int height = getWindow().getWindowManager().getDefaultDisplay().getHeight();
        SplashAd.setSize(width, height);
        SplashAd.setLoadTimeout(3000);
        SplashAd.loadAd();
    }

    @Override
    public void onSplashAdLoad() {
        isLoad = true;
        Log.i("time_log", "loadSplash success time=" + System.currentTimeMillis());
        Log.e(TAG, "----------- onSplashAdLoad ----------");
        Toast.makeText(this, "splash load success.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSplashAdFailed(String error) {
        Log.e(TAG, "----------- onSplashAdFailed ----------" + error);
        dialogDismiss();
    }

    @Override
    public void onSplashAdClicked() {
        isClick = true;
        Log.e(TAG, "----------- onSplashAdClicked ----------");
    }

    @Override
    public void onSplashAdShowed() {
        Log.e(TAG, "----------- onSplashAdShowed ----------");

    }

    @Override
    public void onSplashAdShowFailed(String error) {
        Log.e(TAG, "----------- onSplashAdShowFailed ----------" + error);
        dialogDismiss();
    }

    @Override
    public void onSplashAdTick(long millisUntilFinished) {
        Log.e(TAG, "----------- onSplashAdTick ----------" + millisUntilFinished);
        if (millisUntilFinished <= 0) {
            if (!isClick) {
                dialogDismiss();
            }
        }
    }

    @Override
    public void onSplashAdDismissed() {
        Log.e(TAG, "----------- onSplashAdDismissed ----------");
        dialogDismiss();
    }

    @Override
    public void onDestroy() {
        SplashAd.setSplashAdListener(null);
        dialogDismiss();
        super.onDestroy();
    }

    private void dialogDismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}
