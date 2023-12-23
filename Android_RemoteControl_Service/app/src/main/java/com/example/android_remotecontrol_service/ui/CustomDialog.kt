package com.example.android_remotecontrol_service.ui

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import com.example.android_remotecontrol_service.databinding.ActivityCustomDialogBinding

class CustomDialog(private val mContext: Context) : Dialog(mContext) {

    private lateinit var binding: ActivityCustomDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // make background transparently
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding = ActivityCustomDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            acceptBtn.setOnClickListener {
                openAccessibilitySettings()
            }
            rejectBtn.setOnClickListener {
                dismiss()
            }
        }
    }

    // let user enable to use accessibility service
    private fun openAccessibilitySettings() {
        val intent = Intent("android.settings.ACCESSIBILITY_SETTINGS")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        mContext.startActivity(intent)
    }
}
