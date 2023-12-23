package com.example.android_remotecontrol_service.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

import com.example.android_remotecontrol_service.databinding.ActivityLoginBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private lateinit var views: ActivityLoginBinding

    private lateinit var requestNotificationPermission: ActivityResultLauncher<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        views = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(views.root)

        requestNotificationPermission =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    startActivity(
                        Intent(this, MainActivity::class.java).apply {
                            putExtra("username", views.usernameEt.text.toString())
                        }
                    )

                } else {
                    Toast.makeText(
                        applicationContext,
                        "Notification permission을 승낙해주세요..",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        views.enterBtn.setOnClickListener {
            if (views.usernameEt.text.isNullOrEmpty()) {
                Toast.makeText(this, "please fill the username", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (checkNotificationPermission()) {
                        startActivity(
                            Intent(this, MainActivity::class.java).apply {
                                putExtra("username", views.usernameEt.text.toString())
                            }
                        )
                    } else {
                        requestNotificationPermission.launch(permission)

                    }
                }else{
                    startActivity(
                        Intent(this, MainActivity::class.java).apply {
                            putExtra("username", views.usernameEt.text.toString())
                        }
                    )
                }
            }


        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun checkNotificationPermission(): Boolean {
        return when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(this, permission) -> {
                true
            }

            else -> {
                false
            }
        }
    }

    companion object {
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        val permission = Manifest.permission.POST_NOTIFICATIONS
    }

}