package com.xpfriend.tydrone.factory;

import android.util.Log;

import com.xpfriend.tydrone.core.Logger;

public class AndroidLogger extends Logger {
    public void logd(String tag, String msg) {
        Log.d(tag, msg);
    }

    public void logi(String tag, String msg) {
        Log.i(tag, msg);
    }

    public void logw(String tag, String msg) {
        Log.w(tag, msg);
    }

    public void logw(String tag, String msg, Throwable tr) {
        Log.w(tag, msg, tr);
    }

    public void loge(String tag, String msg) {
        Log.e(tag, msg);
    }

    public void loge(String tag, String msg, Throwable tr) {
        Log.e(tag, msg, tr);
    }
}
