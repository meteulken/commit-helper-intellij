package io.github.meteulken.commithelper.provider

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import io.github.meteulken.commithelper.CommitCore

object GeminiSpec : ProviderSpec {
    override val name: String = "Gemini"
    private const val MODEL = "gemini-2.0-flash"
    private const val ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"

    override fun buildRequest(apiKey: String, prompt: String, temperature: Double): Request {
        val body = """
          {
            "contents":[{"parts":[{"text": ${JSONObject.quote(prompt)}}]}],
            "generationConfig": {"temperature": $temperature}
          }
        """.trimIndent().toRequestBody("application/json".toMediaType())

        return Request.Builder()
            .url("$ENDPOINT?key=$apiKey")
            .addHeader("User-Agent", "CommitHelper/1.0")
            .post(body)
            .build()
    }

    override fun extractText(response: Response, rawBody: String): String =
        JSONObject(rawBody)
            .optJSONArray("candidates")?.optJSONObject(0)
            ?.optJSONObject("content")?.optJSONArray("parts")
            ?.optJSONObject(0)?.optString("text")
            .orEmpty()

    override fun errorMessage(response: Response, rawBody: String): String {
        val apiErr = CommitCore.extractApiError(rawBody)
        return "Gemini request failed: ${response.code} ${response.message}" +
                (apiErr?.let { " - $it" } ?: "")
    }
}
