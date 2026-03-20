package com.demo.butler_voice_app.voice

import ai.picovoice.porcupine.*
import android.content.Context

class WakeWordManager(
    context: Context,
    private val onWake: () -> Unit
) {

    private val porcupineManager = PorcupineManager.Builder()
        .setAccessKey("EX1Uf3pS1ekA7wReSzb3BaOCHyXmFdGg4ZHCMwRMceqUbsnAD31btw==")
        .setKeywordPath("hey_butler.ppn") // ✅ CUSTOM FILE
        .setSensitivity(0.7f)
        .build(context) { _ ->
            onWake()
        }

    fun start() {
        porcupineManager.start()
    }

    fun stop() {
        porcupineManager.stop()
    }

    fun release() {
        porcupineManager.delete()
    }
}