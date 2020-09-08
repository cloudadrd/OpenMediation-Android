// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.utils.request;

import android.content.Context;
import android.os.Build;

import com.nbmediation.sdk.bid.AdTimingBidResponse;
import com.nbmediation.sdk.utils.constant.CommonConstants;
import com.nbmediation.sdk.utils.constant.KeyConstants;
import com.nbmediation.sdk.utils.device.DeviceUtil;
import com.nbmediation.sdk.utils.device.MDIDHandler;
import com.nbmediation.sdk.utils.device.SensorManager;
import com.nbmediation.sdk.utils.request.network.util.NetworkChecker;
import com.nbmediation.sdk.utils.AdtUtil;
import com.nbmediation.sdk.utils.DensityUtil;
import com.nbmediation.sdk.utils.DeveloperLog;
import com.nbmediation.sdk.utils.Gzip;
import com.nbmediation.sdk.utils.JsonUtil;
import com.nbmediation.sdk.utils.event.Event;
import com.nbmediation.sdk.utils.cache.DataCache;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;

/**
 * RequestBuilder
 */
public class RequestBuilder {

    private static final Pattern REG_UNMATCHED_PERCENTAGE = Pattern.compile("(?i)%(?![\\da-f]{2})");

    private static class Param {
        private String name, value;

        private Param(String name, Object value) {
            this.name = name;
            this.value = value == null ? "" : value.toString();
        }
    }

    private List<Param> ps;

    /**
     * Instantiates a new Request builder.
     */
    public RequestBuilder() {
        this.ps = new ArrayList<>();
    }

    private RequestBuilder(List<Param> ps) {
        this.ps = ps;
    }

    /**
     * P request builder.
     *
     * @param name  the name
     * @param value the value
     * @return the request builder
     */
    public RequestBuilder p(String name, Object value) {
        ps.add(new Param(name, value));
        return this;
    }

    /**
     * Format string.
     *
     * @param charset the charset
     * @return the string
     */
    public String format(String charset) {
        return format(ps, charset);
    }

