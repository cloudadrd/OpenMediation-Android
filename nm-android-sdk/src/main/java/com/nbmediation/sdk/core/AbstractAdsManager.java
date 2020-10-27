// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.core;

import android.app.Activity;

import com.nbmediation.sdk.InitCallback;
import com.nbmediation.sdk.bid.AdTimingAuctionManager;
import com.nbmediation.sdk.bid.AdTimingBidResponse;
import com.nbmediation.sdk.bid.AuctionCallback;
import com.nbmediation.sdk.bid.AuctionUtil;
import com.nbmediation.sdk.bid.BidLoseReason;
import com.nbmediation.sdk.core.runnable.AdsScheduleTask;
import com.nbmediation.sdk.mediation.CustomAdsAdapter;
import com.nbmediation.sdk.utils.ActLifecycle;
import com.nbmediation.sdk.utils.AdRateUtil;
import com.nbmediation.sdk.utils.AdapterUtil;
import com.nbmediation.sdk.utils.AdsUtil;
import com.nbmediation.sdk.utils.DeveloperLog;
import com.nbmediation.sdk.utils.HandlerUtil;
import com.nbmediation.sdk.utils.IOUtil;
import com.nbmediation.sdk.utils.InsUtil;
import com.nbmediation.sdk.utils.PlacementUtils;
import com.nbmediation.sdk.utils.Preconditions;
import com.nbmediation.sdk.utils.SceneUtil;
import com.nbmediation.sdk.utils.WorkExecutor;
import com.nbmediation.sdk.utils.constant.CommonConstants;
import com.nbmediation.sdk.utils.crash.CrashUtil;
import com.nbmediation.sdk.utils.device.DeviceUtil;
import com.nbmediation.sdk.utils.error.Error;
import com.nbmediation.sdk.utils.error.ErrorBuilder;
import com.nbmediation.sdk.utils.error.ErrorCode;
import com.nbmediation.sdk.utils.event.EventId;
import com.nbmediation.sdk.utils.event.EventUploadManager;
import com.nbmediation.sdk.utils.helper.HbHelper;
import com.nbmediation.sdk.utils.helper.LrReportHelper;
import com.nbmediation.sdk.utils.helper.WaterFallHelper;
import com.nbmediation.sdk.utils.model.BaseInstance;
import com.nbmediation.sdk.utils.model.Instance;
import com.nbmediation.sdk.utils.model.Placement;
import com.nbmediation.sdk.utils.model.Scene;
import com.nbmediation.sdk.utils.request.network.Request;
import com.nbmediation.sdk.utils.request.network.Response;
import com.nbmediation.sdk.utils.request.network.util.NetworkChecker;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The type Abstract ads manager.
 */
