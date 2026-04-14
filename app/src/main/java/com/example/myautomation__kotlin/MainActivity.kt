package com.example.myautomation__kotlin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.Manifest
import androidx.activity.result.contract.ActivityResultContracts
import android.content.pm.PackageManager
import android.os.Build

class MainActivity : AppCompatActivity() {
    private lateinit var statusText: TextView

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
        val notifGranted = permissions[Manifest.permission.POST_NOTIFICATIONS] ?: false

        if (!cameraGranted || !notifGranted) {
            statusText.text = "Permissions denied — app won't work properly"
        }
    }

    private fun requestPermissions() {
        val permissions = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (permissions.isNotEmpty()) {
            requestPermissionLauncher.launch(permissions.toTypedArray())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        requestPermissions()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        val startButton = findViewById<Button>(R.id.startButton)
        val stopButton = findViewById<Button>(R.id.stopButton)

        startButton.setOnClickListener {
            val intent = Intent(this, AutomationService::class.java)
            ContextCompat.startForegroundService(this, intent)

            statusText.text = "Automation Started"
        }

        stopButton.setOnClickListener {
            val intent = Intent(this, AutomationService::class.java)
            stopService(intent)

            statusText.text = "Automation Stopped"
        }
    }
}