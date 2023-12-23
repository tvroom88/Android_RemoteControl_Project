package com.example.android_remotecontrol_service.utils

enum class DataModelType {
    SignIn, StartStreaming, EndCall, Offer, Answer, IceCandidates, RemoteControl, AccessibilityAccept, AccessibilityReject
}

data class DataModel(
    val type: DataModelType? = null,
    val username: String,
    val target: String? = null,
    val data: Any? = null
)
