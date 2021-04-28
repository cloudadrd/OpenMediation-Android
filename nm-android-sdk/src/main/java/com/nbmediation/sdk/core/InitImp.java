// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.core;

import android.app.Activity;
import android.text.TextUtils;

import com.nbmediation.sdk.InitCallback;
import com.nbmediation.sdk.InitConfiguration;
import com.nbmediation.sdk.bid.BidAuctionManager;
import com.nbmediation.sdk.utils.AFManager;
import com.nbmediation.sdk.utils.ActLifecycle;
import com.nbmediation.sdk.utils.AdLog;
import com.nbmediation.sdk.utils.AdtUtil;
import com.nbmediation.sdk.utils.DeveloperLog;
import com.nbmediation.sdk.utils.HandlerUtil;
import com.nbmediation.sdk.utils.IOUtil;
import com.nbmediation.sdk.utils.JsonUtil;
import com.nbmediation.sdk.utils.OaidHelper;
import com.nbmediation.sdk.utils.SdkUtil;
import com.nbmediation.sdk.utils.WorkExecutor;
import com.nbmediation.sdk.utils.cache.DataCache;
import com.nbmediation.sdk.utils.constant.CommonConstants;
import com.nbmediation.sdk.utils.constant.KeyConstants;
import com.nbmediation.sdk.utils.crash.CrashUtil;
import com.nbmediation.sdk.utils.device.AdvertisingIdClient;
import com.nbmediation.sdk.utils.error.Error;
import com.nbmediation.sdk.utils.error.ErrorBuilder;
import com.nbmediation.sdk.utils.error.ErrorCode;
import com.nbmediation.sdk.utils.event.EventId;
import com.nbmediation.sdk.utils.event.EventUploadManager;
import com.nbmediation.sdk.utils.helper.ConfigurationHelper;
import com.nbmediation.sdk.utils.model.Configurations;
import com.nbmediation.sdk.utils.request.network.Request;
import com.nbmediation.sdk.utils.request.network.Response;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The type Init imp.
 */
public final class InitImp {
    private static AtomicBoolean hasInit = new AtomicBoolean(false);
    private static AtomicBoolean isInitRunning = new AtomicBoolean(false);
    private static InitCallback mCallback;
    private static long sInitStart;

    /**
     * init method
     *
     * @param activity the activity
     * @param callback the callback
     * @param channel  the channel
     */
    public static void init(final Activity activity, InitConfiguration configuration, final InitCallback callback) {
        //
        if (hasInit.get()) {
            return;
        }

        if (isInitRunning.get()) {
            return;
        }

        if (activity == null) {
            Error error = new Error(ErrorCode.CODE_INIT_INVALID_REQUEST
                    , ErrorCode.MSG_INIT_INVALID_REQUEST, ErrorCode.CODE_INTERNAL_REQUEST_ACTIVITY);
            DeveloperLog.LogE(error.toString() + ", init failed because activity is null");
            callbackInitErrorOnUIThread(error);
            return;
        }
        isInitRunning.set(true);
        sInitStart = System.currentTimeMillis();
        mCallback = callback;
        AdtUtil.init(activity);
//        SensorManager.getSingleton();
        ActLifecycle.getInstance().init(activity);
        EventUploadManager.getInstance().init(activity.getApplicationContext());
        EventUploadManager.getInstance().uploadEvent(EventId.INIT_START);
        WorkExecutor.execute(new InitAsyncRunnable(configuration));
    }

    /**
     * Re init sdk.
     *
     * @param activity the activity
     * @param callback the callback
     */
    static void reInitSDK(Activity activity, final InitCallback callback) {
        if (DataCache.getInstance().containsKey(KeyConstants.KEY_APP_KEY)) {
            String appKey = DataCache.getInstance().getFromMem(KeyConstants.KEY_APP_KEY, String.class);
            String appChannel = DataCache.getInstance().getFromMem(KeyConstants.KEY_APP_CHANNEL, String.class);
            String initHost = DataCache.getInstance().getFromMem(KeyConstants.KEY_INIT_HOST, String.class);
            InitConfiguration configuration = new InitConfiguration.Builder().appKey(appKey)
                    .channel(appChannel).initHost(initHost).build();
            InitImp.init(activity, configuration, new InitCallback() {
                @Override
                public void onSuccess() {
                    DeveloperLog.LogD("reInitSDK success");
                    callback.onSuccess();
                }

                @Override
                public void onError(Error error) {
                    callback.onError(error);
                }
            });
        } else {
            Error error = ErrorBuilder.build(ErrorCode.CODE_LOAD_INVALID_REQUEST
                    , ErrorCode.ERROR_NOT_INIT, ErrorCode.CODE_INTERNAL_REQUEST_APPKEY);
            callback.onError(error);
        }
    }

