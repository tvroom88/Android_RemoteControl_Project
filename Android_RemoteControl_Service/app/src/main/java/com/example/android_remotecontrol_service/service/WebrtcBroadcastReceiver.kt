package com.example.android_remotecontrol_service.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityService.GestureResultCallback
import android.accessibilityservice.GestureDescription
import android.accessibilityservice.GestureDescription.StrokeDescription
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Path
import android.util.Log

class WebrtcBroadcastReceiver(private val service: AccessibilityService) : BroadcastReceiver() {

    override fun onReceive(p0: Context?, intent: Intent?) {

        if (intent != null) {
            if (intent.action == "startBroadcastReceiver") {
                Log.d("WebrtcBroadcastReceiver", "Broadcast Receiver is start")
                WebrtcAccessibilityService.isBroadCastReceiverRegistered = true
            }

            if (intent.action == "sendXandYCoordination") {

                val x = intent.getFloatExtra("xRatio", 0F)
                val y = intent.getFloatExtra("yRatio", 0F)

                Log.d("WebrtcBroadcastReceiver", "x " + x)
                Log.d("WebrtcBroadcastReceiver", "y " + y)

                tap(x, y)
            }
        }

    }

    private fun tap(x: Float, y: Float) {
        Log.d("onReceive", "tap-method")
        val path = Path()
        path.moveTo(x, y)
        val stroke = StrokeDescription(path, 0, 100)
        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(stroke)
        val gesture = gestureBuilder.build()

        service.dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription) {
                super.onCompleted(gestureDescription)
                Log.d("abcabc", "Gesture completed")
            }

            override fun onCancelled(gestureDescription: GestureDescription) {
                super.onCancelled(gestureDescription)
                Log.d("abcabc", "Gesture cancelled")
            }
        }, null)
    }
}
