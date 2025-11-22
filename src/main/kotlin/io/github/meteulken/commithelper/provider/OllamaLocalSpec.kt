package io.github.meteulken.commithelper.provider

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject

object OllamaLocalSpec : ProviderSpec {
    override val name: String = "Ollama (Local)"

    // İstersen burayı ileride Settings’ten okursun:
    private const val BASE_URL = "http://127.0.0.1:11434/v1"
    private const val ENDPOINT = "$BASE_URL/chat/completions"
    private const val MODEL = "llama3.2:3b" // hızlı & yeterli; dilediğinde değiştir

    override fun buildRequest(apiKey: String, prompt: String, temperature: Double): Request {
        // Güvenlik: sadece localhost’a izin (istersen kaldır)
        val host = java.net.URI(BASE_URL).host?.lowercase().orEmpty()
        require(host == "127.0.0.1" || host == "localhost") {
            "Offline Mode: only localhost endpoints are allowed"
        }

        val messages = JSONArray()
            .put(JSONObject().put("role", "system").put("content",
                "You write Git commit messages. Output ONE sentence in the requested language, " +
                        "imperative mood, no emoji, no trailing period, keep it short."
            ))
            .put(JSONObject().put("role", "user").put("content", prompt))

        val body = JSONObject()
            .put("model", MODEL)
            .put("temperature", temperature)
            .put("max_tokens", 128)
            .put("stream", false)
            .put("messages", messages)

        return Request.Builder()
            .url(ENDPOINT)
            .addHeader("User-Agent", "CommitHelper/1.0")
            // Yerel olduğu için Authorization başlığı YOK
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .build()
    }

    override fun extractText(response: Response, rawBody: String): String =
        JSONObject(rawBody)
            .optJSONArray("choices")?.optJSONObject(0)
            ?.optJSONObject("message")?.optString("content")
            ?.trim().orEmpty()

    override fun errorMessage(response: Response, rawBody: String): String =
        "Ollama (Local) request failed: ${response.code} ${response.message}"
}
