// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.demo;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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

public class SplashDialogActivityOld extends AppCompatActivity implements SplashAdListener {

    private static final String TAG = "SplashDialogActivity";

    ViewGroup mSplashContainer;

    private View viewDialog;

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
        viewDialog = LayoutInflater.from(this).inflate(R.layout.activity_ad_splash, null);
        mSplashContainer = viewDialog.findViewById(R.id.splash_container);
        WindowManager.LayoutParams p = this.getWindow().getAttributes();  //获取对话框当前的参数值
//        p.width = dm.widthPixels;   //高度设置为屏幕
//        p.height = dm.heightPixels;    //宽度设置为全屏
        p.width = WindowManager.LayoutParams.MATCH_PARENT;
        p.height = WindowManager.LayoutParams.WRAP_CONTENT;
        p.gravity = Gravity.CENTER;
        p.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        p.dimAmount = 0.0f;
        dialog.setContentView(viewDialog);
//        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_IMMERSIVE
//                | View.SYSTEM_UI_FLAG_FULLSCREEN;
//        dialog.getWindow().getDecorView().setSystemUiVisibility(uiOptions);

//        dialog = new CommonDialog1(this);
//        dialog.show();
//        mSplashContainer = dialog.findViewById(R.id.splash_container);
        SplashAd.showAd(mSplashContainer);

    }

    public int getStatusBarHeight(Context context) {
        int statusBarHeight = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }

    public class CommonDialog1 extends Dialog {

        public CommonDialog1(Context context) {
            super(context, R.style.Dialog);
            setOwnerActivity((Activity) context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);

            setContentView(R.layout.activity_ad_splash);
            //按空白处不能取消动画
            setCanceledOnTouchOutside(true);
            //设置window背景，默认的背景会有Padding值，不能全屏。当然不一定要是透明，你可以设置其他背景，替换默认的背景即可。
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            //一定要在setContentView之后调用，否则无效
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }


        @Override
        public void show() {
            super.show();
        }

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
