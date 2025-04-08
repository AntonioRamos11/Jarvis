package com.example.jarvis
import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.IBinder
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AudioCaptureService : Service() {
    private var recorder: AudioRecord? = null
    private var isRecording = false
    private lateinit var speechToTextHelper: SpeechToTextHelper
    private lateinit var llmClient: LLMClient
    private lateinit var voiceResponseHelper: VoiceResponseHelper

    override fun onCreate() {
        super.onCreate()
        speechToTextHelper = SpeechToTextHelper(this)
        llmClient = LLMClient(this)
        voiceResponseHelper = VoiceResponseHelper(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startRecording()
        return START_STICKY
    }

    private fun startRecording() {
        val sampleRate = 16000 // 16kHz
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

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
        recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        )

        isRecording = true
        recorder?.startRecording()

        CoroutineScope(Dispatchers.IO).launch {
            val buffer = ShortArray(bufferSize)
            while (isRecording) {
                val bytesRead = recorder?.read(buffer, 0, bufferSize) ?: 0
                if (bytesRead > 0) {
                    processAudio(buffer, bytesRead)
                }
            }
        }
    }

    private fun processAudio(buffer: ShortArray, bytesRead: Int) {
        // For wake word detection or direct processing
        speechToTextHelper.processAudioChunk(buffer)
    }

    override fun onDestroy() {
        super.onDestroy()
        isRecording = false
        recorder?.stop()
        recorder?.release()
        recorder = null
    }

    override fun onBind(intent: Intent?): IBinder? = null
}