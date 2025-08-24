package io.github.meteulken.commithelper

import com.intellij.openapi.project.Project
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

object DiffUtil {
    private const val PROC_TIMEOUT_MS = 7000
    private const val MAX_DIFF_CHARS = 10_000

    fun getGitDiff(project: Project): String {
        val base = project.basePath ?: return ""
        val pb = ProcessBuilder(
            "git", "-c", "core.pager=cat",
            "-c", "core.quotepath=false",
            "diff", "--staged", "--no-ext-diff", "--no-color", "--unified=0"
        )
            .directory(File(base))
            .redirectErrorStream(true)

        val p = pb.start()

        val outSb = StringBuilder()
        val reader = p.inputStream.bufferedReader(StandardCharsets.UTF_8)
        val t = thread(start = true, isDaemon = true) {
            reader.forEachLine {
                if (outSb.length < MAX_DIFF_CHARS) {
                    val room = MAX_DIFF_CHARS - outSb.length
                    if (it.length + 1 <= room) {
                        outSb.append(it).append('\n')
                    } else {
                        outSb.append(it.take(room.coerceAtLeast(0)))
                    }
                }
            }
        }

        val finished = p.waitFor(PROC_TIMEOUT_MS.toLong(), TimeUnit.MILLISECONDS)
        if (!finished) {
            p.destroyForcibly()
            t.join(300)
            return outSb.toString().take(MAX_DIFF_CHARS) + "\n[diff timed out]"
        }

        t.join(500)
        val out = outSb.toString()

        return if (out.length <= MAX_DIFF_CHARS) out
        else out.take(MAX_DIFF_CHARS) + "\n[...diff truncated...]"
    }
}
