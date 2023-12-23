package com.example.android_remotecontrol_service.repository

import android.content.Intent
import android.util.Log
import com.example.android_remotecontrol_service.socket.SocketClient
import com.example.android_remotecontrol_service.utils.DataModel
import com.example.android_remotecontrol_service.utils.DataModelType.*
import com.example.android_remotecontrol_service.webrtc.MyPeerObserver
import com.example.android_remotecontrol_service.webrtc.WebrtcClient
import com.google.gson.Gson
import org.json.JSONObject
import org.webrtc.*
import java.nio.ByteBuffer
import javax.inject.Inject

class MainRepository @Inject constructor(
    private val socketClient: SocketClient,
    private val webrtcClient: WebrtcClient,
    private val gson: Gson
) : SocketClient.Listener, WebrtcClient.Listener, WebrtcClient.ReceiverListener {

    private lateinit var username: String
    private lateinit var target: String
    private var dataChannel: DataChannel? = null
    private lateinit var surfaceView: SurfaceViewRenderer
    var listener: Listener? = null

    fun init(username: String, surfaceView: SurfaceViewRenderer) {
        this.username = username
        this.surfaceView = surfaceView
        initSocket()
        initWebrtcClient()

    }

    private fun initSocket() {
        socketClient.listener = this
        socketClient.init(username)
    }

    fun setPermissionIntentToWebrtcClient(intent: Intent) {
        webrtcClient.setPermissionIntent(intent)
    }

    fun sendScreenShareConnection(target: String) {
        socketClient.sendMessageToSocket(
            DataModel(
                type = StartStreaming,
                username = username,
                target = target,
                null
            )
        )
    }

    fun sendRequestRemoteControlPermission(target: String) {
        socketClient.sendMessageToSocket(
            DataModel(
                type = RemoteControl,
                username = username,
                target = target,
                null
            )
        )
    }

    fun startScreenCapturing(surfaceView: SurfaceViewRenderer) {
        webrtcClient.startScreenCapturing(surfaceView)
    }

    fun startCall(target: String) {
        webrtcClient.call(target)
    }

    fun sendCallEndedToOtherPeer() {
        socketClient.sendMessageToSocket(
            DataModel(
                type = EndCall,
                username = username,
                target = target,
                null
            )
        )
    }

    fun sendAccessibilityPermissionStatus(status: Boolean, target: String) {
        val dataModel =
            if (status)
                DataModel(
                    type = AccessibilityAccept,
                    username = username,
                    target = target,
                    null
                ) else
                DataModel(
                    type = AccessibilityReject,
                    username = username,
                    target = target,
                    null
                )

        socketClient.sendMessageToSocket(dataModel)
    }

    fun restartRepository() {
        webrtcClient.restart()
    }

    fun onDestroy() {
        socketClient.onDestroy()
        webrtcClient.closeConnection()
    }

    private val dataChannelObserver = object : DataChannel.Observer {
        override fun onBufferedAmountChange(p0: Long) {}
        override fun onStateChange() {}
        override fun onMessage(p0: DataChannel.Buffer?) {
            p0?.let { onDataReceived(it) }
        }
    }

    private fun initWebrtcClient() {
        webrtcClient.listener = this
        webrtcClient.initializeWebrtcClient(username, surfaceView, dataChannelObserver,
            object : MyPeerObserver() {
                override fun onIceCandidate(p0: IceCandidate?) {
                    super.onIceCandidate(p0)
                    p0?.let { webrtcClient.sendIceCandidate(it, target) }
                }

                override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
                    super.onConnectionChange(newState)
                    Log.d("TAG", "onConnectionChange: $newState")
                    if (newState == PeerConnection.PeerConnectionState.CONNECTED) {
                        listener?.onConnectionConnected()
                    }
                }

                override fun onAddStream(p0: MediaStream?) {
                    super.onAddStream(p0)
                    Log.d("TAG", "onAddStream: $p0")
                    p0?.let { listener?.onRemoteStreamAdded(it) }
                }

                override fun onDataChannel(p0: DataChannel?) {
                    super.onDataChannel(p0)
                    dataChannel = p0
                }
            })
    }

    override fun onNewMessageReceived(model: DataModel) {
        when (model.type) {
            StartStreaming -> {
                this.target = model.username
                //notify ui, conneciton request is being made, so show it
                listener?.onConnectionRequestReceived(model.username)
            }

            EndCall -> {
                //notify ui call is ended
                listener?.onCallEndReceived()
            }

            Offer -> {
                webrtcClient.onRemoteSessionReceived(
                    SessionDescription(
                        SessionDescription.Type.OFFER, model.data
                            .toString()
                    )
                )
                this.target = model.username
                webrtcClient.answer(target)
            }

            Answer -> {
                webrtcClient.onRemoteSessionReceived(
                    SessionDescription(SessionDescription.Type.ANSWER, model.data.toString())
                )

            }

            IceCandidates -> {
                val candidate = try {
                    gson.fromJson(model.data.toString(), IceCandidate::class.java)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
                candidate?.let {
                    webrtcClient.addIceCandidate(it)
                }
            }

            RemoteControl -> {
                listener?.openRequestRemoteControlPermissionView(target) //Caller
            }


            AccessibilityAccept -> {
                listener?.statusRemoteControlPermission(true)
            }

            else -> Unit
        }
    }

    override fun onTransferEventToSocket(data: DataModel) {
        socketClient.sendMessageToSocket(data)
    }

    override fun onDataReceived(it: DataChannel.Buffer) {
        listener?.onDataReceivedFromChannel(it)
    }

    interface Listener {
        fun onConnectionRequestReceived(target: String)
        fun onConnectionConnected()
        fun onCallEndReceived()
        fun onRemoteStreamAdded(stream: MediaStream)
        fun openRequestRemoteControlPermissionView(target: String) // request to user to allow to enable accessibility Service
        fun statusRemoteControlPermission(status: Boolean) // check whether caller allow remote control caller's phone
        fun onDataReceivedFromChannel(it: DataChannel.Buffer) // get X and Y coordinate data from callee
    }


    //Test용
    fun sendJsonData() {
        // 예제 사용
        val jsonExample = JSONObject().apply {
            put("key1", "value1")
            put("key2", 42)
        }

        // JSON 객체를 문자열로 변환
        val jsonString = jsonExample.toString()

        // 문자열을 바이트 배열로 변환
        val byteArray = jsonString.toByteArray(Charsets.UTF_8)

        // 바이트 배열을 ByteBuffer로 감싸기
        val buffer = ByteBuffer.wrap(byteArray)

        // DataChannel을 통해 바이트 배열 전송
        dataChannel?.send(DataChannel.Buffer(buffer, false))
    }


    fun sendJsonRatioData(jsonRatio: JSONObject) {

        // JSON 객체를 문자열로 변환
        val jsonString = jsonRatio.toString()

        // 문자열을 바이트 배열로 변환
        val byteArray = jsonString.toByteArray(Charsets.UTF_8)

        // 바이트 배열을 ByteBuffer로 감싸기
        val buffer = ByteBuffer.wrap(byteArray)

        // DataChannel을 통해 바이트 배열 전송
        dataChannel?.send(DataChannel.Buffer(buffer, false))
    }


}