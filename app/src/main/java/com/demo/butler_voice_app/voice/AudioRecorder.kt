package com.demo.butler_voice_app.voice

import android.media.*
import java.io.File
import java.io.FileOutputStream

class AudioRecorder(private val outputFile: File) {

    private val sampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    private var recorder: AudioRecord? = null
    private var isRecording = false

    fun startRecording(durationMs: Int = 4000) {

        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

        recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        )

        val buffer = ByteArray(bufferSize)

        recorder?.startRecording()
        isRecording = true

        val outputStream = FileOutputStream(outputFile)

        val startTime = System.currentTimeMillis()

        while (isRecording && System.currentTimeMillis() - startTime < durationMs) {
            val read = recorder!!.read(buffer, 0, buffer.size)
            if (read > 0) {
                outputStream.write(buffer, 0, read)
            }
        }

        recorder?.stop()
        recorder?.release()
        recorder = null

        outputStream.close()
    }
}