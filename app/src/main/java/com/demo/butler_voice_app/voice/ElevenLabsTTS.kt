package com.demo.butler_voice_app.voice

import android.content.Context
import android.media.MediaPlayer
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ElevenLabsTTS(
    private val apiKey: String,
    private val context: Context
) {

    private val client = OkHttpClient()

    fun speak(text: String, onDone: () -> Unit) {

        val url = "https://api.elevenlabs.io/v1/text-to-speech/LcfcDJNUP1GQjkzn1xUU"

        val json = """
        {
          "text": "$text",
          "model_id": "eleven_monolingual_v1"
        }
        """.trimIndent()

        val body = json.toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("xi-api-key", apiKey)
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {

                val audioBytes = response.body?.bytes()

                if (audioBytes == null) {
                    return
                }

                val file = File(context.cacheDir, "tts.mp3")
                val fos = FileOutputStream(file)
                fos.write(audioBytes)
                fos.close()

                val player = MediaPlayer()
                player.setDataSource(file.absolutePath)
                player.prepare()
                player.start()

                player.setOnCompletionListener {
                    player.release()
                    onDone()
                }
            }
        })
    }
}