    /**
     * init success?
     *
     * @return the boolean
     */
    public static boolean isInit() {
        return hasInit.get();
    }

    /**
     * Is init running boolean.
     *
     * @return the boolean
     */
    static boolean isInitRunning() {
        return isInitRunning.get();
    }

    private static void requestConfig(Activity activity, InitConfiguration configuration) {
        try {
            DeveloperLog.LogD("Nm init request config");
            //requests Config
            ConfigurationHelper.getConfiguration(configuration.getAppKey(), configuration.getInitHost(),
                    new InitRequestCallback(activity, configuration.getAppKey()));
        } catch (Exception e) {
            DeveloperLog.LogD("requestConfig  exception : ", e);
            CrashUtil.getSingleton().saveException(e);
            Error error = new Error(ErrorCode.CODE_INIT_UNKNOWN_INTERNAL_ERROR
                    , ErrorCode.MSG_INIT_UNKNOWN_INTERNAL_ERROR, ErrorCode.CODE_INTERNAL_UNKNOWN_OTHER);
            DeveloperLog.LogE(error.toString() + ", requestConfig");
            callbackInitErrorOnUIThread(error);
        }
    }

    /**
     * Inits global utils
     */
    private static void initUtil() {
        DataCache.getInstance().init(AdtUtil.getApplication());
    }

    private static void doAfterGetConfig(String appKey, Configurations config) {
        try {
            DeveloperLog.enableDebug(AdtUtil.getApplication(), config.getD() == 1);
            AFManager.checkAfDataStatus();
            EventUploadManager.getInstance().updateReportSettings(config);
            //reports error logs
            CrashUtil.getSingleton().uploadException(config, appKey);
        } catch (Exception e) {
            DeveloperLog.LogD("doAfterGetConfig  exception : ", e);
            CrashUtil.getSingleton().saveException(e);
        }
    }

    private static void callbackInitErrorOnUIThread(final Error result) {
        AdLog.getSingleton().LogE("Init Failed: " + result);
        HandlerUtil.runOnUiThread(new InitFailRunnable(result));
    }

    private static void callbackInitSuccessOnUIThread() {
        AdLog.getSingleton().LogD("Init Success");
        HandlerUtil.runOnUiThread(new InitSuccessRunnable());
    }

    private static void initCompleteReport(int eventId, Error error) {
        JSONObject jsonObject = new JSONObject();
        if (error != null) {
            JsonUtil.put(jsonObject, "msg", error);
        }
        if (sInitStart != 0) {
            int dur = (int) (System.currentTimeMillis() - sInitStart) / 1000;
            JsonUtil.put(jsonObject, "duration", dur);
        }
        EventUploadManager.getInstance().uploadEvent(eventId, jsonObject);
    }

    private static class InitSuccessRunnable implements Runnable {

        @Override
        public void run() {
            DeveloperLog.LogD("Nm init Success ");
            hasInit.set(true);
            isInitRunning.set(false);
            if (mCallback != null) {
                mCallback.onSuccess();
            }
            initCompleteReport(EventId.INIT_COMPLETE, null);
        }
    }

    private static class InitAsyncRunnable implements Runnable {
        private InitConfiguration initConfiguration;

        private InitAsyncRunnable(InitConfiguration configuration) {
            this.initConfiguration = configuration;
        }

        @Override
        public void run() {
            try {
                final Activity activity = ActLifecycle.getInstance().getActivity();
                Error error = SdkUtil.banRun(activity, initConfiguration.getAppKey());
                if (error != null) {
                    callbackInitErrorOnUIThread(error);
                    return;
                }
                initUtil();
                DataCache.getInstance().setMEM(KeyConstants.KEY_APP_KEY, initConfiguration.getAppKey());
                String appChannel = initConfiguration.getChannel();
                if (TextUtils.isEmpty(appChannel)) {
                    appChannel = "";
                }
                DataCache.getInstance().setMEM(KeyConstants.KEY_APP_CHANNEL, appChannel);
                if (!TextUtils.isEmpty(initConfiguration.getInitHost())) {
                    DataCache.getInstance().setMEM(KeyConstants.KEY_INIT_HOST, initConfiguration.getInitHost());
                }
                AdvertisingIdClient.getGaid(AdtUtil.getApplication(), new AdvertisingIdClient.OnGetGaidListener() {
                    @Override
                    public void onGetGaid(String gaid) {
                        if (!TextUtils.isEmpty(gaid)) {
                            DataCache.getInstance().set(KeyConstants.RequestBody.KEY_GAID, gaid);
                            requestConfig(activity, initConfiguration);
                        } else {
                            OaidHelper.getOaid(AdtUtil.getApplication(), new OaidHelper.oaidListener() {
                                @Override
                                public void onGetOaid(String oaid) {
                                    requestConfig(activity, initConfiguration);
                                }
                            });
                        }
                    }
                });
            } catch (Exception e) {
                DeveloperLog.LogD("initOnAsyncThread  exception : ", e);
                CrashUtil.getSingleton().saveException(e);
                Error error = new Error(ErrorCode.CODE_INIT_UNKNOWN_INTERNAL_ERROR
                        , ErrorCode.MSG_INIT_UNKNOWN_INTERNAL_ERROR, ErrorCode.CODE_INTERNAL_UNKNOWN_OTHER);
                DeveloperLog.LogE(error.toString() + ", initOnAsyncThread");
                callbackInitErrorOnUIThread(error);
            }
        }
    }

