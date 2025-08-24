package io.github.meteulken.commithelper

import org.json.JSONObject
import java.util.Locale

object CommitCore {
    const val SUBJECT_LIMIT = 72
    const val MAX_DIFF = 6000

    private val CONVENTIONAL_RE = Regex(
        "^(build|chore|ci|docs|feat|fix|perf|refactor|revert|style|test)(\\([^)]*\\))?:\\s+(.+)$",
        RegexOption.IGNORE_CASE
    )

    data class ParsedBranch(val scope: String?, val typeHint: String?)

    fun temperature(randomness: String): Double =
        if (randomness.equals("Creative", true)) 0.7 else 0.0

    fun parseBranch(branch: String): ParsedBranch {
        val m = Regex(
            "^(feat|feature|fix|bugfix|hotfix|docs|refactor|perf|test|chore)/([^/]+)",
            RegexOption.IGNORE_CASE
        ).find(branch)

        if (m != null) {
            val typeRaw = m.groupValues[1]
            val type = when (typeRaw.lowercase(Locale.getDefault())) {
                "feature" -> "feat"
                else -> typeRaw.lowercase(Locale.getDefault())
            }

            val rawScope = m.groupValues[2]

            val ISSUE_RE = Regex("^[A-Za-z]+-\\d+$")
            val scope = rawScope
                .takeIf { it.isNotBlank() && !ISSUE_RE.matches(it) }
                ?.split('-', '_', '/')
                ?.firstOrNull()

            return ParsedBranch(scope, type)
        }

        return ParsedBranch(null, null)
    }

    fun buildStyleInstr(style: String, scope: String?, typeHint: String?): String =
        if (style.equals("conventional", true)) """
            Use Conventional Commits.
            - Allowed types: feat, fix, docs, refactor, perf, test, chore
            - Subject ≤ $SUBJECT_LIMIT chars, imperative mood, no trailing period.
            - Use scope "${scope ?: ""}" if relevant.
            - If unsure, default type = ${typeHint ?: "feat"}.
        """.trimIndent()
        else """
            Do NOT use any Conventional Commit prefixes (feat:, fix:, etc.).
            - Subject must be plain sentence only.
            - Subject ≤ $SUBJECT_LIMIT chars, imperative mood, no trailing period.
        """.trimIndent()

    fun buildLangInstr(language: String, diff: String): String {
        val isTr = language.equals("Turkish", true)
        val hints = if (isTr) extractEntityHints(diff) else ""
        val hintInstr = if (isTr && hints.isNotBlank())
            "Somut isimleri özellikle an: $hints" else ""

        return if (isTr) """
        Write the message in Turkish.
        - Kip: **edilgen geçmiş zaman** kullan (örn: eklendi, düzeltildi, güncellendi, kaldırıldı, yeniden adlandırıldı, taşındı).
        - **Projeye özgü özel adları** (sınıf, dosya, fonksiyon, test, değişken, entity, domain terimleri) 
          **aslıyla koru, Türkçeye çevirme.**
        - Genel ifadelerden kaçın; mümkün olduğunda somut dosya/sınıf/test adlarını belirt.
        - "Modeller eklendi", "Değişiklikler yapıldı" gibi belirsiz kalıpları kullanma.
        $hintInstr
    """.trimIndent()
        else "Write the message in English."
    }

    fun trimmedDiff(diff: String): String =
        buildString {
            append(diff.take(MAX_DIFF))
            if (diff.length > MAX_DIFF) append("\n[Diff truncated]")
        }

    fun buildPrompt(
        language: String,
        diff: String,
        style: String,
        scope: String?,
        typeHint: String?
    ): String = """
        You write Git commit messages.
        ${buildLangInstr(language, diff)}
        ${buildStyleInstr(style, scope, typeHint)}
        Rules:
        - First line is the subject, single line only.
        - No code fences, quotes, or explanations.
        - Do not include the branch name in the commit message.
        - Analyze **all files and changes together**, and write a single summary that reflects the overall purpose of the commit.
        - Do not generate separate commit messages per file.

        DIFF (truncated if long):
        ${trimmedDiff(diff)}
    """.trimIndent()

    fun postProcess(raw: String, conventional: Boolean, scope: String?, typeHint: String?): String {
        var s = raw.trim()
        s = s.removePrefix("```").removeSuffix("```")
            .replace(Regex("^```[a-zA-Z]*\\s*"), "")
            .trim()
        var subject = s.replace("\r\n", "\n").lineSequence().firstOrNull().orEmpty()
            .replace(Regex("\\s+"), " ").trim().removeSuffix(".")
        if (subject.length > SUBJECT_LIMIT) {
            val cut = subject.take(SUBJECT_LIMIT + 1)
            subject = cut.substring(0, cut.lastIndexOf(' ').takeIf { it > 0 } ?: SUBJECT_LIMIT).trim()
        }
        if (CONVENTIONAL_RE.containsMatchIn(subject)) return subject
        return if (conventional) {
            val type = (typeHint ?: "feat").lowercase(Locale.getDefault())
            buildString {
                append(type)
                scope?.takeIf { it.isNotBlank() }?.let { append("($it)") }
                append(": ")
                append(subject)
            }
        } else subject
    }

    fun extractApiError(body: String): String? = runCatching {
        val j = JSONObject(body)
        j.optJSONObject("error")?.optString("message")?.takeIf { it.isNotBlank() }
    }.getOrNull()

    fun extractEntityHints(diff: String): String {
        val names = linkedSetOf<String>()
        Regex("""(?m)^\+\+\+\s+b/([^\n]+)""").findAll(diff).forEach {
            val file = it.groupValues[1]
            val base = file.substringAfterLast('/').substringBeforeLast('.')
            if (base.isNotBlank()) names += base
        }
        Regex("""(?m)^diff --git a/[^ ]+ b/([^ \n]+)""").findAll(diff).forEach {
            val file = it.groupValues[1]
            val base = file.substringAfterLast('/').substringBeforeLast('.')
            if (base.isNotBlank()) names += base
        }
        Regex("""\b(?:class|data\s+class|interface)\s+([A-Z][A-Za-z0-9_]*)""")
            .findAll(diff).forEach { names += it.groupValues[1] }
        Regex("""\b([A-Z][A-Za-z0-9_]*Tests?)\b""").findAll(diff).forEach {
            names += it.groupValues[1]
        }
        val banned = setOf("Main", "App", "Test", "Tests")
        return names.filter { it.length in 3..50 && it !in banned }
            .distinct()
            .take(6)
            .joinToString(", ")
    }
}
