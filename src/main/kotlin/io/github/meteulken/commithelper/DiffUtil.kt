package io.github.meteulken.commithelper

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.Change
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

object DiffUtil {

    fun collectSelectedDiffs(project: Project, changes: List<Change>): String {
        val basePath = project.basePath ?: return ""
        if (changes.isEmpty()) return ""

        val root = File(basePath)
        val paths = changes.mapNotNull { it.virtualFile?.path ?: it.afterRevision?.file?.path }
            .map { File(it).relativeTo(root).path }
            .filter { it.isNotBlank() }

        if (paths.isEmpty()) return ""

        return try {
            runGitProcess(basePath, paths)
        } catch (t: Throwable) {
            "Error while collecting diff: ${t.message}"
        }
    }

    private fun runGitProcess(basePath: String, paths: List<String>): String {
        // We use 'git diff HEAD -- path1 path2' to get changes for specific files.
        // This covers both staged and unstaged changes for those files compared to HEAD.
        val cmd = mutableListOf("git", "-C", basePath, "diff", "HEAD", "--")
        cmd.addAll(paths)

        val diffOutput = runGitCommand(basePath, cmd)

        val filtered = diffOutput
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
