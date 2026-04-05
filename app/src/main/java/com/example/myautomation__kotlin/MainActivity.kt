package com.example.myautomation__kotlin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val statusText = findViewById<TextView>(R.id.statusText)
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