package com.example.android_remotecontrol_service.utils

import android.app.Activity
import android.content.Context
import android.util.DisplayMetrics


class DeviceSizeUtil {
    fun getDeviceWidth(context: Context): Int {
        val metrics: DisplayMetrics = context.resources.displayMetrics
        return metrics.widthPixels
    }

    fun getDeviceHeight(context: Context): Int {
        val metrics: DisplayMetrics = context.resources.displayMetrics
        return metrics.heightPixels
    }

    private fun getStatusBarHeight(context: Context): Int {
        var result = 0
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    private fun getNavigationBarHeight(context: Context): Int {
        val metrics = DisplayMetrics()
        (context as Activity).windowManager.defaultDisplay.getMetrics(metrics)
        val usableHeight = metrics.heightPixels
        context.windowManager.defaultDisplay.getRealMetrics(metrics)
        val realHeight = metrics.heightPixels
        return if (realHeight > usableHeight) realHeight - usableHeight else 0
        return 0
    }
}