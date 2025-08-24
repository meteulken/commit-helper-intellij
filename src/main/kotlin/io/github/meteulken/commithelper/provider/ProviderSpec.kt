package io.github.meteulken.commithelper.provider

import okhttp3.Request
import okhttp3.Response


interface ProviderSpec {
    val name: String
    fun buildRequest(apiKey: String, prompt: String, temperature: Double): Request
    fun extractText(response: Response, rawBody: String): String
    fun errorMessage(response: Response, rawBody: String): String
}
