package com.cloudtech.shell.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cloudtech.shell.SdkImpl;
import com.cloudtech.shell.utils.PreferencesUtils;
import com.cloudtech.shell.manager.TimingTaskManager;
import com.cloudtech.shell.utils.SLog;

/**
 * Created by jiantao.tu on 2018/4/18.
 */
@Deprecated
public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        TimingTaskManager.setRepeating(context, PreferencesUtils.getIntervalSecond());
        SLog.i("AlarmReceiver alarm execute");
        if (SdkImpl.request()) SLog.i("AlarmReceiver alarm execute OK.");
    }
}
