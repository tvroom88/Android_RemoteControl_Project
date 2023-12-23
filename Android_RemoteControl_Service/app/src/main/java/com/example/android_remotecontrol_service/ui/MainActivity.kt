package com.example.android_remotecontrol_service.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.android_remotecontrol_service.databinding.ActivityMainBinding
import com.example.android_remotecontrol_service.repository.MainRepository
import com.example.android_remotecontrol_service.service.WebrtcAccessibilityService
import com.example.android_remotecontrol_service.service.WebrtcService
import com.example.android_remotecontrol_service.service.WebrtcServiceRepository
import com.example.android_remotecontrol_service.utils.DeviceSizeUtil
import com.example.android_remotecontrol_service.utils.RatioModel

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.hilt.android.AndroidEntryPoint
import org.webrtc.DataChannel
import org.webrtc.MediaStream
import org.webrtc.RendererCommon.ScalingType
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), MainRepository.Listener {

    private var username: String? = null
    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var webrtcServiceRepository: WebrtcServiceRepository

    @Inject
    lateinit var mainRepository: MainRepository

    private val capturePermissionRequestCode = 1

    private lateinit var customDialog: CustomDialog

    private lateinit var tempTarget: String

    private var isAllowedAccessibilityService: Boolean = false

    private var surfaceWidth = 0
    private var surfaceHeight = 0


    private var gson: Gson? = null

    private var screenWidthPixels = 0
    private var screenHeightPixels = 0

    private lateinit var deviceSizeUtil: DeviceSizeUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    override fun onResume() {
        super.onResume()
        if (customDialog.isShowing && WebrtcAccessibilityService.isRunning(this)) {
            customDialog.dismiss()
            webrtcServiceRepository.sendAccessibilityServiceStatusToServer(true, tempTarget)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun init() {
        username = intent.getStringExtra("username")
        if (username.isNullOrEmpty()) {
            finish()
        }

        deviceSizeUtil = DeviceSizeUtil()

        screenWidthPixels = deviceSizeUtil.getDeviceWidth(this)
        screenHeightPixels = deviceSizeUtil.getDeviceHeight(this)


        // to get Accessibility Service permission, first show requesting permission dialog
        customDialog = CustomDialog(this)


        gson = GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create()


        WebrtcService.surfaceView = binding.surfaceView
        WebrtcService.listener = this
        webrtcServiceRepository.startIntent(username!!)
        binding.requestBtn.setOnClickListener {
            startScreenCapture()
        }

        binding.surfaceView.setScalingType(ScalingType.SCALE_ASPECT_FILL)
        binding.surfaceView.requestLayout()


        // To get surfaceView width and height.
        val observer: ViewTreeObserver = binding.surfaceView.viewTreeObserver
        observer.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {

                surfaceWidth = binding.surfaceView.width
                surfaceHeight = binding.surfaceView.height
                binding.surfaceView.viewTreeObserver.removeGlobalOnLayoutListener(this)
            }
        })

        // To get touched position.
        binding.surfaceView.setOnTouchListener { _, event ->
            val action = event.action
            val curX = event.x // 눌린 곳의 X좌표
            val curY = event.y // 눌린 곳의 Y좌표

            when (action) {
                MotionEvent.ACTION_DOWN -> { // 처음 눌렸을 때
                    Log.d("setOnTouchListener", "손가락 눌림 : $curX, $curY")
                }

                MotionEvent.ACTION_MOVE -> { // 누르고 움직였을 때
                    Log.d("setOnTouchListener", "손가락 움직임 : $curX, $curY")
                }

                MotionEvent.ACTION_UP -> { // 누른 걸 뗐을 때
                    Log.d("setOnTouchListener", "손가락 뗌 : $curX, $curY")
                    Log.d("setOnTouchListener", "크기 너비 : $surfaceWidth, $surfaceHeight")


                    //After getting x, y position, send to caller through dataChannel
                    if (isAllowedAccessibilityService) {
                        val ratioX = curX / surfaceWidth.toFloat()
                        val ratioY = curY / surfaceHeight.toFloat()
                        webrtcServiceRepository.sendDataChannelMessage(ratioX, ratioY)
                    }
                }
            }
            true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode != capturePermissionRequestCode) return
        WebrtcService.screenPermissionIntent = data
        if(data != null){
            webrtcServiceRepository.requestConnection(
                binding.targetEt.text.toString()
            )
        }

    }

    private fun startScreenCapture() {
        val mediaProjectionManager = application.getSystemService(
            Context.MEDIA_PROJECTION_SERVICE
        ) as MediaProjectionManager

        startActivityForResult(
            mediaProjectionManager.createScreenCaptureIntent(), capturePermissionRequestCode
        )
    }

    override fun onConnectionRequestReceived(target: String) {
        runOnUiThread {
            binding.apply {
                notificationTitle.text = "$target is requesting for connection"
                notificationLayout.isVisible = true
                notificationAcceptBtn.setOnClickListener {
                    webrtcServiceRepository.acceptCAll(target)
                    notificationLayout.isVisible = false

                    // adding remoteControl button only for callee to request remote control to caller
                    setRemoteControlBtn(target)
                }
                notificationDeclineBtn.setOnClickListener {
                    notificationLayout.isVisible = false
                }
            }
        }
    }

    // adding remoteControl button only for callee to request remote control to caller
    private fun setRemoteControlBtn(target: String) {
        Log.d("JaehongLee", "setRemoteControlBtn")

        binding.remoteControl.apply {
            isVisible = true
            setOnClickListener {
                webrtcServiceRepository.sendRequestRemoteControlPermission(target)
            }
        }
    }

    override fun onConnectionConnected() {
        runOnUiThread {
            binding.apply {
                requestLayout.isVisible = false
                disconnectBtn.isVisible = true
                disconnectBtn.setOnClickListener {
                    webrtcServiceRepository.endCallIntent()
                    restartUi()
                }
            }
        }
    }

    override fun onCallEndReceived() {
        runOnUiThread {
            restartUi()
        }
    }

    override fun onRemoteStreamAdded(stream: MediaStream) {
        runOnUiThread {
            binding.surfaceView.isVisible = true
            stream.videoTracks[0].addSink(binding.surfaceView)
        }
    }

    //Before starting remote control, user should enable to use accessibility service
    override fun openRequestRemoteControlPermissionView(target: String) {
        tempTarget = target

        /**
         *  If Accessibility Service is already allowed, then just show toast message.
         *  otherwise, show dialog to get permission
         */
        if (WebrtcAccessibilityService.showSetup(this, this, customDialog)) {
            webrtcServiceRepository.sendAccessibilityServiceStatusToServer(true, target)
        }
    }

    // Callee check whether Caller allow or not remote control
    override fun statusRemoteControlPermission(status: Boolean) {
        isAllowedAccessibilityService = status
    }


    override fun onDataReceivedFromChannel(it: DataChannel.Buffer) {

        val data: ByteBuffer = it.data
        val decodedString = StandardCharsets.UTF_8.decode(data).toString()

        val ratioModel: RatioModel?
        try {
            ratioModel = gson?.fromJson(decodedString, RatioModel::class.java)

            ratioModel?.let {
                val intent = Intent("sendXandYCoordination")
                intent.putExtra("xRatio", ratioModel.xRatio * screenWidthPixels.toFloat())
                intent.putExtra("yRatio", ratioModel.yRatio * screenHeightPixels.toFloat())
                sendBroadcast(intent)
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "error : $e")
        }
    }

    private fun restartUi() {
        binding.apply {
            disconnectBtn.isVisible = false
            requestLayout.isVisible = true
            notificationLayout.isVisible = false
            surfaceView.isVisible = false
        }
    }
}