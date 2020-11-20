package com.nbmediation.sdk.mobileads;

import android.app.Application;
import android.content.Context;

import com.smaato.sdk.core.Config;
import com.smaato.sdk.core.Gender;
import com.smaato.sdk.core.SmaatoSdk;
import com.smaato.sdk.core.log.LogLevel;

/**
 * Created by jiantao.tu on 2020/11/20.
 */
public class SmaatoHelper {

    private static boolean isInit = false;

    public synchronized static void init(Application context, String key) {
        if (isInit) {
            return;
        }
        // // initialize SDK first!
        Config config = Config.builder()
                // log errors only
                .setLogLevel(LogLevel.ERROR)
                // allow HTTPS traffic only
                .setHttpsOnly(true)
                .build();
        // initialize the Smaato SDK
        SmaatoSdk.init(context, config, key);

        // You can also initialize the Smaato SDK without configuration:
        // SmaatoSdk.init(this, "SMAATO_PUBLISHER_ID");

        // optional configuration
        SmaatoSdk.setSearchQuery("bitcoin, lamborghini, san-francisco");
        SmaatoSdk.setGender(Gender.MALE); // usually set after user logs in
        SmaatoSdk.setAge(40); // usually set after user logs in
        // allow the Smaato SDK to automatically get the user's location
        SmaatoSdk.setGPSEnabled(true);
        isInit = true;
    }
}
