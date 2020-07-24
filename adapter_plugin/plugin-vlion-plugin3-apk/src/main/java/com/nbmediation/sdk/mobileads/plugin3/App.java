package com.nbmediation.sdk.mobileads.plugin3;

import android.content.Context;
import android.support.multidex.MultiDex;

import com.nbmediation.sdk.mobileads.PluginApplication;

/**
 * Created by jiantao.tu on 2020/7/22.
 */
public class App extends PluginApplication {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
