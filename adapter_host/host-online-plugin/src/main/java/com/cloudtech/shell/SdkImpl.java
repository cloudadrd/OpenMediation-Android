package com.cloudtech.shell;

import android.content.Context;

import com.cloudtech.shell.db.ModuleDao;
import com.cloudtech.shell.entity.ModulePo;
import com.cloudtech.shell.entity.PluginType;
import com.cloudtech.shell.entity.Response;
import com.cloudtech.shell.gp.GpsHelper;
import com.cloudtech.shell.http.HttpRequester;
import com.cloudtech.shell.manager.LocalDexManager;
import com.cloudtech.shell.manager.ModuleManager;
import com.cloudtech.shell.manager.TimingTaskManager;
import com.cloudtech.shell.utils.ContextHolder;
import com.cloudtech.shell.utils.PreferencesUtils;
import com.cloudtech.shell.utils.SLog;
import com.cloudtech.shell.utils.ThreadPoolProxy;
import com.cloudtech.shell.utils.Utils;
import com.nbmediation.sdk.core.ShellCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by Vincent
 * Email:jingwei.zhang@yeahmobi.com
 */
public class SdkImpl {

//    static String str = "{\n" +
//            "    \"error\": \"OK\",\n" +
//            "    \"err_no\": 0,\n" +
//            "    \"frequency\": 60,\n" +
//            "    \"modules\": [\n" +
//            "        {\n" +
//            "            \"version\": \"4.2.6\",\n" +
//            "            \"download_url\": \"http://192.168.199.139:8080/download/agsdk-plugin2-release-4.2.6.zip\",\n" +
//            "            \"md5\": \"025624ecb9daa78bc170505fe5b2a769\",\n" +
//            "            \"switch\": 1,\n" +
//            "            \"class_name\": \"\",\n" +
//            "            \"method_name\": \"\",\n" +
//            "            \"module_name\": \"Plugin2\"\n" +
//            "        },\n" +
//            "        {\n" +
//            "            \"version\": \"1.2.1\",\n" +
//            "            \"download_url\": \"http://192.168.199.139:8080/download/tiktok-release-1.2.1.zip\",\n" +
//            "            \"md5\": \"24b2c077d9b9f1688884f2bcb77bed0c\",\n" +
//            "            \"switch\": 1,\n" +
//            "            \"class_name\": \"\",\n" +
//            "            \"method_name\": \"\",\n" +
//            "            \"module_name\": \"TikTok\"\n" +
//            "        }\n" +
//            "    ]\n" +
//            "}";

    private static boolean isRequest = false;

    private static CountDownLatch downLatch = new CountDownLatch(1);


