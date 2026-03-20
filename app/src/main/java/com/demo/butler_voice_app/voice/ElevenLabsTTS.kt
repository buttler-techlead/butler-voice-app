package com.demo.butler_voice_app.voice

import okhttp3.*
import java.io.File
import java.io.FileOutputStream

class ElevenLabsTTS(
    private val apiKey: String,
    private val context: Context
) {

    private val client = OkHttpClient()

    fun speak(text: String, onDone: () -> Unit) {

        val url = "https://api.elevenlabs.io/v1/text-to-speech/YOUR_VOICE_ID"

        val json = """
        {
          "text": "$text",
          "model_id": "eleven_monolingual_v1"
        }
        """.trimIndent()

        val request = Request.Builder()
            .url(url)
            .post(RequestBody.create("application/json".toMediaTypeOrNull(), json))
            .addHeader("xi-api-key", apiKey)
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {}

            override fun onResponse(call: Call, response: Response) {

                val file = File(context.cacheDir, "tts.mp3")
                val fos = FileOutputStream(file)
                fos.write(response.body!!.bytes())
                fos.close()

                val player = android.media.MediaPlayer()
                player.setDataSource(file.absolutePath)
                player.prepare()
                player.start()

                player.setOnCompletionListener {
                    onDone()
                }
            }
        })
    }
}