    private static class InitFailRunnable implements Runnable {
        private Error mError;

        /**
         * Instantiates a new Init fail runnable.
         *
         * @param result the result
         */
        InitFailRunnable(Error result) {
            mError = result;
        }

        @Override
        public void run() {
            DeveloperLog.LogD("Nm init error  " + mError);
            hasInit.set(false);
            isInitRunning.set(false);
            if (mCallback != null) {
                mCallback.onError(mError);
            }
            initCompleteReport(EventId.INIT_FAILED, mError);
        }
    }

    private static class InitRequestCallback implements Request.OnRequestCallback {

        private String appKey;
        private Activity mActivity;

        /**
         * Instantiates a new Init request callback.
         *
         * @param appKey the app key
         */
        InitRequestCallback(Activity activity, String appKey) {
            this.appKey = appKey;
            this.mActivity = activity;
        }

        @Override
        public void onRequestSuccess(Response response) {
            try {
                if (response.code() != HttpURLConnection.HTTP_OK) {
                    Error error = new Error(ErrorCode.CODE_INIT_SERVER_ERROR
                            , ErrorCode.MSG_INIT_SERVER_ERROR, ErrorCode.CODE_INTERNAL_SERVER_ERROR);
                    DeveloperLog.LogE(error.toString() + "Nm init request config response code not 200 : " + response.code());
                    callbackInitErrorOnUIThread(error);
                    return;
                }

                String requestData = new String(ConfigurationHelper.checkResponse(response), Charset.forName(CommonConstants.CHARTSET_UTF8));
                if (TextUtils.isEmpty(requestData)) {
                    Error error = new Error(ErrorCode.CODE_INIT_SERVER_ERROR
                            , ErrorCode.MSG_INIT_SERVER_ERROR, ErrorCode.CODE_INTERNAL_SERVER_ERROR);
                    DeveloperLog.LogE(error.toString() + ", Nm init response data is null: " + requestData);
                    callbackInitErrorOnUIThread(error);
                    return;
                }
                //adds global data to memory
                Configurations config = ConfigurationHelper.parseFormServerResponse(requestData);
                if (config != null) {
                    DeveloperLog.LogD("Nm init request config success");
                    DataCache.getInstance().setMEM(KeyConstants.KEY_CONFIGURATION, config);
                    try {
                        BidAuctionManager.getInstance().initBid(mActivity, config);
                    } catch (Exception e) {
                        DeveloperLog.LogD("initBid  exception : ", e);
                        CrashUtil.getSingleton().saveException(e);
                    }
                    callbackInitSuccessOnUIThread();
                    doAfterGetConfig(appKey, config);
                } else {
                    Error error = new Error(ErrorCode.CODE_INIT_SERVER_ERROR
                            , ErrorCode.MSG_INIT_SERVER_ERROR, ErrorCode.CODE_INTERNAL_SERVER_ERROR);
                    DeveloperLog.LogE(error.toString() + ", Nm init format config is null");
                    callbackInitErrorOnUIThread(error);
                }
            } catch (Exception e) {
                CrashUtil.getSingleton().saveException(e);
                Error error = new Error(ErrorCode.CODE_INIT_SERVER_ERROR
                        , ErrorCode.MSG_INIT_SERVER_ERROR, ErrorCode.CODE_INTERNAL_UNKNOWN_OTHER);
                DeveloperLog.LogE(error.toString() + ", request config exception:" + e);
                callbackInitErrorOnUIThread(error);
            } finally {
                IOUtil.closeQuietly(response);
            }
        }

        @Override
        public void onRequestFailed(String error) {
            Error result = new Error(ErrorCode.CODE_INIT_SERVER_ERROR
                    , ErrorCode.MSG_INIT_SERVER_ERROR, ErrorCode.CODE_INTERNAL_SERVER_FAILED);
            DeveloperLog.LogE("request config failed : " + result + ", error:" + error);
            AdLog.getSingleton().LogE("Init Failed: " + error);
            callbackInitErrorOnUIThread(result);
        }
    }
}
