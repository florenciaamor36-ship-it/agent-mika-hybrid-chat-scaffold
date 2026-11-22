package com.laclave.hybridagent

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : Activity() {
    private val history = JSONArray()
    private lateinit var transcript: TextView
    private lateinit var prompt: EditText
    private lateinit var sendButton: Button
    private var apiBase = "https://api.openai.com/v1"
    private var model = "gpt-4o-mini"
    private var apiKey = ""
    private val background = Color.rgb(18, 18, 24)
    private val panel = Color.rgb(31, 31, 42)
    private val accent = Color.rgb(255, 190, 65)
    private val textColor = Color.rgb(245, 245, 248)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SecureSettings.load(this)?.let {
            apiBase = it.optString("base", apiBase)
            model = it.optString("model", model)
            apiKey = it.optString("key", "")
        }
        buildScreen()
    }

    private fun buildScreen() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(background)
            setPadding(dp(16), dp(18), dp(16), dp(12))
        }
        val heading = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val title = TextView(this).apply {
            text = "Asistente híbrido"
            textSize = 22f
            setTextColor(textColor)
            setTypeface(null, Typeface.BOLD)
        }
        heading.addView(title, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        heading.addView(Button(this).apply {
            text = "Modelo"
            setOnClickListener { showSettings() }
        })
        root.addView(heading)
        root.addView(TextView(this).apply {
            text = "Chat experimental · modelo por internet · historial solo durante esta sesión"
            textSize = 12f
            setTextColor(Color.LTGRAY)
            setPadding(0, dp(4), 0, dp(12))
        })

        val scroll = ScrollView(this).apply {
            setBackgroundColor(panel)
            isFillViewport = true
        }
        transcript = TextView(this).apply {
            text = "Hola. Configurá un proveedor compatible en «Modelo» para empezar.\n\nTodavía no controlo otras apps: ese módulo se agregará aparte y pedirá permiso explícito."
            textSize = 16f
            setTextColor(textColor)
            setPadding(dp(14), dp(14), dp(14), dp(14))
            textIsSelectable = true
        }
        scroll.addView(transcript)
        root.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        prompt = EditText(this).apply {
            hint = "Escribí tu mensaje"
            setTextColor(textColor)
            setHintTextColor(Color.LTGRAY)
            setBackgroundColor(panel)
            minLines = 2
            maxLines = 5
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            setPadding(dp(12), dp(8), dp(12), dp(8))
        }
        root.addView(prompt, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(12)
        })
        sendButton = Button(this).apply {
            text = "Enviar"
            setTextColor(Color.BLACK)
            setBackgroundColor(accent)
            setOnClickListener { sendMessage() }
        }
        root.addView(sendButton, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(8)
        })
        setContentView(root)
    }

    private fun showSettings() {
        val fields = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), 0)
        }
        val baseField = field("Dirección base (API compatible)", apiBase)
        val modelField = field("Modelo", model)
        val keyField = field("Clave API (no la compartas)", apiKey).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        fields.addView(baseField)
        fields.addView(modelField)
        fields.addView(keyField)
        fields.addView(TextView(this).apply {
            text = "La clave se cifra con Android Keystore en este teléfono. Los mensajes se envían al proveedor que configures."
            textSize = 12f
            setPadding(0, dp(8), 0, 0)
        })
        AlertDialog.Builder(this)
            .setTitle("Configurar modelo")
            .setView(fields)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Guardar") { _, _ ->
                apiBase = baseField.text.toString().trim().trimEnd('/').ifBlank { "https://api.openai.com/v1" }
                model = modelField.text.toString().trim().ifBlank { "gpt-4o-mini" }
                apiKey = keyField.text.toString().trim()
                SecureSettings.save(this, JSONObject().put("base", apiBase).put("model", model).put("key", apiKey))
                appendLine("Sistema", "Configuración guardada en este dispositivo.")
            }
            .show()
    }

    private fun field(label: String, value: String): EditText = EditText(this).apply {
        hint = label
        setText(value)
        setSingleLine(true)
        setPadding(dp(8), dp(8), dp(8), dp(8))
    }

    private fun sendMessage() {
        val text = prompt.text.toString().trim()
        if (text.isEmpty()) return
        if (apiKey.isBlank()) {
            AlertDialog.Builder(this).setMessage("Primero configurá la dirección, el modelo y la clave de tu proveedor.")
                .setPositiveButton("Configurar") { _, _ -> showSettings() }
                .setNegativeButton("Ahora no", null).show()
            return
        }
        prompt.setText("")
        appendLine("Vos", text)
        history.put(JSONObject().put("role", "user").put("content", text))
        sendButton.isEnabled = false
        Thread {
            val result = runCatching { requestModel() }
            runOnUiThread {
                sendButton.isEnabled = true
                result.onSuccess { answer ->
                    history.put(JSONObject().put("role", "assistant").put("content", answer))
                    appendLine("Asistente", answer)
                }.onFailure { error ->
                    val message = error.message ?: "No se pudo conectar con el proveedor."
                    appendLine("Aviso", message)
                }
            }
        }.start()
    }

    private fun requestModel(): String {
        val connection = (URL("$apiBase/chat/completions").openConnection() as HttpURLConnection)
        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = 20_000
            connection.readTimeout = 60_000
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Authorization", "Bearer $apiKey")
            val body = JSONObject().put("model", model).put("messages", history).put("stream", false)
                .toString().toByteArray(Charsets.UTF_8)
            connection.outputStream.use { it.write(body) }
            val status = connection.responseCode
            val response = (if (status in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (status !in 200..299) throw IllegalStateException("El proveedor respondió HTTP $status. Revisá el modelo, la dirección y la clave.")
            val content = JSONObject(response).getJSONArray("choices").getJSONObject(0)
                .getJSONObject("message").optString("content")
            if (content.isBlank()) throw IllegalStateException("El proveedor devolvió una respuesta vacía.")
            return content
        } finally {
            connection.disconnect()
        }
    }

    private fun appendLine(who: String, message: String) {
        transcript.append("\n\n$who:\n$message")
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
