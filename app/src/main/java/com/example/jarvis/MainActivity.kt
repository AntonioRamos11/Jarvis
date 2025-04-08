package com.example.jarvis

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import com.example.jarvis.databinding.ActivityMainBinding
import android.Manifest
import android.content.pm.PackageManager

class MainActivity : AppCompatActivity() {
    private lateinit var wakeWordDetector: WakeWordDetector
    private lateinit var voiceResponseHelper: VoiceResponseHelper
    private lateinit var speechToTextHelper: SpeechToTextHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Request permissions
        requestPermissions()

        // Initialize components
        voiceResponseHelper = VoiceResponseHelper(this)
        speechToTextHelper = SpeechToTextHelper(this)

        // Initialize wake word detector
        wakeWordDetector = WakeWordDetector(this).apply {
            initialize {
                voiceResponseHelper.speak("Yes? How can I help?")
                speechToTextHelper.startListening()
            }
        }
    }

    private fun requestPermissions() {
        val requiredPermissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.INTERNET,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN
        )

        if (requiredPermissions.any { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }) {
            requestPermissions(requiredPermissions, 0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        wakeWordDetector.stopListening()
        voiceResponseHelper.shutdown()
    }
}
