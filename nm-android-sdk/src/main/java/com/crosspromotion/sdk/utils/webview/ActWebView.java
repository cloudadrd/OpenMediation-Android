// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.crosspromotion.sdk.utils.webview;

import android.content.Context;

import com.nbmediation.sdk.utils.AdtUtil;
import com.nbmediation.sdk.utils.DeveloperLog;
import com.nbmediation.sdk.utils.HandlerUtil;
import com.nbmediation.sdk.utils.crash.CrashUtil;

public final class ActWebView {

    private BaseWebView mActView;
    private boolean isDestroyed;

    private static final class ActWebViewHolder {
        private static ActWebView sInstance = new ActWebView();
    }

    private ActWebView() {
    }

    public static ActWebView getInstance() {
        return ActWebViewHolder.sInstance;
    }

    public void init(final Context context) {
        HandlerUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mActView == null || isDestroyed) {
                        mActView = new BaseWebView(context.getApplicationContext());
//                        mActView.loadUrl("about:blank");
                        isDestroyed = false;
                    }
                } catch (Throwable e) {
                    DeveloperLog.LogD("ActWebView", e);
                    CrashUtil.getSingleton().saveException(e);
                }
            }
        });
    }

    public BaseWebView getActView() {
        if (isDestroyed || mActView == null) {
            init(AdtUtil.getApplication());
            return mActView;
        }
        return mActView;
    }

    public void destroy(String jsName) {
        if (mActView == null) {
            return;
        }

        mActView.stopLoading();
        mActView.removeAllViews();
        mActView.clearHistory();
        mActView.removeJavascriptInterface(jsName);
        mActView.setWebViewClient(null);
        mActView.setWebChromeClient(null);
        mActView.freeMemory();
        isDestroyed = true;
    }
}