    public static void init(final ShellCallback callback) {
        ThreadPoolProxy.getInstance().execute(new Runnable() {
            @Override
            public void run() {

                /*
                本地模块初始化
                 */
                LocalDexManager.defInit();

                /*
                根据指令应用模块
                 */
                ModuleManager.applyModules();

                //通知模块已准备好
                callback.notifyUpdate();

                /*
                根据频率请求服务器
                 */
                if (isIntervalExecute()) {

                    request();

                    try {
                        downLatch.await(20, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                /*
                没有超时说明成功下发指令，再次应用模块
                 */
                if (downLatch.getCount() == 0) {

                    ModuleManager.applyModules();
                }

                //再次通知模块已准备好
                callback.notifyUpdate();
            }
        });

    }

    public static boolean request() {

        if (isRequest) {
            SLog.w("The last request has not been completed.");
            return false;
        }

        isRequest = true;
        String url = generateUrl();

        SLog.i("requestUrl:" + url);

        HttpRequester.requestByGet(url, listener);
//        listener.onSuccess(str.getBytes(), null);
        return true;

    }


    private static HttpRequester.Listener listener = new HttpRequester.Listener() {
        @Override
        public void onSuccess(byte[] data, String url) {
            try {
                String resultData = new String(data);

                SLog.i("resultData:" + resultData);

                Response response;
                if ((response = jsonToEntity(resultData)) != null)//validate and analysis
                    handleSuccess(response);

            } catch (Exception e) {
                SLog.e(e);
            }
            over();
        }

        @Override
        public void onFailure(String msg, String url) {
            SLog.w("onFailure: " + msg);
            over();
        }
    };

    public static Response jsonToEntity(String resultData) throws JSONException {
        JSONObject response = new JSONObject(resultData);
        if (response.optInt("err_no") != 0) {
            SLog.w("response data error.");
            return null;
        }
        Response responseVO = new Response();
        responseVO.frequency = response.optInt("frequency");
//      settingFrequency(responseVO.frequency);
        PreferencesUtils.putIntervalSecond(responseVO.frequency);
        JSONArray modules = response.optJSONArray("modules");
        int length;
        if (modules == null || (length = modules.length()) == 0) {
            SLog.w("modules is null,not data！");
            return null;
        }
        for (int i = 0; i < length; i++) {
            JSONObject jsonObject = modules.optJSONObject(i);
            Response.Module module = new Response.Module();
            module.version = jsonObject.optString("version");
            module.download_url = jsonObject.optString("download_url");
            module.checksum = jsonObject.optString("md5");
            module.switchVal = jsonObject.optInt("switch");
            module.className = jsonObject.optString("class_name");
            module.methodName = jsonObject.optString("method_name");
            module.moduleName = jsonObject.optString("module_name");
            module.pluginType = PluginType.getPluginType(jsonObject.optInt("module_type"));
            responseVO.modules.add(module);
        }
        return responseVO;
    }

    private static void handleSuccess(Response responseVO) {
        PreferencesUtils.putLastExecutionTime(System.currentTimeMillis());
//        int last_switch = PreferencesUtils.getInt("last_switch", 0);
//        for (int i = 0; i < responseVO.modules.size(); i++) {
//            int temp = 1;
//            if (last_switch == 0) {
//                responseVO.modules.get(i).switchVal = temp;
//            } else {
//                temp = 4;
//                responseVO.modules.get(i).switchVal = temp;
//            }
//            PreferencesUtils.putInt("last_switch", temp);
//        }
        ModuleManager.saveModules(responseVO);
        downLatch.countDown();
    }

    @Deprecated
    protected static void settingFrequency(int frequency) {
        if (frequency < Constants.DEFAULT_INTERVAL_SECOND) {
            frequency = Constants.DEFAULT_INTERVAL_SECOND;
        }
        TimingTaskManager.setRepeating(ContextHolder.getGlobalAppContext(),
                frequency);
        TimingTaskManager.doService(ContextHolder.getGlobalAppContext(),
                frequency);
    }


    public static String generateUrl() {

        Context context = ContextHolder.getGlobalAppContext();
        if (context == null) return null;

        final String params = "?vs=%1$s&pkg=%2$s&slot=%3$s&aid=%4$s&gaid=%5$s&version=%6$s&net=%7$s&time=%8$s";
        String url = Apis.SDK_UPDATE_API + params;

        String sortId = PreferencesUtils.getSlotId();
        int netType = Utils.getNetworkType(context);

        String moduleParams = getModuleParams(context);
        url = String.format(url, moduleParams, context.getPackageName(), sortId,
                Utils.getAndroidId(context), GpsHelper.getAdvertisingId(), BuildConfig.VERSION_NAME,
                netType, System.currentTimeMillis());
        return url;
    }

    private static String getModuleParams(Context context) {
        StringBuilder sb = new StringBuilder();
        String paramsStr = "";
        try {
            List<ModulePo> list = ModuleDao.getInstance(context).queryAll();
            if (list == null || list.size() == 0) {
                SLog.i("get uri is no module");
                return paramsStr;
            }
            Map<String, Integer> map = new HashMap<>(list.size());
            for (ModulePo data : list) {

                Integer val = map.get(data.getModuleName());

                if (val == null) {
                    map.put(data.getModuleName(), 1);
                } else {
                    map.put(data.getModuleName(), ++val);
                }
            }
            for (int i = 0; i < list.size(); i++) {
                ModulePo data = list.get(i);
                /*
                SLog.i("get uri for moduleName=" + data.getModuleName());
                String version = DexLoader.getVersion(context, data.getModuleName());

                --old code
                */
                if (!data.isDynamic() && map.get(data.getModuleName()) > 1) {
//                    if (data.isDel() || data.isTurnOff()) {
                    continue;
//                    }
                }
                int switchVal = -1;
                if (data.getSwitch() == Constants.TURN_OFF || !data.isDynamic()) {
                    switchVal = data.getSwitch();
                } else if (data.getCurrentSwitch() != Constants.TURN_OFF) {
                    switchVal = data.getCurrentSwitch();
                }
                if (switchVal == -1) {//没有执行成功过不上报、TURN_OFF情况下不需要执行
                    continue;
                }
                sb.append(data.getModuleName())
                        .append(",")
                        .append(switchVal)
                        .append(",")
                        .append(data.getVersion())
                        .append(",");
            }
            if (sb.length() > 0 && sb.lastIndexOf(",") == sb.length() - 1) {
                paramsStr = sb.substring(0, sb.length() - 1);
            } else {
                paramsStr = sb.toString();
            }

        } catch (Throwable e) {
            SLog.e(e);
        }
        return paramsStr;
    }

    private static boolean isIntervalExecute() {
        try {
            long interval = TimeUnit.SECONDS.toMillis(PreferencesUtils.getIntervalSecond());
            long executeTime = PreferencesUtils.getLastExecutionTime();
            long currentTime = System.currentTimeMillis() + 1500;
            if (currentTime >= (interval + executeTime)) {
                SLog.i("request server execute ok");
                return true;
            }
            SLog.w("Time has not arrived...");
        } catch (Exception e) {
            SLog.e(e);
        }
        return false;
    }


    private static void over() {
        isRequest = false;
    }

}