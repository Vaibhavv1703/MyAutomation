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
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter

class MainActivity : AppCompatActivity() {
    private lateinit var statusText: TextView

    private val serviceStartedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            statusText.text = getString(R.string.status_started)
        }
    }
    override fun onResume() {
        super.onResume()
        registerReceiver(
            serviceStartedReceiver,
            IntentFilter("SERVICE_STARTED"),
            RECEIVER_NOT_EXPORTED
        )
    }
    override fun onPause() {
        super.onPause()
        unregisterReceiver(serviceStartedReceiver)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
        val notifGranted = permissions[Manifest.permission.POST_NOTIFICATIONS] ?: false

        if (!cameraGranted || !notifGranted) {
            statusText.text = getString(R.string.status_denied)
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
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        val startButton = findViewById<Button>(R.id.startButton)
        val stopButton = findViewById<Button>(R.id.stopButton)

        requestPermissions()

        startButton.setOnClickListener {
            val intent = Intent(this, AutomationService::class.java)
            ContextCompat.startForegroundService(this, intent)

            statusText.text = getString(R.string.status_started)
        }

        stopButton.setOnClickListener {
            val intent = Intent(this, AutomationService::class.java)
            stopService(intent)

            statusText.text = getString(R.string.status_stopped)
        }
    }
}