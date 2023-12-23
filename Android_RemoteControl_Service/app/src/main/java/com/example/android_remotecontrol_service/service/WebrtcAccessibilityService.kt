package com.example.android_remotecontrol_service.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.provider.Settings
import android.text.TextUtils.SimpleStringSplitter
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.android_remotecontrol_service.ui.CustomDialog

class WebrtcAccessibilityService : AccessibilityService() {

    private var intentFilter = IntentFilter()
    private var myBroadcastReceiver: WebrtcBroadcastReceiver? = null

    override fun onCreate() {
        super.onCreate()

        myBroadcastReceiver = WebrtcBroadcastReceiver(this)
        intentFilter.addAction("startBroadcastReceiver")
        intentFilter.addAction("sendXandYCoordination")
        ContextCompat.registerReceiver(
            this,
            myBroadcastReceiver,
            intentFilter,
            ContextCompat.RECEIVER_EXPORTED
        )


        val intent = Intent("startBroadcastReceiver")
        this.sendBroadcast(intent)


        Log.d("WebrtcAccessibilityService", "onCreate : ")

    }

    override fun onServiceConnected() {
        super.onServiceConnected()

        val info = AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_VISUAL
        info.notificationTimeout = 1000
        this.serviceInfo = info
        Log.d("WebrtcAccessibilityService", "onServiceConnected : ")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }


    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val nodeInfo = event.source ?: return

        // 뒤는 원하는 대로...
        val id = nodeInfo.viewIdResourceName // View의 ID를 취득
        val packageName = event.packageName.toString() // View의 ID를 취득
        val resourceName = nodeInfo.viewIdResourceName
        val className = nodeInfo.className.toString() // Class명을 취득
        val eventType = AccessibilityEvent.eventTypeToString(event.eventType)
        Log.d("onAccessibilityEvent", "id : $id")
        Log.d("onAccessibilityEvent", "packageName : $packageName")
        Log.d("onAccessibilityEvent", "resourceName : $resourceName")
        Log.d("onAccessibilityEvent", "className : $className")
        Log.d("onAccessibilityEvent", "eventType : $eventType")
    }


    override fun onInterrupt() {
        // 서비스 중단 시 호출되는 로직
        Log.d("WebrtcAccessibilityService", "onInterrupt: something went wrong")
    }

    override fun onDestroy() {
        Log.d("WebrtcAccessibilityService", "WebRtcCobrowseAccessibilityService 종료됨")
        unregisterReceiver(myBroadcastReceiver)
    }

    companion object {
        var isBroadCastReceiverRegistered = false

        private fun isEnabled(context: Context): Boolean {
            try {
                val info = context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SERVICES
                )

                if (info.services == null) {
                    return false
                }

                val var2 = info.services
                val var3 = var2.size
                for (var4 in 0 until var3) {
                    val service = var2[var4]
                    if (service.name == WebrtcAccessibilityService::class.java.name) {
                        return service.permission == "android.permission.BIND_ACCESSIBILITY_SERVICE"
                    }
                }
            } catch (var6: PackageManager.NameNotFoundException) {
                Log.i("WebrtcAccessibilityService", "Failed to read the app package info $var6")
            }
            return false
        }

        fun isRunning(context: Context): Boolean {
            val componentName = ComponentName(context, WebrtcAccessibilityService::class.java)
            val enabledServicesSetting =
                Settings.Secure.getString(context.contentResolver, "enabled_accessibility_services")
            return if (enabledServicesSetting == null) {
                false
            } else {
                val colonSplitter = SimpleStringSplitter(':')
                colonSplitter.setString(enabledServicesSetting)
                var enabledService: ComponentName?
                do {
                    if (!colonSplitter.hasNext()) {
                        return false
                    }
                    val componentNameString = colonSplitter.next()
                    enabledService = ComponentName.unflattenFromString(componentNameString)
                } while (enabledService == null || enabledService != componentName)
                true
            }
        }

        fun showSetup(mContext: Context, mActivity: Activity, customDialog: CustomDialog): Boolean {
            if (isEnabled(mContext)) {
                if (!isRunning(mContext)) {
                    // if accessibility service is not enabled yet
                    mActivity.runOnUiThread {
                        customDialog.show()
                    }
                    return false

                } else {
                    // if accessibility service is already enabled
                    Log.i(
                        "WebrtcAccessibilityService",
                        "Accessibility service is enabled and running"
                    )

                    mActivity.runOnUiThread {
                        Toast.makeText(
                            mContext,
                            "Already Permission is allowed. You don't need to do anything to enable accessibiltiy Service",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return true
                }
            } else {
                // if accessibility service is already enabled

                Log.i("WebrtcAccessibilityService", "Accessibility service is enabled and running")
                mActivity.runOnUiThread {
                    Toast.makeText(
                        mContext,
                        "Already Permission is allowed. You don't need to do anything to enable accessibiltiy Service",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return true
            }
        }
    }


}
