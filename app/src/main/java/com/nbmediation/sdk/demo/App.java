package com.nbmediation.sdk.demo;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;


/**
 * Created by jiantao.tu on 2020/6/3.
 */
public class App extends Application {

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
