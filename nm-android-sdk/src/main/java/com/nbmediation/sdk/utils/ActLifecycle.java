// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.nbmediation.sdk.utils.crash.CrashUtil;

import java.util.concurrent.atomic.AtomicReference;

public class ActLifecycle implements Application.ActivityLifecycleCallbacks {

    private static String[] ADS_ACT = new String[]{
//            new String(Base64.decode("Y29tLmFkdGltaW5nLm1lZGlhdGlvbnNkaw==", Base64.NO_WRAP),//com.adtiming.mediationsdk
//                    Charset.forName(CommonConstants.CHARTSET_UTF8)),
//            new String(Base64.decode("Y29tLmdvb2dsZS5hbmRyb2lkLmdtcy5hZHM=", Base64.NO_WRAP),//com.google.android.gms.ads
//                    Charset.forName(CommonConstants.CHARTSET_UTF8)),
//            new String(Base64.decode("Y29tLmZhY2Vib29r", Base64.NO_WRAP),//com.facebook
//                    Charset.forName(CommonConstants.CHARTSET_UTF8)),
//            new String(Base64.decode("Y29tLnVuaXR5M2Q=", Base64.NO_WRAP),//com.unity3d
//                    Charset.forName(CommonConstants.CHARTSET_UTF8)),
//            new String(Base64.decode("Y29tLnZ1bmdsZQ==", Base64.NO_WRAP),//com.vungle
//                    Charset.forName(CommonConstants.CHARTSET_UTF8)),
//            new String(Base64.decode("Y29tLmFkY29sb255", Base64.NO_WRAP),//com.adcolony
//                    Charset.forName(CommonConstants.CHARTSET_UTF8)),
//            new String(Base64.decode("Y29tLmFwcGxvdmlu", Base64.NO_WRAP),//com.applovin
//                    Charset.forName(CommonConstants.CHARTSET_UTF8)),
//            new String(Base64.decode("Y29tLm1vcHVi", Base64.NO_WRAP),//com.mopub
//                    Charset.forName(CommonConstants.CHARTSET_UTF8)),
//            new String(Base64.decode("Y29tLnRhcGpveQ==", Base64.NO_WRAP),//com.tapjoy
//                    Charset.forName(CommonConstants.CHARTSET_UTF8)),
//            new String(Base64.decode("Y29tLmNoYXJ0Ym9vc3Q=", Base64.NO_WRAP),//com.chartboost
//                    Charset.forName(CommonConstants.CHARTSET_UTF8)),
//            new String(Base64.decode("Y29tLm5ibWVkaWF0aW9uLnNkay5tb2JpbGVhZHM=", Base64.NO_WRAP),//com.nbmediation.sdk.mobileads
//                    Charset.forName(CommonConstants.CHARTSET_UTF8)),
//            new String(Base64.decode("Y29tLmJ5dGVkYW5jZS5zZGs=", Base64.NO_WRAP),//com.bytedance.sdk
//                    Charset.forName(CommonConstants.CHARTSET_UTF8))
    };
    private AtomicReference<Activity> mThisActivity = new AtomicReference<>(null);

    private static final class DKLifecycleHolder {
        private static final ActLifecycle INSTANCE = new ActLifecycle();
    }

    private ActLifecycle() {
        try {
            AdtUtil.getApplication().registerActivityLifecycleCallbacks(this);
        } catch (Exception e) {
            CrashUtil.getSingleton().saveException(e);
        }
    }

    public static ActLifecycle getInstance() {
        return DKLifecycleHolder.INSTANCE;
    }

    public void init(Activity activity) {
        mThisActivity.set(activity);
    }

    public Activity getActivity() {
        return mThisActivity.get();
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        DeveloperLog.LogD("onActivityCreated: " + activity.toString());
        mThisActivity.set(activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        DeveloperLog.LogD("onActivityStarted -begin: " + activity.toString());
        if (isAdActivity(activity)) {
            DeveloperLog.LogD("onActivityStarted -end isAdActivity return");
            return;
        }
        Activity old = mThisActivity.get();
        if (old == null || old != activity) {
            mThisActivity.set(activity);
        }
        DeveloperLog.LogD("onActivityStarted -end mThisActivity=: " + mThisActivity.get());

    }

    @Override
    public void onActivityResumed(Activity activity) {
        DeveloperLog.LogD("onActivityResumed classpath: " + activity.toString());
    }

    @Override
    public void onActivityPaused(Activity activity) {
        DeveloperLog.LogD("onActivityPaused classpath: " + activity.toString());
    }

    @Override
    public void onActivityStopped(Activity activity) {
        DeveloperLog.LogD("onActivityStopped classpath: " + activity.toString());
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        Activity current = mThisActivity.get();
        DeveloperLog.LogD("onActivityDestroyed activity=" + activity.toString() + ", current=" + (current != null ? current.toString() : "null"));

        if (current == activity) {
            mThisActivity.set(null);
        }
    }

    private boolean isAdActivity(Activity activity) {
        String address = activity.toString();
        for (String s : ADS_ACT) {
            if (address.contains(s)) {
                return true;
            }
        }
        return false;
    }
}
