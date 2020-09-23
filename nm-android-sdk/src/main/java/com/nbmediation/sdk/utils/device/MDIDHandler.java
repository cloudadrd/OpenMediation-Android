package com.nbmediation.sdk.utils.device;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.bun.miitmdid.core.MdidSdkHelper;
import com.bun.miitmdid.interfaces.IIdentifierListener;
import com.bun.miitmdid.interfaces.IdSupplier;
import com.nbmediation.sdk.utils.DeveloperLog;
import com.nbmediation.sdk.utils.cache.DataCache;

import static com.bun.miitmdid.core.ErrorCode.INIT_ERROR_DEVICE_NOSUPPORT;
import static com.bun.miitmdid.core.ErrorCode.INIT_ERROR_LOAD_CONFIGFILE;
import static com.bun.miitmdid.core.ErrorCode.INIT_ERROR_MANUFACTURER_NOSUPPORT;
import static com.bun.miitmdid.core.ErrorCode.INIT_ERROR_RESULT_DELAY;
import static com.bun.miitmdid.core.ErrorCode.INIT_HELPER_CALL_ERROR;


/**
 * Created by jiantao.tu on 2020/3/23.
 */
public class MDIDHandler {

    public static String MDID = null;

    private static final String MDID_ID_KEY = "mdid_id_key";

    public static void init(Context context) {
        if (Build.VERSION.SDK_INT < 21) {
            return;
        }
        try {
            int code = MdidSdkHelper.InitSdk(context, true, new IIdentifierListener() {
                @Override
                public void OnSupport(boolean b, IdSupplier idSupplier) {
                    if (idSupplier == null) {
                        return;
                    }
                    String oaid = idSupplier.getOAID();
                    if (oaid.equals("NO") || TextUtils.isEmpty(oaid)) {
                        oaid = "";
                    }
//                    String vaid = idSupplier.getVAID();
//                    String aaid = idSupplier.getAAID();
//                    StringBuilder builder = new StringBuilder();
//                    builder.append("support: ").append(b ? "true" : "false").append("\n");
//                    builder.append("OA=").append(oaid).append("$");
//                    builder.append("VA=").append(vaid).append("$");
//                    builder.append("AA=").append(aaid).append("$");
//                    MDID = builder.toString();
                    MDID = oaid;
                    DataCache.getInstance().set(MDID_ID_KEY, MDID);
                    DeveloperLog.LogD("mdid: value=" + MDID);
                }
            });
            switch (code) {
                case INIT_ERROR_DEVICE_NOSUPPORT://不支持的设备
                    DeveloperLog.LogD("mdid 不支持的设备");
                    break;
                case INIT_ERROR_LOAD_CONFIGFILE://加载配置文件出错
                    DeveloperLog.LogD("mdid 加载配置文件出错");
                    break;
                case INIT_ERROR_MANUFACTURER_NOSUPPORT://不支持的设备厂商
                    DeveloperLog.LogD("mdid 不支持的设备厂商");
                    break;
                case INIT_ERROR_RESULT_DELAY://获取接口是异步的，结果会在回调中返回，回调执行的回调可能在工作线程
                    DeveloperLog.LogD("mdid 获取接口是异步的，结果会在回调中返回，回调执行的回调可能在工作线程");
                    break;
                case INIT_HELPER_CALL_ERROR://反射调用出错
                    DeveloperLog.LogD("mdid 反射调用出错");
                    break;
            }
        } catch (Throwable e) {
            DeveloperLog.LogE("MDIDHandler", e);
        }
    }

    public static String getMdid() {
        return TextUtils.isEmpty(MDID) ? MDID : DataCache.getInstance().get(MDID_ID_KEY, String.class);
    }
}