    /**
     * Format string.
     *
     * @param ps      the ps
     * @param charset the charset
     * @return the string
     */
    public static String format(List<Param> ps, String charset) {
        try {
            final StringBuilder result = new StringBuilder();
            for (Param p : ps) {
                final String encodedName = URLEncoder.encode(p.name, charset);
                final String encodedValue = URLEncoder.encode(p.value, charset);
                if (result.length() > 0) {
                    result.append('&');
                }
                result.append(encodedName);
                if (encodedValue != null) {
                    result.append('=').append(encodedValue);
                }
            }
            return result.toString();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Format string.
     *
     * @return the string
     */
    public String format() {
        return format(CommonConstants.CHARTSET_UTF8);
    }

    /**
     * From request builder.
     *
     * @param query the query
     * @return the request builder
     */
    public static RequestBuilder from(String query) {
        return new RequestBuilder(parse(query));
    }

    private static List<Param> parse(String query) {
        query = REG_UNMATCHED_PERCENTAGE.matcher(query).replaceAll("%25");
        String[] qs = query.split("&", -1);
        List<Param> list = new ArrayList<>(qs.length);
        for (String s : qs) {
            int ei = s.indexOf('=');
            String n, v = null;
            if (ei == -1) {
                n = s;
            } else {
                n = s.substring(0, ei);
                v = s.substring(ei + 1);
            }
            try {
                list.add(new Param(URLDecoder.decode(n, CommonConstants.CHARTSET_UTF8), v == null ?
                        null : URLDecoder.decode(v, CommonConstants.CHARTSET_UTF8)));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        return list;
    }

    /**
     * Build lr url string.
     *
     * @param url the url
     * @return the string
     */
    public static String buildLrUrl(String url) {
        return url.concat("?").concat(new RequestBuilder()
                .p(KeyConstants.Request.KEY_API_VERSION, CommonConstants.API_VERSION)
                .p(KeyConstants.Request.KEY_PLATFORM, CommonConstants.PLAT_FORM_ANDROID)
                .p(KeyConstants.Request.KEY_SDK_VERSION, CommonConstants.SDK_VERSION_NAME)
                .format());
    }

    /**
     * Build ic url string.
     *
     * @param url the url
     * @return the string
     */
    public static String buildIcUrl(String url) {
        return url.concat("?").concat(new RequestBuilder()
                .p(KeyConstants.Request.KEY_API_VERSION, CommonConstants.API_VERSION)
                .p(KeyConstants.Request.KEY_PLATFORM, CommonConstants.PLAT_FORM_ANDROID)
                .p(KeyConstants.Request.KEY_SDK_VERSION, CommonConstants.SDK_VERSION_NAME)
                .format());
    }

    /**
     * Build init url string.
     *
     * @param appKey the app key
     * @return the string
     */
    public static String buildInitUrl(String appKey) {
        return CommonConstants.INIT_URL.concat("?").concat(new RequestBuilder()
                .p(KeyConstants.Request.KEY_API_VERSION, CommonConstants.API_VERSION)
                .p(KeyConstants.Request.KEY_PLATFORM, CommonConstants.PLAT_FORM_ANDROID)
                .p(KeyConstants.Request.KEY_SDK_VERSION, CommonConstants.SDK_VERSION_NAME)
                .p(KeyConstants.Request.KEY_APP_KEY, appKey).format());
    }

    /**
     * Build wf url string.
     *
     * @param url the url
     * @return the string
     */
    public static String buildWfUrl(String url) {
        return url.concat("?").concat(new RequestBuilder()
                .p(KeyConstants.Request.KEY_API_VERSION, CommonConstants.API_VERSION)
                .p(KeyConstants.Request.KEY_PLATFORM, CommonConstants.PLAT_FORM_ANDROID)
                .p(KeyConstants.Request.KEY_SDK_VERSION, CommonConstants.SDK_VERSION_NAME)
                .format());
    }

    /**
     * Build iap url string.
     *
     * @param url the url
     * @return the string
     */
    public static String buildIapUrl(String url) {
        return url.concat("?").concat(new RequestBuilder()
                .p(KeyConstants.Request.KEY_API_VERSION, CommonConstants.API_VERSION)
                .p(KeyConstants.Request.KEY_PLATFORM, CommonConstants.PLAT_FORM_ANDROID)
                .p(KeyConstants.Request.KEY_SDK_VERSION, CommonConstants.SDK_VERSION_NAME)
                .p(KeyConstants.Request.KEY_APP_KEY, DataCache.getInstance().get(KeyConstants.KEY_APP_KEY, String.class))
                .format());
    }

    /**
     * Build event url string.
     *
     * @param eventUrl the event url
     * @return the string
     */
    public static String buildEventUrl(String eventUrl) {
        return eventUrl.concat("?").concat(new RequestBuilder()
                .p(KeyConstants.Request.KEY_API_VERSION, CommonConstants.API_VERSION)
                .p(KeyConstants.Request.KEY_PLATFORM, CommonConstants.PLAT_FORM_ANDROID)
                .p(KeyConstants.Request.KEY_SDK_VERSION, CommonConstants.SDK_VERSION_NAME)
                .p(KeyConstants.Request.KEY_APP_KEY, DataCache.getInstance().get(KeyConstants.KEY_APP_KEY, String.class))
                .format());
    }

    /**
     * Build hb url string.
     *
     * @param url the url
     * @return the string
     */
    public static String buildHbUrl(String url) {
        return url.concat("?").concat(new RequestBuilder()
                .p(KeyConstants.Request.KEY_API_VERSION, CommonConstants.API_VERSION)
                .p(KeyConstants.Request.KEY_PLATFORM, CommonConstants.PLAT_FORM_ANDROID)
                .p(KeyConstants.Request.KEY_SDK_VERSION, CommonConstants.SDK_VERSION_NAME)
                .format());
    }

    /**
     * Build lr request body byte [ ].
     *
     * @param extras the extras
     * @return the byte [ ]
     * @throws Exception the exception
     */
    public static byte[] buildLrRequestBody(int... extras) throws Exception {
        JSONObject jsonObject = getRequestBodyBaseJson();
        JsonUtil.put(jsonObject, KeyConstants.RequestBody.KEY_PID, extras[0]);
        JsonUtil.put(jsonObject, KeyConstants.RequestBody.KEY_SCENE, extras[1]);
        JsonUtil.put(jsonObject, KeyConstants.RequestBody.KEY_ACT, extras[2]);
        JsonUtil.put(jsonObject, KeyConstants.RequestBody.KEY_MID, extras[3]);
        JsonUtil.put(jsonObject, KeyConstants.RequestBody.KEY_IID, extras[4]);
        JsonUtil.put(jsonObject, "abt", extras[5]);
        JsonUtil.put(jsonObject, KeyConstants.RequestBody.KEY_TYPE, extras[6]);
        DeveloperLog.LogD("lr params:" + jsonObject.toString());
        return Gzip.inGZip(jsonObject.toString().getBytes(Charset.forName(CommonConstants.CHARTSET_UTF8)));
    }

    /**
     * Build ic request body byte [ ].
     *
     * @param extras the extras
     * @return the byte [ ]
     */
    public static byte[] buildIcRequestBody(Object... extras) {
        Map<String, Object> params = new HashMap<>();
        params.put("pid", extras[0]);
        params.put("mid", extras[1]);
        params.put("iid", extras[2]);
        params.put("scene", extras[3]);
        params.put("content", extras[4]);
        return Gzip.inGZip(new JSONObject(params).toString().getBytes(Charset.forName(CommonConstants.CHARTSET_UTF8)));
    }

    /**
     * All keys must present! Values can be null
     *
     * @param adapters the adapters
     * @return the byte [ ]
     * @throws Exception the exception
     */
    public static byte[] buildConfigRequestBody(JSONArray adapters) throws Exception {
        Context context = AdtUtil.getApplication();
        JSONObject body = getRequestBodyBaseJson();
        body.put(KeyConstants.RequestBody.KEY_W, DensityUtil.getPhoneWidth(context));
        body.put(KeyConstants.RequestBody.KEY_H, DensityUtil.getPhoneHeight(context));
        body.put(KeyConstants.RequestBody.KEY_TZ, DeviceUtil.getTimeZone());
        body.put(KeyConstants.RequestBody.KEY_LANG_NAME, DeviceUtil.getLocaleInfo().get(KeyConstants.RequestBody.KEY_LANG_NAME));
        body.put(KeyConstants.RequestBody.KEY_BUILD, Build.DISPLAY);
        body.put(KeyConstants.RequestBody.KEY_LIP, DeviceUtil.getHostIp());
        body.put(KeyConstants.RequestBody.KEY_ADNS, adapters);
        body.put(KeyConstants.RequestBody.KEY_BTIME, DeviceUtil.getBtime());
        body.put(KeyConstants.RequestBody.KEY_RAM, DeviceUtil.getTotalRAM(context));
        body.put(KeyConstants.RequestBody.KEY_ANDROID, buildAndroidRequestBody(context));

        DeveloperLog.LogD("init params:" + body.toString());

        return Gzip.inGZip(body.toString().getBytes(Charset.forName(CommonConstants.CHARTSET_UTF8)));
    }

    private static JSONObject getRequestBodyBaseJson() throws Exception {
        JSONObject body = new JSONObject();
        Map<String, Object> map = DeviceUtil.getLocaleInfo();
        Context context = AdtUtil.getApplication();
        body.put(KeyConstants.RequestBody.KEY_TS, System.currentTimeMillis());
        body.put(KeyConstants.RequestBody.KEY_FLT, DeviceUtil.getFlt());
        body.put(KeyConstants.RequestBody.KEY_FIT, DeviceUtil.getFit());
        body.put(KeyConstants.RequestBody.KEY_ZO, DeviceUtil.getTimeZoneOffset());
        body.put(KeyConstants.RequestBody.KEY_SESSION, DeviceUtil.getSessionId());
        body.put(KeyConstants.RequestBody.KEY_UID, DeviceUtil.getUid());
        body.put(KeyConstants.RequestBody.KEY_MDID, MDIDHandler.getMdid());
        body.put(KeyConstants.RequestBody.KEY_DID, MDIDHandler.getAndroidId());
        body.put(KeyConstants.RequestBody.KEY_DTYPE, 2);
        body.put(KeyConstants.RequestBody.KEY_JB, DeviceUtil.isRoot() ? 1 : 0);
        body.put(KeyConstants.RequestBody.KEY_LANG, map.get(KeyConstants.RequestBody.KEY_LANG));
        body.put(KeyConstants.RequestBody.KEY_LCOUNTRY  , map.get(KeyConstants.RequestBody.KEY_LCOUNTRY));
        body.put(KeyConstants.RequestBody.KEY_BUNDLE, context != null ? context.getPackageName() : "");
        body.put(KeyConstants.RequestBody.KEY_MAKE, Build.MANUFACTURER);
        body.put(KeyConstants.RequestBody.KEY_BRAND, Build.BRAND);
        body.put(KeyConstants.RequestBody.KEY_MODEL, Build.MODEL);
        body.put(KeyConstants.RequestBody.KEY_OSV, Build.VERSION.RELEASE);
        body.put(KeyConstants.RequestBody.KEY_APPV, DeviceUtil.getVersionName(context));
        body.put(KeyConstants.RequestBody.KEY_CONT, NetworkChecker.getConnectType(context));
        body.put(KeyConstants.RequestBody.KEY_CARRIER, NetworkChecker.getNetworkOperator(context));
        body.put(KeyConstants.RequestBody.KEY_FM, DeviceUtil.getFm());
        Map<String, Integer> battery = DeviceUtil.getBatteryInfo(context);
        if (battery == null || battery.isEmpty()) {
            body.put(KeyConstants.RequestBody.KEY_BATTERY, 0);
        } else {
            for (Map.Entry<String, Integer> integerEntry : battery.entrySet()) {
                if (integerEntry == null) {
                    continue;
                }

                body.put(integerEntry.getKey(), integerEntry.getValue());
            }

            if (!body.has(KeyConstants.RequestBody.KEY_BATTERY)) {
                body.put(KeyConstants.RequestBody.KEY_BATTERY, 0);
            }
        }
        return body;
    }

    /**
     * Build wf request body byte [ ].
     *
     * @param responses the responses
     * @param extras    the extras
     * @return the byte [ ]
     * @throws Exception the exception
     */
    public static byte[] buildWfRequestBody(List<AdTimingBidResponse> responses, String... extras) throws Exception {
        JSONObject body = getRequestBodyBaseJson();
        body.put(KeyConstants.RequestBody.KEY_PID, extras[0]);
        body.put(KeyConstants.RequestBody.KEY_W, extras[1]);
        body.put(KeyConstants.RequestBody.KEY_H, extras[2]);
        body.put(KeyConstants.RequestBody.KEY_IAP, extras[5]);
        body.put("act", extras[3]);
        body.put(KeyConstants.RequestBody.KEY_IMPRTIMES, Integer.valueOf(extras[4]));
        if (responses != null && !responses.isEmpty()) {
            JSONArray array = new JSONArray();
            for (AdTimingBidResponse response : responses) {
                if (response == null) {
                    continue;
                }
                JSONObject object = new JSONObject();
                JsonUtil.put(object, "iid", response.getIid());
                JsonUtil.put(object, "price", response.getPrice());
                JsonUtil.put(object, "cur", response.getCur());
                array.put(object);
            }
            body.put("bid", array);
        }
        DeveloperLog.LogD("request wf params : " + body.toString());
        return Gzip.inGZip(body.toString().getBytes(Charset.forName(CommonConstants.CHARTSET_UTF8)));
    }

    /**
     * Build iap request body byte [ ].
     *
     * @param extras the extras
     * @return the byte [ ]
     * @throws Exception the exception
     */
    public static byte[] buildIapRequestBody(String... extras) throws Exception {
        JSONObject body = getRequestBodyBaseJson();
        body.put("cur", extras[0]);
        body.put("iap", extras[1]);
        body.put("iapt", extras[2]);

        DeveloperLog.LogD("iap params : " + body.toString());
        return Gzip.inGZip(body.toString().getBytes(Charset.forName(CommonConstants.CHARTSET_UTF8)));
    }


    /**
     * Build event request body byte [ ].
     *
     * @param events the events
     * @return the byte [ ]
     * @throws Exception the exception
     */
    public static byte[] buildEventRequestBody(ConcurrentLinkedQueue<Event> events) throws Exception {
        JSONObject body = getRequestBodyBaseJson();
        body.put(KeyConstants.RequestBody.KEY_APPK, DataCache.getInstance().get(KeyConstants.KEY_APP_KEY, String.class));
        JSONArray jsonEvents = new JSONArray();
        for (Event e : events) {
            jsonEvents.put(e.toJSONObject());
        }
        body.put("events", jsonEvents);
        DeveloperLog.LogD("event report params : " + body.toString());
        return Gzip.inGZip(body.toString().getBytes(Charset.forName(CommonConstants.CHARTSET_UTF8)));
    }

    /**
     * Build error request body byte [ ].
     *
     * @param extras the extras
     * @return the byte [ ]
     * @throws Exception the exception
     */
    public static byte[] buildErrorRequestBody(String... extras) throws Exception {
        JSONObject body = getRequestBodyBaseJson();
        body.put("tag", extras[0]);
        body.put("error", extras[1]);
        return Gzip.inGZip(body.toString().getBytes(Charset.forName(CommonConstants.CHARTSET_UTF8)));
    }

    /**
     * Build hb request body byte [ ].
     *
     * @param extras the extras
     * @return the byte [ ]
     * @throws Exception the exception
     */
    public static byte[] buildHbRequestBody(Object... extras) throws Exception {
        JSONObject body = getRequestBodyBaseJson();
        body.put(KeyConstants.RequestBody.KEY_PID, extras[0]);
        body.put(KeyConstants.RequestBody.KEY_IAP, extras[1]);
        body.put(KeyConstants.RequestBody.KEY_IMPRTIMES, extras[2]);
        body.put("act", extras[3]);
        return Gzip.inGZip(body.toString().getBytes(Charset.forName(CommonConstants.CHARTSET_UTF8)));
    }

    private static JSONObject buildAndroidRequestBody(Context context) throws Exception {
        JSONObject androidBody = new JSONObject();
        androidBody.put(KeyConstants.Android.KEY_DEVICE, Build.DEVICE);
        androidBody.put(KeyConstants.Android.KEY_PRODUCE, Build.PRODUCT);
        androidBody.put(KeyConstants.Android.KEY_SD, DensityUtil.getScreenDensity());
        androidBody.put(KeyConstants.Android.KEY_SS, DensityUtil.getScreenSize());
        androidBody.put(KeyConstants.Android.KEY_CPU_ABI, DeviceUtil.getSystemProperties(KeyConstants.Device.KEY_RO_CPU_ABI));
        androidBody.put(KeyConstants.Android.KEY_CPU_ABI2, DeviceUtil.getSystemProperties(KeyConstants.Device.KEY_RO_CPU_ABI2));
        androidBody.put(KeyConstants.Android.KEY_CPU_ABI_LIST, DeviceUtil.getSystemProperties(KeyConstants.Device.KEY_RO_CPU_ABI_LIST));
        androidBody.put(KeyConstants.Android.KEY_CPU_ABI_LIST_32, DeviceUtil.getSystemProperties(KeyConstants.Device.KEY_RO_CPU_ABI_LIST_32));
        androidBody.put(KeyConstants.Android.KEY_CPU_ABI_LIST_64, DeviceUtil.getSystemProperties(KeyConstants.Device.KEY_RO_CPU_ABI_LIST_64));
        androidBody.put(KeyConstants.Android.KEY_API_LEVEL, Build.VERSION.SDK_INT);
        androidBody.put(KeyConstants.Android.KEY_D_DPI, DensityUtil.getDensityDpi(context));
        androidBody.put(KeyConstants.Android.KEY_DIM_SIZE, DensityUtil.getDimSize());
        androidBody.put(KeyConstants.Android.KEY_XDP, Integer.toString(DensityUtil.getXdpi(context)));
        androidBody.put(KeyConstants.Android.KEY_YDP, Integer.toString(DensityUtil.getYdpi(context)));
        androidBody.put(KeyConstants.Android.KEY_DFPID, DeviceUtil.getUniquePsuedoId());
        androidBody.put(KeyConstants.Android.KEY_TIME_ZONE, DeviceUtil.getTimeZone());
        androidBody.put(KeyConstants.Android.KEY_ARCH, DeviceUtil.getSystemProperties(KeyConstants.Device.KEY_RO_ARCH));
        androidBody.put(KeyConstants.Android.KEY_CHIPNAME, DeviceUtil.getSystemProperties(KeyConstants.Device.KEY_RO_CHIPNAME));
        androidBody.put(KeyConstants.Android.KEY_BRIDGE, DeviceUtil.getSystemProperties(KeyConstants.Device.KEY_RO_BRIDGE));
        androidBody.put(KeyConstants.Android.KEY_NATIVE_BRIDGE, DeviceUtil.getSystemProperties(KeyConstants.Device.KEY_NATIVE_BRIDGE));
        androidBody.put(KeyConstants.Android.KEY_BRIDGE_EXEC, DeviceUtil.getSystemProperties(KeyConstants.Device.KEY_RO_BRIDGE_EXEC));
        androidBody.put(KeyConstants.Android.KEY_ISA_X86_FEATURES, DeviceUtil.getSystemProperties(KeyConstants.Device.KEY_ISA_X86_FEATURES));
        androidBody.put(KeyConstants.Android.KEY_ISA_X86_VARIANT, DeviceUtil.getSystemProperties(KeyConstants.Device.KEY_ISA_X86_VARIANT));
        androidBody.put(KeyConstants.Android.KEY_ZYGOTE, DeviceUtil.getSystemProperties(KeyConstants.Device.KEY_RO_ZYGOTE));
        androidBody.put(KeyConstants.Android.KEY_MOCK_LOCATION, DeviceUtil.getSystemProperties(KeyConstants.Device.KEY_RO_MOCK_LOCATION));
        androidBody.put(KeyConstants.Android.KEY_ISA_ARM, DeviceUtil.getSystemProperties(KeyConstants.Device.KEY_RO_ISA_ARM));
        androidBody.put(KeyConstants.Android.KEY_ISA_ARM_FEATURES, DeviceUtil.getSystemProperties(KeyConstants.Device.KEY_ISA_ARM_FEATURES));
        androidBody.put(KeyConstants.Android.KEY_ISA_ARM_VARIANT, DeviceUtil.getSystemProperties(KeyConstants.Device.KEY_ISA_ARM_VARIANT));
        androidBody.put(KeyConstants.Android.KEY_ISA_ARM64_FEATURES, DeviceUtil.getSystemProperties(KeyConstants.Device.KEY_ISA_ARM64_FEATURES));
        androidBody.put(KeyConstants.Android.KEY_ISA_ARM64_VARIANT, DeviceUtil.getSystemProperties(KeyConstants.Device.KEY_ISA_ARM64_VARIANT));
        androidBody.put(KeyConstants.Android.KEY_BUILD_USER, DeviceUtil.getSystemProperties(KeyConstants.Device.KEY_RO_BUILD_USER));
        androidBody.put(KeyConstants.Android.KEY_KERNEL_QEMU, DeviceUtil.getSystemProperties(KeyConstants.Device.KEY_RO_KERNEL_QEMU));
        androidBody.put(KeyConstants.Android.KEY_HARDWARE, DeviceUtil.getSystemProperties(KeyConstants.Device.KEY_RO_HARDWARE));

        JSONArray sensorArray = SensorManager.getSingleton().getSensorData();
        androidBody.put(KeyConstants.Android.KEY_SENSOR_SIZE, sensorArray != null ? sensorArray.length() : 0);
        androidBody.put(KeyConstants.Android.KEY_SENSORS, sensorArray);

        androidBody.put(KeyConstants.Android.KEY_AS, DeviceUtil.getInstallVending(context));
        androidBody.put(KeyConstants.Android.KEY_FB_ID, DeviceUtil.getFacebookId(context));
        androidBody.put(KeyConstants.RequestBody.KEY_TDM, DeviceUtil.disk());
        return androidBody;
    }
}
