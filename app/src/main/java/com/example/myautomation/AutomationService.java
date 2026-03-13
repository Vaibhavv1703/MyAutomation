package com.example.myautomation;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraManager;
import android.os.Vibrator;
import android.os.VibrationEffect;

public class AutomationService extends Service {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Vibrator vibrator;

    private boolean isFlashOn = false;
    private long lastShakeTime = 0;

    public AutomationService() {}

    private void startForegroundServiceNotification() {

        String channelId = "automation_service";

        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel channel = new NotificationChannel(
                channelId,
                "Automation Service",
                NotificationManager.IMPORTANCE_LOW
        );
        manager.createNotificationChannel(channel);

        Notification notification =
                new androidx.core.app.NotificationCompat.Builder(this, channelId)
                        .setContentTitle("MyAutomation running")
                        .setContentText("Womp Womp Nigga")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setOngoing(true)
                        .build();

        startForeground(1, notification);
    }

    private final SensorEventListener sensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float acceleration = (float) Math.sqrt(x * x + y * y + z * z);
            long currentTime = System.currentTimeMillis();

            if (acceleration > 45 && currentTime - lastShakeTime > 1000) {
                lastShakeTime = currentTime;
                toggleFlashlight();
                vibrate();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForegroundServiceNotification();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (accelerometer != null) {
            sensorManager.registerListener(
                    sensorListener,
                    accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL
            );
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sensorManager != null) {
            sensorManager.unregisterListener(sensorListener);
        }
    }

    private void toggleFlashlight() {
        try {
            CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            String cameraId = cameraManager.getCameraIdList()[0];

            if (!isFlashOn) {
                cameraManager.setTorchMode(cameraId, true);
                isFlashOn = true;
            } else {
                cameraManager.setTorchMode(cameraId, false);
                isFlashOn = false;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void vibrate() {
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(
                    VibrationEffect.createOneShot(
                            300,
                            VibrationEffect.DEFAULT_AMPLITUDE
                    )
            );
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }
}