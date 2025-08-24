package io.github.meteulken.commithelper.provider

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject

object MistralSpec : ProviderSpec {
    override val name: String = "Mistral"
    private const val MODEL = "mistral-small-latest"
    private const val ENDPOINT = "https://api.mistral.ai/v1/chat/completions"

    override fun buildRequest(apiKey: String, prompt: String, temperature: Double): Request {
        val bodyJson = JSONObject()
            .put("model", MODEL)
            .put("temperature", temperature)
            .put("messages", listOf(
                JSONObject().put("role", "system").put("content", "You write Git commit messages."),
                JSONObject().put("role", "user").put("content", prompt)
            ))

        return Request.Builder()
            .url(ENDPOINT)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("User-Agent", "CommitHelper/1.0")
            .post(bodyJson.toString().toRequestBody("application/json".toMediaType()))
            .build()
    }

    override fun extractText(response: Response, rawBody: String): String =
        JSONObject(rawBody)
            .optJSONArray("choices")?.optJSONObject(0)
            ?.optJSONObject("message")?.optString("content")
            ?.trim()
            .orEmpty()

    override fun errorMessage(response: Response, rawBody: String): String =
        "Mistral request failed: ${response.code} ${response.message}"
}
