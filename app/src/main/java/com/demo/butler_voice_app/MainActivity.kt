package com.demo.butler_voice_app

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.demo.butler_voice_app.api.ApiClient
import com.demo.butler_voice_app.core.*
import com.demo.butler_voice_app.ui.theme.ButlervoiceappTheme
import com.demo.butler_voice_app.voice.*
import java.io.File

class MainActivity : ComponentActivity() {

    private val apiClient = ApiClient()

    lateinit var conversationManager: ConversationManager
    lateinit var cartManager: CartManager

    lateinit var wakeWordManager: WakeWordManager
    lateinit var sarvamSTT: SarvamSTT
    lateinit var elevenTTS: ElevenLabsTTS

    private val recordRequestCode = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ButlervoiceappTheme {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🎤 Butler AI\nSay 'Butler'",
                        fontSize = 22.sp
                    )
                }
            }
        }

        cartManager = CartManager()

        conversationManager = ConversationManager(
            cartManager,
            ::speak,
            apiClient
        )

        // 🔊 AUDIO OUTPUT
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        audioManager.mode = AudioManager.MODE_NORMAL
        audioManager.isSpeakerphoneOn = true

        checkMicPermission()
    }

    // 🎤 PERMISSION
    private fun checkMicPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                recordRequestCode
            )
        } else {
            initVoiceSystem()
        }
    }

    // 🚀 INIT ALL VOICE SYSTEMS
    private fun initVoiceSystem() {

        // ✅ Sarvam STT
        sarvamSTT = SarvamSTT("sk_kklnpbrh_OVI7itj4WuvXVxsxG3nLsj5u")

        // ✅ ElevenLabs TTS
        elevenTTS = ElevenLabsTTS("dee5eee4f62699e04cb091b21abaa77ef671612610254ffef96e94576fc996f9", this)

        // ✅ Wake Word (Porcupine)
        wakeWordManager = WakeWordManager(this) {
            runOnUiThread {
                Log.d("WAKE", "Wake word detected")

                speak("Yes, I am your Butler. What would you like to order?")
            }
        }

        wakeWordManager.start()
    }

    // 🎤 START STT AFTER WAKE
    private fun startSTT() {

        val audioFile: File = recordAudio()

        sarvamSTT.transcribe(audioFile) { text ->

            runOnUiThread {

                if (text.isNullOrEmpty()) {
                    speak("Sorry, I didn't catch that")
                    return@runOnUiThread
                }

                Log.d("STT", "User said: $text")

                conversationManager.onUserInput(text)
            }
        }
    }

    // 🔊 SPEAK FUNCTION (ElevenLabs)
    private fun speak(text: String) {

        wakeWordManager.stop() // 🔴 stop wake listening

        elevenTTS.speak(text) {

            runOnUiThread {
                startSTT() // 🟢 listen after speaking
            }
        }
    }

    // 🎤 SIMPLE AUDIO RECORD (TEMP)
    private fun recordAudio(): File {

        val file = File(cacheDir, "recorded_audio.wav")

        val recorder = com.demo.butler_voice_app.voice.AudioRecorder(file)

        recorder.startRecording(4000) // 4 seconds

        return file
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == recordRequestCode &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            initVoiceSystem()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        wakeWordManager.stop()
        wakeWordManager.release()
    }
}