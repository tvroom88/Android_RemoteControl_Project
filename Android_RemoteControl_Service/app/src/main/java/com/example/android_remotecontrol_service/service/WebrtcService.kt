package com.example.android_remotecontrol_service.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.android_remotecontrol_service.R
import com.example.android_remotecontrol_service.repository.MainRepository

import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import org.webrtc.DataChannel
import org.webrtc.MediaStream
import org.webrtc.SurfaceViewRenderer
import javax.inject.Inject

@AndroidEntryPoint
class WebrtcService @Inject constructor() : Service(), MainRepository.Listener {

    companion object {
        var screenPermissionIntent: Intent? = null
        var surfaceView: SurfaceViewRenderer? = null
        var listener: MainRepository.Listener? = null
    }

    @Inject
    lateinit var mainRepository: MainRepository

    private lateinit var notificationManager: NotificationManager
    private lateinit var username: String


    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(
            NotificationManager::class.java
        )
        mainRepository.listener = this
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            when (intent.action) {
                "StartIntent" -> {
                    this.username = intent.getStringExtra("username").toString()
                    mainRepository.init(username, surfaceView!!)
                    startServiceWithNotification()
                }

                "StopIntent" -> {
                    stopMyService()
                }

                "STOP_ACTION" -> {
                    stopMyService()
                }

                "EndCallIntent" -> {
                    mainRepository.sendCallEndedToOtherPeer()
                    mainRepository.onDestroy()
                    stopMyService()
                }

                "AcceptCallIntent" -> {
                    val target = intent.getStringExtra("target")
                    target?.let {
                        mainRepository.startCall(it)
                    }
                }

                "RequestConnectionIntent" -> {
                    val target = intent.getStringExtra("target")
                    target?.let {
                        mainRepository.setPermissionIntentToWebrtcClient(screenPermissionIntent!!)
                        mainRepository.startScreenCapturing(surfaceView!!)
                        mainRepository.sendScreenShareConnection(it)
                    }
                }

                // Callee send request to Caller "Can I control your device remotely?"
                "RequestRemoteControlIntent" -> {
                    val target = intent.getStringExtra("target")
                    target?.let {
                        mainRepository.sendRequestRemoteControlPermission(target)
                    }
                }

                "accessibilityServiceStatusIntent" -> {
                    val status = intent.getBooleanExtra("status", false)
                    val target = intent.getStringExtra("target")

                    target?.let {
                        mainRepository.sendAccessibilityPermissionStatus(status, target)
                    }
                }

                // send X, Y coordinate through dataChannel
                "dataChannelMessage" -> {
                    mainRepository.sendJsonData()
                }

                "dataChannelMessage_Coordinate" -> {
                    Log.d("abcabc", "WebrtcService")
                    val xRatio = intent.getFloatExtra("xRatio", 0.0F)
                    val yRatio = intent.getFloatExtra("yRatio", 0.0F)
                    val ratioJson = JSONObject().apply {
                        put("xRatio", xRatio)
                        put("yRatio", yRatio)
                    }
                    mainRepository.sendJsonRatioData(ratioJson)

                }

            }
        }

        return START_STICKY
    }

    private fun stopMyService() {
        mainRepository.onDestroy()
        stopSelf()
        notificationManager.cancelAll()
    }

    private fun startServiceWithNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                "channel1", "foreground", NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(notificationChannel)


            val stopIntent = Intent(this, WebrtcService::class.java)
            stopIntent.action = "STOP_ACTION"
            val stopPendingIntent =
                PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_MUTABLE)

            val notification = NotificationCompat.Builder(this, "channel1")
                .setContentTitle("Foreground Service")
                .setContentText("Running")
                .setSmallIcon(R.drawable.ic_baseline_videocam_24)
                .addAction(R.drawable.ic_baseline_videocam_24, "End Session", stopPendingIntent)
                .build();

            startForeground(1, notification)
        }

    }


    override fun onConnectionRequestReceived(target: String) {
        listener?.onConnectionRequestReceived(target)
    }

    override fun onConnectionConnected() {
        listener?.onConnectionConnected()
    }

    override fun onCallEndReceived() {
        listener?.onCallEndReceived()
        stopMyService()
    }

    override fun onRemoteStreamAdded(stream: MediaStream) {
        listener?.onRemoteStreamAdded(stream)
    }

    override fun openRequestRemoteControlPermissionView(target: String) {
        listener?.openRequestRemoteControlPermissionView(target)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    override fun statusRemoteControlPermission(status: Boolean) {
        listener?.statusRemoteControlPermission(status)
    }

    override fun onDataReceivedFromChannel(it: DataChannel.Buffer) {
        listener?.onDataReceivedFromChannel(it)
    }

}