package com.example.service

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import kotlin.random.Random

sealed class AudioRecordState {
    object Idle : AudioRecordState()
    data class Recording(
        val secondsRemaining: Int,
        val totalSeconds: Int = 60,
        val amplitudeLevel: Float = 0f,
        val audioFile: File? = null
    ) : AudioRecordState() {
        val secondsElapsed: Int get() = totalSeconds - secondsRemaining
        val amplitudeNormalized: Float get() = amplitudeLevel.coerceIn(0.1f, 1f)
    }
    object ProcessingAnalysis : AudioRecordState()
    data class Completed(
        val audioFile: File?,
        val recordedDurationSeconds: Int,
        val simulated: Boolean = false
    ) : AudioRecordState()
    data class Error(val message: String) : AudioRecordState()
}

class AudioSafetyRecorder(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val _recordingState = MutableStateFlow<AudioRecordState>(AudioRecordState.Idle)
    val recordingState: StateFlow<AudioRecordState> = _recordingState.asStateFlow()

    private var mediaRecorder: MediaRecorder? = null
    private var currentOutputFile: File? = null
    private var timerJob: Job? = null
    private var startTimeMillis: Long = 0L
    private var isRealRecording = false

    fun isRecording(): Boolean = _recordingState.value is AudioRecordState.Recording

    fun startOneMinuteEmergencyRecording(
        onComplete: (File?, Int, Boolean) -> Unit
    ) {
        if (isRecording()) return

        val hasRecordAudio = context.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED

        val targetFile = File(context.cacheDir, "emergency_sos_audio_${System.currentTimeMillis()}.m4a")
        currentOutputFile = targetFile
        startTimeMillis = System.currentTimeMillis()
        isRealRecording = false

        if (hasRecordAudio) {
            try {
                val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    MediaRecorder(context)
                } else {
                    @Suppress("DEPRECATION")
                    MediaRecorder()
                }
                recorder.apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setAudioEncodingBitRate(128000)
                    setAudioSamplingRate(44100)
                    setOutputFile(targetFile.absolutePath)
                    prepare()
                    start()
                }
                mediaRecorder = recorder
                isRealRecording = true
                Log.d("AudioSafetyRecorder", "Hardware microphone recording initialized: ${targetFile.name}")
            } catch (e: Exception) {
                Log.w("AudioSafetyRecorder", "Microphone init failed, falling back to simulated acoustic capture: ${e.message}")
                mediaRecorder = null
                isRealRecording = false
            }
        } else {
            Log.d("AudioSafetyRecorder", "Microphone permission not granted; running simulated acoustic telemetry.")
            isRealRecording = false
        }

        // Start 60-second countdown job with live amplitude monitoring
        timerJob?.cancel()
        timerJob = scope.launch(Dispatchers.Default) {
            val totalSeconds = 60
            for (remaining in totalSeconds downTo 1) {
                if (!isActive) break

                val amp = if (isRealRecording && mediaRecorder != null) {
                    try {
                        val maxAmp = mediaRecorder?.maxAmplitude ?: 0
                        (maxAmp.toFloat() / 32767f).coerceIn(0.05f, 1.0f)
                    } catch (_: Exception) {
                        Random.nextFloat() * 0.4f + 0.1f
                    }
                } else {
                    // Acoustic oscillation simulation for emulator or fallback
                    (0.2f + (Random.nextFloat() * 0.5f)).coerceIn(0.1f, 0.9f)
                }

                _recordingState.value = AudioRecordState.Recording(
                    secondsRemaining = remaining,
                    totalSeconds = totalSeconds,
                    amplitudeLevel = amp,
                    audioFile = targetFile
                )
                delay(1000L)
            }

            // Time expired at 60s
            finalizeRecording(onComplete)
        }
    }

    fun stopAndAnalyzeNow(onComplete: (File?, Int, Boolean) -> Unit) {
        timerJob?.cancel()
        finalizeRecording(onComplete)
    }

    fun cancelRecording() {
        timerJob?.cancel()
        cleanupRecorder()
        currentOutputFile?.delete()
        currentOutputFile = null
        _recordingState.value = AudioRecordState.Idle
    }

    private fun finalizeRecording(onComplete: (File?, Int, Boolean) -> Unit) {
        val durationSec = ((System.currentTimeMillis() - startTimeMillis) / 1000).toInt().coerceAtLeast(1)
        val recordedFile = currentOutputFile
        val wasSimulated = !isRealRecording

        cleanupRecorder()

        _recordingState.value = AudioRecordState.Completed(
            audioFile = recordedFile,
            recordedDurationSeconds = durationSec,
            simulated = wasSimulated
        )

        scope.launch(Dispatchers.Main) {
            onComplete(recordedFile, durationSec, wasSimulated)
        }
    }

    private fun cleanupRecorder() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            Log.w("AudioSafetyRecorder", "Error releasing MediaRecorder: ${e.message}")
        } finally {
            mediaRecorder = null
        }
    }
}
