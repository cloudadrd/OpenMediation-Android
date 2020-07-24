package com.nbmediation.sdk.utils.cache;

import android.text.TextUtils;

import com.nbmediation.sdk.utils.constant.KeyConstants;

import java.util.Map;

/**
 * Created by jiantao.tu on 2020/7/21.
 */
public class GlobalVariable {

    public static String CUSTOM_ID = null;


    public static void setCustomDataForMap(Map<String, String> data) {
        if (!TextUtils.isEmpty(GlobalVariable.CUSTOM_ID)) {
            data.put(KeyConstants.CUSTOM_ID_KEY, GlobalVariable.CUSTOM_ID);
        }
    }

    public static void setCustomDataObjForMap(Map<String, Object> data) {
        if (!TextUtils.isEmpty(GlobalVariable.CUSTOM_ID)) {
            data.put(KeyConstants.CUSTOM_ID_KEY, GlobalVariable.CUSTOM_ID);
        }
    }

}
