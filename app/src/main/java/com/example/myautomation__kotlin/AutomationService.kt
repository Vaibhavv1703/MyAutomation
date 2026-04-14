package com.example.myautomation__kotlin

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.VibratorManager
import android.hardware.camera2.CameraManager
import android.os.VibrationEffect
import android.os.Vibrator
import kotlin.math.abs

class AutomationService : Service() {
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private lateinit var vibrator: Vibrator

    private var lastShakeTime = 0L
    private var isFlashOn = false

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        startForegroundNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(VibratorManager::class.java)
            vibrator = vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        accelerometer?.let {
            sensorManager.registerListener(
                sensorListener,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
        val broadcastIntent = Intent("SERVICE_STARTED")
        sendBroadcast(broadcastIntent)

        return START_STICKY
    }

    private fun startForegroundNotification() {
        val channelId = "automation_service"
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            channelId,
            "Automation Service",
            NotificationManager.IMPORTANCE_LOW
        )
        manager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("MyAutomation Running")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .build()

        startForeground(1, notification)
    }

    private val sensorListener = object : SensorEventListener {
        private var lastX = 0f
        private var directionChanges = 0
        private var lastDirectionChangeTime = 0L
        private var lastDeltaSign = 0

        override fun onSensorChanged(event: SensorEvent) {
            val x = event.values[0]
            val currentTime = System.currentTimeMillis()

            val delta = x - lastX

            if (abs(delta) > 10) {
                val currentSign = if (delta > 0) 1 else -1

                if (lastDeltaSign != 0 && currentSign != lastDeltaSign) {
                    // Genuine reversal — phone went one way then came back
                    if (currentTime - lastDirectionChangeTime < 400) {
                        directionChanges++
                    } else {
                        directionChanges = 1
                    }
                    lastDirectionChangeTime = currentTime
                }

                lastDeltaSign = currentSign
                lastX = x
            }

            if (directionChanges >= 5 && currentTime - lastShakeTime > 1000) {
                lastShakeTime = currentTime
                directionChanges = 0
                toggleFlashlight()
                vibrate()
            }

            if (currentTime - lastDirectionChangeTime > 500) {
                directionChanges = 0
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(sensorListener)
    }
    private fun toggleFlashlight() {
        try {
            val cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList[0]

            isFlashOn = !isFlashOn
            cameraManager.setTorchMode(cameraId, isFlashOn)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun vibrate() {
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    300,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        }
    }
}