public abstract class AbstractAdsManager extends AdsApi implements InitCallback,
        Request.OnRequestCallback, HbHelper.OnHbCallback {
    /**
     * The M activity.
     */
    protected Activity mActivity;
    /**
     * The M placement.
     */
    protected Placement mPlacement;
    /**
     * The M listener wrapper.
     */
    protected ListenerWrapper mListenerWrapper;
    /**
     * The Is in showing progress.
     */
    protected boolean isInShowingProgress;
    /**
     * The M scene.
     */
    protected Scene mScene;
    //    private List<AdTimingBidResponse> mBidResponses;
    private Map<Integer, AdTimingBidResponse> mS2sBidResponses;


    private NmManager.LOAD_TYPE mLoadType;
    //Adapters to be loaded
    private CopyOnWriteArrayList<Instance> mTotalIns;
    //
    private int mCacheSize;
    //
    private boolean isInLoadingProgress;
    //
    private boolean isManualTriggered;

    //last callback's status
    private AtomicBoolean mLastAvailability = new AtomicBoolean(false);
    private AtomicBoolean mDidScheduleTaskStarted = new AtomicBoolean(false);
    private AtomicBoolean isAReadyReported = new AtomicBoolean(false);

    /**
     * Instantiates a new Abstract ads manager.
     */
    public AbstractAdsManager() {
        mTotalIns = new CopyOnWriteArrayList<>();
        mListenerWrapper = new ListenerWrapper();
//        mBidResponses = new ArrayList<>();
    }

    /**
     * Ends this loading
     */
    @Override
    protected void callbackLoadError(Error error) {
        isInLoadingProgress = false;
        isManualTriggered = false;
    }

    @Override
    protected void callbackAvailableOnManual() {
        isManualTriggered = false;
    }

    @Override
    protected void callbackShowError(Error error) {
        isInShowingProgress = false;
    }

    /**
     * For an instance to load ads
     */
    @Override
    protected void loadInsAndSendEvent(Instance instance) {
        if (AdRateUtil.shouldBlockInstance(Preconditions.checkNotNull(mPlacement) ?
                mPlacement.getId() : "" + instance.getKey(), instance)) {
            instance.setMediationState(Instance.MEDIATION_STATE.CAPPED);
            onInsCapped(instance);
            return;
        }
        LrReportHelper.report(instance, mLoadType.getValue(), mPlacement.getWfAbt(), CommonConstants.INSTANCE_LOAD);
        instance.reportInsLoad();
        if (mS2sBidResponses != null && mS2sBidResponses.containsKey(instance.getId())) {
            inLoadWithBid(instance, AuctionUtil.generateMapRequestData(mS2sBidResponses.get(instance.getId())));
//            AuctionUtil.removeBidResponse(mBidResponses, instance);
        } else {
            insLoad(instance);
        }
    }

    @Override
    public void onResume(Activity activity) {
        if (Preconditions.checkNotNull(activity)) {
            mActivity = activity;
            if (mTotalIns != null && !mTotalIns.isEmpty()) {
                for (Instance in : mTotalIns) {
                    in.onResume(activity);
                }
            }
        }
    }

    @Override
    public void onPause(Activity activity) {
        if (Preconditions.checkNotNull(activity)) {
            mActivity = activity;
            if (mTotalIns != null && !mTotalIns.isEmpty()) {
                for (Instance in : mTotalIns) {
                    in.onPause(activity);
                }
            }
        }
    }

    @Override
    protected void setCurrentPlacement(Placement placement) {
        if (Preconditions.checkNotNull(placement)) {
            mPlacement = placement;
            mCacheSize = placement.getCs();
            mListenerWrapper.setPlacementId(placement.getId());
        }
    }

    /**
     * before load starts, checks: init---frequency control---show in progress---trigger type
     * When manually triggered, first checks available ads, and replenishes if necessary before checking if loading is in progress
     * Tiggers other than Manual are automatically called by the SDK,
     *
     * @param type load triggered by: Manual,Init,AdClose,Interval
     */
    @Override
    protected void loadAdWithAction(NmManager.LOAD_TYPE type) {
        DeveloperLog.LogD("loadAdWithAction : " + mPlacement + " action: " + type.toString());

        int availableCount = InsUtil.instanceCount(mTotalIns, Instance.MEDIATION_STATE.AVAILABLE);

        //if load is manually triggered
        if (type == NmManager.LOAD_TYPE.MANUAL) {
            isManualTriggered = true;
            //only checks ScheduleTask when manually triggered
            checkScheduleTaskStarted();
        } else {
            String pid = mPlacement != null ? mPlacement.getId() : "";
            reportEvent(EventId.ATTEMPT_TO_BRING_NEW_FEED, PlacementUtils.placementEventParams(pid));
            if (availableCount > 0) {
                reportEvent(EventId.AVAILABLE_FROM_CACHE, PlacementUtils.placementEventParams(pid));
            }
        }

        //returns if load can't start
        Error error = checkLoadAvailable();
        if (error != null) {
            AdsUtil.loadBlockedReport(Preconditions.checkNotNull(mPlacement) ? mPlacement.getId() : "", error);
            return;
        }

        //When manually triggered, first checks available ads in cache
        if (isManualTriggered) {
            if (hasAvailableCache() && shouldNotifyAvailableChanged(true)) {
                callbackAvailableOnManual();
            }
        }

        //to replenish?
        if (availableCount < mCacheSize) {
            delayLoad(type);
        } else {
            DeveloperLog.LogD("cache is full, cancel this request");
            error = ErrorBuilder.build(ErrorCode.CODE_LOAD_INVALID_REQUEST
                    , "cache is full, cancel this request", ErrorCode.CODE_INTERNAL_UNKNOWN_OTHER);
            AdsUtil.loadBlockedReport(Preconditions.checkNotNull(mPlacement) ? mPlacement.getId() : "", error);
        }
    }

    @Override
    protected void showAd(String scene) {
        Error error = checkShowAvailable(scene);
        if (Preconditions.checkNotNull(error)) {
            callbackShowError(error);
            return;
        }

        if (AdRateUtil.shouldBlockScene(mPlacement.getId(), mScene)) {
            error = ErrorBuilder.build(ErrorCode.CODE_SHOW_SCENE_CAPPED
                    , ErrorCode.MSG_SHOW_SCENE_CAPPED, -1);
            callbackShowError(error);
            return;
        }
        for (Instance in : mTotalIns) {
            if (!isInsAvailable(in)) {
                resetMediationStateAndNotifyLose(in);
                continue;
            }
            insShow(in);
            notifyInsBidWin(in);
            return;
        }
        error = ErrorBuilder.build(ErrorCode.CODE_SHOW_NO_AD_READY
                , ErrorCode.MSG_SHOW_NO_AD_READY, -1);
        DeveloperLog.LogE(error.toString());
        callbackShowError(error);
    }

    @Override
    protected boolean isPlacementAvailable() {
        if (isInShowingProgress || !Preconditions.checkNotNull(mPlacement) || mTotalIns == null || mTotalIns.isEmpty()) {
            return false;
        }

        for (Instance in : mTotalIns) {
            if (!isInsAvailable(in)) {
                resetMediationStateAndNotifyLose(in);
                continue;
            }
            return true;
        }
        return super.isPlacementAvailable();
    }

    @Override
    protected boolean hasAvailableCache() {
        return InsUtil.instanceCount(mTotalIns, Instance.MEDIATION_STATE.AVAILABLE) > 0;
    }

    /**
     *
     */
    @Override
    protected void checkScheduleTaskStarted() {
        if (!mDidScheduleTaskStarted.get()) {
            scheduleLoadAdTask();
        }
    }

    /**
     * Notifies availability change when manually trigged, or cache availability changed
     */
    @Override
    protected boolean shouldNotifyAvailableChanged(boolean available) {
        if (isInShowingProgress) {
            DeveloperLog.LogD("shouldNotifyAvailableChanged : " + false + " because current is in showing");
            return false;
        }
        if ((isManualTriggered || mLastAvailability.get() != available)) {
            DeveloperLog.LogD("shouldNotifyAvailableChanged for placement: " + mPlacement + " " + true);
            isManualTriggered = false;
            mLastAvailability.set(available);
            return true;
        }
        DeveloperLog.LogD("shouldNotifyAvailableChanged for placement : " + mPlacement + " " + false);
        return super.shouldNotifyAvailableChanged(available);
    }

    @Override
    protected void onInsInitFailed(Instance instance, Error error) {
        notifyLoadFailedInsBidLose(instance);
        if (shouldFinishLoad()) {
            boolean hasCache = hasAvailableCache();
            if (isManualTriggered && !hasCache) {
                callbackLoadFailedOnManual(error);
            }
            if (!hasCache) {
                reportEvent(EventId.NO_MORE_OFFERS, AdsUtil.buildAbtReportData(mPlacement.getWfAbt(),
                        PlacementUtils.placementEventParams(mPlacement != null ? mPlacement.getId() : "")));
            }
            if (shouldNotifyAvailableChanged(hasCache)) {
                onAvailabilityChanged(hasCache, error);
            }
            notifyUnLoadInsBidLose();
        } else {
            initOrFetchNextAdapter();
        }
    }

    @Override
    protected synchronized void onInsReady(final Instance instance) {
        LrReportHelper.report(instance, mLoadType.getValue(), mPlacement.getWfAbt(), CommonConstants.INSTANCE_READY);
        if (!shouldFinishLoad()) {
            initOrFetchNextAdapter();
        } else {
            notifyUnLoadInsBidLose();
        }
        if (isManualTriggered) {
            callbackLoadSuccessOnManual();
        }
        if (shouldNotifyAvailableChanged(true)) {
            if (!isAReadyReported.get()) {
                isAReadyReported.set(true);
                LrReportHelper.report(instance.getPlacementId(), mLoadType.getValue(), mPlacement.getWfAbt(),
                        CommonConstants.WATERFALL_READY);
            }
            onAvailabilityChanged(true, null);
        }
    }

    @Override
    protected synchronized void onInsLoadFailed(Instance instance, Error error) {
        notifyLoadFailedInsBidLose(instance);
        if (shouldFinishLoad()) {
            boolean hasCache = hasAvailableCache();
            if (isManualTriggered && !hasCache) {
                callbackLoadFailedOnManual(error);
            }
            if (!hasCache) {
                reportEvent(EventId.NO_MORE_OFFERS, AdsUtil.buildAbtReportData(mPlacement.getWfAbt(),
                        PlacementUtils.placementEventParams(mPlacement != null ? mPlacement.getId() : "")));
            }
            if (shouldNotifyAvailableChanged(hasCache)) {
                DeveloperLog.LogD("onInsLoadFailed shouldFinishLoad shouldNotifyAvailableChanged " + hasCache);
                onAvailabilityChanged(hasCache, error);
            }

            notifyUnLoadInsBidLose();
        } else {
            initOrFetchNextAdapter();
        }
    }

    @Override
    protected void onInsOpen(final Instance instance) {
        LrReportHelper.report(instance, mScene != null ? mScene.getId() : -1, mLoadType.getValue(),
                CommonConstants.INSTANCE_IMPR);
        //if availability changed from false to true
        if (shouldNotifyAvailableChanged(false)) {
            onAvailabilityChanged(false, null);
        }
        isInShowingProgress = true;
    }

    @Override
    protected void onInsClick(Instance instance) {
        LrReportHelper.report(instance, mScene != null ? mScene.getId() : -1, mLoadType.getValue(),
                CommonConstants.INSTANCE_CLICK);
    }

    @Override
    protected void onInsClose() {
        isInShowingProgress = false;
        callbackAdClosed();
        boolean hasCache = hasAvailableCache();
        if (shouldNotifyAvailableChanged(hasCache)) {
            onAvailabilityChanged(hasCache, null);
        }
        checkShouldLoadsWhenClose();
    }

    @Override
    protected void onInsCapped(Instance instance) {
        Error error = ErrorBuilder.build(ErrorCode.CODE_LOAD_CAPPED, "load ad failed", -1);
        onInsLoadFailed(instance, error);
    }

    @Override
    public void onSuccess() {
        //only trigged by manual 
        delayLoad(NmManager.LOAD_TYPE.MANUAL);
    }

    @Override
    public void onError(Error result) {
        //
        callbackLoadError(result);
    }

    @Override
    public void onHbSuccess(int abt, BaseInstance[] instances) {
        try {
            mPlacement.setHbAbt(abt);
//        AdTimingAuctionManager.getInstance().bid(mActivity, mPlacement.getId(), instances, abt,
//                mPlacement.getT(), this);
            List<AdTimingBidResponse> tokens = AdTimingAuctionManager.getInstance().getBidToken(mActivity, instances);
            WaterFallHelper.wfRequest(getPlacementInfo(), mLoadType, null, tokens, this);
        } catch (Exception e) {
            CrashUtil.getSingleton().saveException(e);
        }
    }

    @Override
    public void onHbFailed(String error) {
        try {
            WaterFallHelper.wfRequest(getPlacementInfo(), mLoadType, null, null, this);
        } catch (Exception e) {
            Error adTimingError = ErrorBuilder.build(ErrorCode.CODE_LOAD_INVALID_REQUEST
                    , ErrorCode.MSG_LOAD_INVALID_REQUEST, ErrorCode.CODE_LOAD_UNKNOWN_INTERNAL_ERROR);
            callbackLoadError(adTimingError);
            DeveloperLog.LogD("load ad error", e);
            CrashUtil.getSingleton().saveException(e);
        }
    }

//    @Override
//    public void onBidComplete(List<AdTimingBidResponse> responses) {
//        try {
//            if (responses != null) {
//                mBidResponses.addAll(responses);
//            }
//            WaterFallHelper.wfRequest(getPlacementInfo(), mLoadType, responses, this);
//        } catch (Exception e) {
//            Error error = ErrorBuilder.build(ErrorCode.CODE_LOAD_INVALID_REQUEST
//                    , ErrorCode.MSG_LOAD_INVALID_REQUEST, ErrorCode.CODE_LOAD_UNKNOWN_INTERNAL_ERROR);
//            callbackLoadError(error);
//            DeveloperLog.LogD("load ad error", e);
//            CrashUtil.getSingleton().saveException(e);
//        }
//    }


    @Override
    public void onRequestSuccess(Response response) {
        try {
            if (!Preconditions.checkNotNull(response) || response.code() != HttpURLConnection.HTTP_OK) {
                Error error = ErrorBuilder.build(ErrorCode.CODE_LOAD_SERVER_ERROR
                        , ErrorCode.MSG_LOAD_SERVER_ERROR, ErrorCode.CODE_INTERNAL_SERVER_ERROR);
                DeveloperLog.LogE(error.toString() + ", request cl http code:"
                        + (response != null ? response.code() : "null") + ", placement:" + mPlacement);
                callbackLoadError(error);
                return;
            }

            //when not trigged by init, checks cache before aReady reporting
            if (mLoadType != NmManager.LOAD_TYPE.INIT) {
                int availableCount = InsUtil.instanceCount(mTotalIns, Instance.MEDIATION_STATE.AVAILABLE);
                if (availableCount > 0) {
                    isAReadyReported.set(true);
                    LrReportHelper.report(mPlacement.getId(), mLoadType.getValue(), mPlacement.getWfAbt(),
                            CommonConstants.WATERFALL_READY);
                }
            }

            List<Instance> lastAvailableIns = InsUtil.getInsWithStatus(mTotalIns, Instance.MEDIATION_STATE.AVAILABLE);
            JSONObject clInfo = new JSONObject(response.body().string());
            int code = clInfo.optInt("code");
            if (code != 0) {
                if (lastAvailableIns == null || lastAvailableIns.isEmpty()) {
                    String msg = clInfo.optString("msg");
                    Error error = new Error(ErrorCode.CODE_LOAD_NO_AVAILABLE_AD
                            , msg, ErrorCode.CODE_INTERNAL_SERVER_ERROR);
                    DeveloperLog.LogE(error.toString());
                    callbackLoadError(error);
                }
                return;
            }
            mPlacement.setWfAbt(clInfo.optInt("abt"));
            List<Instance> tmp = WaterFallHelper.getListInsResult(clInfo, mPlacement);
            if (tmp == null || tmp.isEmpty()) {
                if (lastAvailableIns == null || lastAvailableIns.isEmpty()) {
                    Error error = new Error(ErrorCode.CODE_LOAD_NO_AVAILABLE_AD
                            , ErrorCode.MSG_LOAD_NO_AVAILABLE_AD, ErrorCode.CODE_INTERNAL_SERVER_ERROR);
                    DeveloperLog.LogE(error.toString() + ", tmp:" + tmp + ", last:" + lastAvailableIns);
                    callbackLoadError(error);
                } else {
                    DeveloperLog.LogD("request cl success, but ins[] is empty, but has history");
                    isInLoadingProgress = false;
                }
            } else {
                if (lastAvailableIns != null && !lastAvailableIns.isEmpty()) {
                    InsUtil.reOrderIns(lastAvailableIns, tmp);
                }
                mTotalIns.clear();
                mTotalIns.addAll(tmp);
                InsUtil.resetInsStateOnClResponse(mTotalIns);
                DeveloperLog.LogD("TotalIns is : " + mTotalIns.toString());
                int availableCount = InsUtil.instanceCount(mTotalIns, Instance.MEDIATION_STATE.AVAILABLE);
                reSizeCacheSize();
                DeveloperLog.LogD("after cl, cache size is : " + mCacheSize);
                //if availableCount == mCacheSize, do not load any new instance
                if (availableCount == mCacheSize) {
                    DeveloperLog.LogD("no new ins should be loaded, current load progress finish");
                    isInLoadingProgress = false;
                } else {
                    Map<Integer, AdTimingBidResponse> bidResponseMap = WaterFallHelper.getS2sBidResponse(clInfo);
                    if (bidResponseMap != null && !bidResponseMap.isEmpty()) {
                        if (mS2sBidResponses == null) {
                            mS2sBidResponses = new HashMap<>();
                        }

                        mS2sBidResponses.putAll(bidResponseMap);
                    }
                    doLoadOnUiThread();
                }
            }
        } catch (Exception e) {
            Error error = ErrorBuilder.build(ErrorCode.CODE_LOAD_SERVER_ERROR
                    , ErrorCode.MSG_LOAD_SERVER_ERROR, ErrorCode.CODE_INTERNAL_UNKNOWN_OTHER);
            DeveloperLog.LogE(error.toString() + ", request cl success, but failed when parse response" +
                    ", Placement:" + mPlacement, e);
            CrashUtil.getSingleton().saveException(e);
            callbackLoadError(error);
        } finally {
            IOUtil.closeQuietly(response);
        }
    }

    @Override
    public void onRequestFailed(String error) {
        Error errorResult = ErrorBuilder.build(ErrorCode.CODE_LOAD_SERVER_ERROR
                , ErrorCode.MSG_LOAD_SERVER_ERROR, ErrorCode.CODE_INTERNAL_SERVER_FAILED);
        DeveloperLog.LogD(errorResult.toString() + ", request cl failed : " + errorResult + ", error" + error);
        callbackLoadError(errorResult);
    }

    public void loadAdWithInterval() {

        loadAdWithAction(NmManager.LOAD_TYPE.INTERVAL);
    }

    /**
     * schedules Load Ad Task
     */
    private void scheduleLoadAdTask() {
        if (mPlacement == null || mPlacement.getRf() <= 0) {
            return;
        }
        mDidScheduleTaskStarted.set(true);
        WorkExecutor.scheduleWithFixedDelay(new AdsScheduleTask(this), mPlacement.getRf(), mPlacement.getRf(),
                TimeUnit.SECONDS);
    }

    private void delayLoad(final NmManager.LOAD_TYPE type) {
        try {
            isInLoadingProgress = true;
            mLoadType = type;
            isAReadyReported.set(false);
//            mBidResponses.clear();
            if (mS2sBidResponses != null) {
                mS2sBidResponses.clear();
            }
            if (mPlacement.hasHb()) {
                HbHelper.executeHb(mPlacement, type, this);
            } else {
                WaterFallHelper.wfRequest(getPlacementInfo(), mLoadType, null, null, this);
            }
        } catch (Exception e) {
            Error error = ErrorBuilder.build(ErrorCode.CODE_LOAD_INVALID_REQUEST
                    , ErrorCode.MSG_LOAD_INVALID_REQUEST, ErrorCode.CODE_LOAD_UNKNOWN_INTERNAL_ERROR);
            callbackLoadError(error);
            DeveloperLog.LogD("load ad error", e);
            CrashUtil.getSingleton().saveException(e);
        }
    }

    /**
     * load can start if
     * 1.Activity available
     * 2.network available
     */
    private Error checkLoadAvailable() {
        //does nothing if Showing in Progress
        if (isInShowingProgress || isInLoadingProgress) {
            Error error = ErrorBuilder.build(ErrorCode.CODE_LOAD_INVALID_REQUEST
                    , ErrorCode.MSG_LOAD_INVALID_REQUEST, ErrorCode.CODE_INTERNAL_REQUEST_PLACEMENTID);
            DeveloperLog.LogD("loadAdWithAction: " + mPlacement + ", type:"
                    + mLoadType.toString() + " stopped," +
                    " cause current is in loading/showing progress");
            callbackLoadError(error);
            return error;
        }
        //activity available?
        if (!checkActRef()) {
            Error error = ErrorBuilder.build(ErrorCode.CODE_LOAD_INVALID_REQUEST
                    , ErrorCode.MSG_LOAD_INVALID_REQUEST, ErrorCode.CODE_INTERNAL_UNKNOWN_ACTIVITY);
            DeveloperLog.LogE(error.toString() + "load ad but activity is not available");
            callbackLoadError(error);
            return error;
        }

        //network available?
        if (!NetworkChecker.isAvailable(mActivity)) {
            Error error = ErrorBuilder.build(ErrorCode.CODE_LOAD_NETWORK_ERROR
                    , ErrorCode.MSG_LOAD_NETWORK_ERROR, -1);
            DeveloperLog.LogE("load ad network not available");
            callbackLoadError(error);
            return error;
        }

        if (!Preconditions.checkNotNull(mPlacement)) {
            Error error = ErrorBuilder.build(ErrorCode.CODE_LOAD_INVALID_REQUEST
                    , ErrorCode.MSG_LOAD_INVALID_REQUEST, ErrorCode.CODE_INTERNAL_REQUEST_PLACEMENTID);
            DeveloperLog.LogE(error.toString() + ", placement is null");
            callbackLoadError(error);
            return error;
        }

        if (AdRateUtil.shouldBlockPlacement(mPlacement)) {
            Error error = new Error(ErrorCode.CODE_LOAD_CAPPED
                    , ErrorCode.MSG_LOAD_CAPPED, -1);
            DeveloperLog.LogD(error.toString() + ", Placement :" + mPlacement.getId() + " is blocked");
            callbackLoadError(error);
            return error;
        }
        return null;
    }

    /**
     *
     */
    private void doLoadOnUiThread() {
        HandlerUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                initOrFetchNextAdapter();
            }
        });
    }

    /**
     * @return limit of loadable instances
     */
    private int getLoadLimit() {
        //compares with server issued max concurrent number
        return Math.min(mPlacement.getBs(), mCacheSize -
                InsUtil.instanceCount(mTotalIns, Instance.MEDIATION_STATE.AVAILABLE));
    }

    /**
     * Inits an adapter and loads. Skips if already in progress
     */
    private synchronized void initOrFetchNextAdapter() {
        int canLoadCount = 0;
        for (Instance instance : mTotalIns) {
            Instance.MEDIATION_STATE state = instance.getMediationState();
            if (state == Instance.MEDIATION_STATE.INIT_PENDING ||
                    state == Instance.MEDIATION_STATE.LOAD_PENDING) {
                ++canLoadCount;
            } else if (state == Instance.MEDIATION_STATE.NOT_INITIATED) {
                //inits first if not
                CustomAdsAdapter adsAdapter = AdapterUtil.getCustomAdsAdapter(instance.getMediationId());
                if (adsAdapter == null) {
                    instance.setMediationState(Instance.MEDIATION_STATE.INIT_FAILED);
                } else {
                    ++canLoadCount;
                    instance.setAdapter(adsAdapter);
                    initInsAndSendEvent(instance);
                }
            } else if (state == Instance.MEDIATION_STATE.INITIATED
                    || state == Instance.MEDIATION_STATE.NOT_AVAILABLE) {
                ++canLoadCount;
                this.loadInsAndSendEvent(instance);
            }

            if (canLoadCount >= getLoadLimit()) {
                return;
            }
        }
        //
        if (canLoadCount == 0) {
            Error error = ErrorBuilder.build(ErrorCode.CODE_LOAD_NO_AVAILABLE_AD
                    , ErrorCode.MSG_LOAD_NO_AVAILABLE_AD, -1);
            DeveloperLog.LogE(error.toString());
            boolean hasCache = hasAvailableCache();
            if (hasCache) {
                if (shouldNotifyAvailableChanged(hasCache)) {
                    onAvailabilityChanged(hasCache, error);
                }
            } else {
                callbackLoadError(error);
            }
        }
    }

    /**
     * Finishes load when ads count suffices or all instances have been loaded: sum of ready, initFailed,
     * loadFailed, Capped
     *
     * @return should finish load or not
     */
    private boolean shouldFinishLoad() {
        int readyCount = InsUtil.instanceCount(mTotalIns, Instance.MEDIATION_STATE.AVAILABLE);
        int allLoadedCount = InsUtil.instanceCount(mTotalIns, Instance.MEDIATION_STATE.AVAILABLE,
                Instance.MEDIATION_STATE.INIT_FAILED, Instance.MEDIATION_STATE.LOAD_FAILED,
                Instance.MEDIATION_STATE.CAPPED);
        if (readyCount >= mCacheSize || allLoadedCount == mTotalIns.size()) {
            DeveloperLog.LogD("full of cache or loaded all ins, current load is finished : " +
                    readyCount);
            isInLoadingProgress = false;
            return true;
        }
        return false;
    }

    /**
     * Called at ads close
     */
    private void checkShouldLoadsWhenClose() {
        loadAdWithAction(NmManager.LOAD_TYPE.CLOSE);
    }

    /**
     * @return activity's availability
     */
    private boolean checkActRef() {
        if (!DeviceUtil.isActivityAvailable(mActivity)) {
            Activity activity = ActLifecycle.getInstance().getActivity();
            if (activity == null) {
                return false;
            }
            mActivity = activity;
        }
        return true;
    }

    /**
     * showing is available if
     * 1.Activity is available
     * 2.init finished
     * 3.placement isn't null
     */
    private Error checkShowAvailable(String scene) {
        if (isInShowingProgress) {
            DeveloperLog.LogE("show ad failed,current is showing");
            return ErrorBuilder.build(-1, "show ad failed,current is showing", -1);
        }
        //Activity is available?
        if (!checkActRef()) {
            return ErrorBuilder.build(ErrorCode.CODE_SHOW_UNKNOWN_INTERNAL_ERROR
                    , ErrorCode.MSG_SHOW_UNKNOWN_INTERNAL_ERROR, ErrorCode.CODE_INTERNAL_UNKNOWN_ACTIVITY);
        }

        if (!Preconditions.checkNotNull(mPlacement)) {
            DeveloperLog.LogD("placement is null");
            return ErrorBuilder.build(ErrorCode.CODE_SHOW_INVALID_ARGUMENT
                    , ErrorCode.MSG_SHOW_INVALID_ARGUMENT, ErrorCode.CODE_INTERNAL_REQUEST_PLACEMENTID);
        }
        mScene = SceneUtil.getScene(mPlacement, scene);
        if (!Preconditions.checkNotNull(mScene)) {
            return ErrorBuilder.build(ErrorCode.CODE_SHOW_SCENE_NOT_FOUND
                    , ErrorCode.MSG_SHOW_SCENE_NOT_FOUND, -1);
        }
        return null;
    }

    /**
     * re-calculates cached ads count
     */
    private void reSizeCacheSize() {
        mCacheSize = Math.min(mCacheSize, mTotalIns.size());
        int size = mCacheSize;
        if (mPlacement != null) {
            size = mPlacement.getCs();
        }
        mCacheSize = Math.min(size, mTotalIns.size());
    }

    private void resetMediationStateAndNotifyLose(Instance instance) {
        if (instance.getMediationState() == Instance.MEDIATION_STATE.AVAILABLE) {
            instance.setMediationState(Instance.MEDIATION_STATE.NOT_AVAILABLE);
        }
        notifyUnShowedBidLose(instance);
    }

    private void notifyInsBidWin(BaseInstance instance) {
        if (mTotalIns == null || mS2sBidResponses == null || instance == null) {
            return;
        }
        if (!mS2sBidResponses.containsKey(instance.getId())) {
            return;
        }
        AdTimingBidResponse bidResponse = mS2sBidResponses.get(instance.getId());
        if (bidResponse == null) {
            return;
        }
        AuctionUtil.s2sNotifyBidWin(bidResponse.getNurl(), instance);
    }

    private void notifyUnShowedBidLose(BaseInstance instance) {
        if (mTotalIns == null || mS2sBidResponses == null || instance == null) {
            return;
        }
        if (!mS2sBidResponses.containsKey(instance.getId())) {
            return;
        }
        AuctionUtil.s2sNotifyBidLose(mS2sBidResponses.get(instance.getId()),
                BidLoseReason.INVENTORY_DID_NOT_MATERIALISE.getValue(), instance);
    }

    private void notifyLoadFailedInsBidLose(BaseInstance instance) {
        if (mTotalIns == null || mS2sBidResponses == null || instance == null) {
            return;
        }
        if (!mS2sBidResponses.containsKey(instance.getId())) {
            return;
        }
        AuctionUtil.s2sNotifyBidLose(mS2sBidResponses.get(instance.getId()),
                BidLoseReason.INTERNAL.getValue(), instance);
    }

    private void notifyUnLoadInsBidLose() {
        if (mTotalIns == null || mS2sBidResponses == null) {
            return;
        }
        Map<BaseInstance, AdTimingBidResponse> unLoadInsBidResponses = new HashMap<>();
        for (Instance in : mTotalIns) {
            if (in == null) {
                continue;
            }
            if ((in.getMediationState() == Instance.MEDIATION_STATE.NOT_INITIATED ||
                    in.getMediationState() == Instance.MEDIATION_STATE.NOT_AVAILABLE) &&
                    mS2sBidResponses.containsKey(in.getId())) {
                AdTimingBidResponse response = mS2sBidResponses.get(in.getId());
                if (response != null) {
                    unLoadInsBidResponses.put(in, response);
                }
            }
        }
        if (!unLoadInsBidResponses.isEmpty()) {
            AuctionUtil.s2sNotifyBidLose(unLoadInsBidResponses, BidLoseReason.LOST_TO_HIGHER_BIDDER.getValue());
        }
    }


    private void reportEvent(int eventId, JSONObject data) {
        EventUploadManager.getInstance().uploadEvent(eventId, data);
    }
}
