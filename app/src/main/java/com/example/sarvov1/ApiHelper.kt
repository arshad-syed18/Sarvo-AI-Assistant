package com.example.sarvov1

import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.URL

interface ApiCallback {
    fun onApiSuccess(response: String)
    fun onApiError(error: String)
}
class ApiHelper (private val apiCallback: ApiCallback){

    private val BASE_URL = "https://deep-friendly-kodiak.ngrok-free.app/user-input"

    fun getResponse(input: String) {
        Thread(Runnable {
            try {
                val url = URL(BASE_URL)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("Accept", "application/json")
                connection.connectTimeout = 60000
                connection.readTimeout = 60000

                val requestBody = "{\"input\":\"$input\"}"

                val outputStream = DataOutputStream(connection.outputStream)
                outputStream.write(requestBody.toByteArray())
                outputStream.flush()
                outputStream.close()

                val responseCode = connection.responseCode
                val headers = connection.headerFields
                val inputStream = connection.inputStream

                inputStream.use { input ->
                    val reader = InputStreamReader(input)
                    val response = StringBuilder()
                    val bufferedReader = BufferedReader(reader)

                    bufferedReader.forEachLine {
                        response.append(it.trim())
                    }

                    // Parse JSON response
                    val jsonResponse = JSONObject(response.toString())
                    val assistantResponse = jsonResponse.getString("assistant_response")

                    // Log.d("API", "Assistant Response: $assistantResponse")
                    apiCallback.onApiSuccess(assistantResponse)


                }

            } catch (e: Exception) {
                Log.d("API Failure", e.message.toString())
                apiCallback.onApiError(e.message.toString())
                e.printStackTrace()
            }
        }).start()
    }
}