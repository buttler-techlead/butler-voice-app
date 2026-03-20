package com.demo.butler_voice_app.voice

import ai.picovoice.porcupine.*
import android.content.Context

class WakeWordManager(
    context: Context,
    private val onWake: () -> Unit
) {

    private val porcupineManager = PorcupineManager.Builder()
        .setAccessKey("EX1Uf3pS1ekA7wReSzb3BaOCHyXmFdGg4ZHCMwRMceqUbsnAD31btw==")
        .setKeyword(Porcupine.BuiltInKeyword.PORCUPINE) // ✅ FIXED
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