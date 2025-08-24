package io.github.meteulken.commithelper

import okhttp3.OkHttpClient
import io.github.meteulken.commithelper.provider.ProviderSpec
import java.time.Duration

object CommitGenerator {
    private val http by lazy {
        OkHttpClient.Builder()
            .callTimeout(Duration.ofSeconds(30))
            .connectTimeout(Duration.ofSeconds(10))
            .readTimeout(Duration.ofSeconds(30))
            .build()
    }

    fun generate(
        provider: ProviderSpec,
        diff: String,
        branch: String,
        apiKey: String,
        style: String,
        randomness: String,
        language: String
    ): String {
        if (apiKey.isBlank()) return "${provider.name} API key not set"

        val (scope, typeHint) = CommitCore.parseBranch(branch)

        val prompt = CommitCore.buildPrompt(
            language = language,
            diff = diff,
            style = style,
            scope = scope,
            typeHint = typeHint
        )

        val temperature = CommitCore.temperature(randomness)

        return try {
            val req = provider.buildRequest(apiKey, prompt, temperature)
            http.newCall(req).execute().use { resp ->
                val raw = resp.body?.string().orEmpty()
                if (!resp.isSuccessful) return provider.errorMessage(resp, raw)

                val text = provider.extractText(resp, raw).trim()
                val cleaned = CommitCore.postProcess(
                    raw = text,
                    conventional = style.equals("conventional", true),
                    scope = scope,
                    typeHint = typeHint
                )
                if (cleaned.isBlank()) "Empty response" else cleaned
            }
        } catch (e: Exception) {
            "${provider.name} request error: ${e.message}"
        }
    }
}
