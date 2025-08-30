package io.github.meteulken.commithelper

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.ChangeListManager
import java.io.File
import java.io.BufferedReader
import java.io.InputStreamReader

object DiffUtil {

    fun collectSelectedDiffs(project: Project): String {
        val basePath = project.basePath ?: return ""
        val changeListManager = ChangeListManager.getInstance(project)
        val changes = changeListManager.allChanges
        if (changes.isEmpty()) return ""

        return try {
            runGitProcess(basePath)
        } catch (_: ClassNotFoundException) {
            runGitProcess(basePath)
        } catch (t: Throwable) {
            "Error while collecting diff: ${t.message}"
        }
    }

    private fun runGitProcess(basePath: String): String {
        val diffs = mutableListOf<String>()

        diffs += runGitCommand(basePath, listOf("git", "-C", basePath, "diff", "--cached"))
        diffs += runGitCommand(basePath, listOf("git", "-C", basePath, "diff"))

        val allDiff = diffs.joinToString("\n")

        val filtered = allDiff
            .lines()
            .filter { it.startsWith("+") || it.startsWith("-") }

        val sb = StringBuilder()

        filtered.forEach { line ->
            when {
                line.startsWith("+++") -> {
                    val fileName = line.removePrefix("+++ b/").trim()
                    sb.appendLine("File: $fileName")
                }
                line.startsWith("---") -> {
                }
                line.startsWith("+") && !line.startsWith("+++") -> {
                    sb.appendLine("  + ${line.removePrefix("+")}")
                }
                line.startsWith("-") && !line.startsWith("---") -> {
                    sb.appendLine("  - ${line.removePrefix("-")}")
                }
            }
        }

        val result = sb.toString().trim()
        return if (result.length > 6000) {
            result.take(6000) + "\n[...diff truncated...]"
        } else {
            result
        }
    }

    private fun runGitCommand(basePath: String, cmd: List<String>): String {
        val process = ProcessBuilder(cmd)
            .directory(File(basePath))
            .redirectErrorStream(true)
            .start()

        val output = BufferedReader(InputStreamReader(process.inputStream)).readText()
        process.waitFor()
        return output
    }
}
