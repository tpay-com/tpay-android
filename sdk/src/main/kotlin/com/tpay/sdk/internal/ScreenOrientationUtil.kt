package com.tpay.sdk.internal

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import com.tpay.sdk.extensions.runDelayedOnMainThread

object ScreenOrientationUtil {
    fun lock(activity: Activity) {
        activity.requestedOrientation =
            if (activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            } else {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
    }

    fun unlock(activity: Activity) {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }
}