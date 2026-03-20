package com.demo.butler_voice_app.voice

import okhttp3.*
import org.json.JSONObject
import java.io.File

class SarvamSTT(private val apiKey: String) {

    private val client = OkHttpClient()

    fun transcribe(audioFile: File, callback: (String?) -> Unit) {

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", audioFile.name,
                RequestBody.create("audio/wav".toMediaTypeOrNull(), audioFile))
            .build()

        val request = Request.Builder()
            .url("https://api.sarvam.ai/speech-to-text")
            .post(requestBody)
            .addHeader("Authorization", "Bearer $apiKey")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                val json = JSONObject(response.body?.string() ?: "{}")
                val text = json.optString("transcript")
                callback(text)
            }
        })
    }
}