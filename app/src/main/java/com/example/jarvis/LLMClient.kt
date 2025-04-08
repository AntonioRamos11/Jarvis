package com.example.jarvis

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class LLMClient(private val context: Context) {
    private val client = OkHttpClient()
    private val serverUrl = "http://your-local-ip:8000/analyze"

    suspend fun queryLLM(text: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val requestBody = FormBody.Builder()
                    .add("text", text)
                    .build()

                val request = Request.Builder()
                    .url(serverUrl)
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                response.body?.string()?.let { jsonString ->
                    val jsonObject = JSONObject(jsonString)
                    jsonObject.getString("response")
                } ?: "No response from LLM"
            } catch (e: Exception) {
                Log.e("LLMClient", "Error querying LLM", e)
                "Sorry, I encountered an error processing your request."
            }
        }
    }
}