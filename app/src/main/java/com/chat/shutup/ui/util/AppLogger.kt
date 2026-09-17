package com.chat.shutup.ui.util

import android.util.Log
import com.chat.shutup.BuildConfig

object AppLogger {
    private const val TAG = "ShutUpLog"

    fun d(message: String, tag: String = TAG) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message)
        }
    }

    fun e(message: String, throwable: Throwable? = null, tag: String = TAG) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, message, throwable)
        } else {
            // In release, we only log high-level errors without full stack trace to standard log
            // Ideally, this should also send to Crashlytics
            Log.e(tag, message)
        }
    }

    fun i(message: String, tag: String = TAG) {
        Log.i(tag, message)
    }
}
