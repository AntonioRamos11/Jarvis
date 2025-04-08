package com.example.jarvis

import ai.picovoice.porcupine.Porcupine
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioRecord
import android.util.Log
import androidx.core.app.ActivityCompat

class WakeWordDetector(private val context: Context) {
    private var porcupine: Porcupine? = null
    private var audioRecord: AudioRecord? = null
    private var isListening = false

    fun initialize(callback: () -> Unit) {
        try {
            porcupine = Porcupine.Builder()
                .setKeywordPath("jarvis_android.ppn") // Place your wake word model in assets
                .setSensitivity(0.5f)
                .build(context)

            startListening(callback)
        } catch (e: Exception) {
            Log.e("WakeWordDetector", "Initialization failed", e)
        }
    }

    private fun startListening(callback: () -> Unit) {
        if (porcupine == null) return

        val audioData = ShortArray(porcupine!!.frameLength)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        audioRecord = AudioRecord(1
            MediaRecorder.AudioSource.MIC,
            porcupine!!.sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            porcupine!!.frameLength * 2
        )

        isListening = true
        audioRecord?.startRecording()

        CoroutineScope(Dispatchers.IO).launch {
            while (isListening) {
                audioRecord?.read(audioData, 0, audioData.size)?.let { read ->
                    if (read == audioData.size) {
                        try {
                            val keywordIndex = porcupine?.process(audioData)
                            if (keywordIndex != -1) {
                                withContext(Dispatchers.Main) {
                                    callback()
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("WakeWordDetector", "Processing error", e)
                        }
                    }
                }
            }
        }
    }

    fun stopListening() {
        isListening = false
        audioRecord?.stop()
        audioRecord?.release()
        porcupine?.delete()
    }
}