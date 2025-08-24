package io.github.meteulken.commithelper

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.generateServiceName
import com.intellij.ide.passwordSafe.PasswordSafe

object KeyStorage {
    private const val SERVICE = "CommitHelper"
    private const val ACCOUNT_GEMINI = "GeminiApiKey"
    private const val ACCOUNT_MISTRAL = "MistralApiKey"

    private fun attr(account: String): CredentialAttributes =
        CredentialAttributes(generateServiceName(SERVICE, account), account)

    fun saveGemini(value: String?) {
        val v = value?.trim().orEmpty().ifEmpty { null }
        PasswordSafe.instance.setPassword(attr(ACCOUNT_GEMINI), v)
    }

    fun loadGemini(): String =
        PasswordSafe.instance.getPassword(attr(ACCOUNT_GEMINI)) ?: ""

    fun saveMistral(value: String?) {
        val v = value?.trim().orEmpty().ifEmpty { null }
        PasswordSafe.instance.setPassword(attr(ACCOUNT_MISTRAL), v)
    }

    fun loadMistral(): String =
        PasswordSafe.instance.getPassword(attr(ACCOUNT_MISTRAL)) ?: ""
}
