package io.github.meteulken.commithelper

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.io.File
import kotlin.math.abs

object BranchUtil {

    private val ISSUE_REGEX = Regex("([A-Za-z]+-\\d+)", RegexOption.IGNORE_CASE)
    private val DEFAULT_BRANCHES = setOf("main", "master", "develop", "dev", "test")

    fun getCurrentBranch(project: Project): String? {
        val raw = runCatching {
            val clazz = Class.forName("git4idea.repo.GitRepositoryManager")
            val getInstance = clazz.getMethod("getInstance", Project::class.java)
            val manager = getInstance.invoke(null, project)
            val reposMethod = clazz.getMethod("getRepositories")
            val repos = reposMethod.invoke(manager) as? Collection<*>

            if (!repos.isNullOrEmpty()) {
                val repoClass = Class.forName("git4idea.repo.GitRepository")
                val branchMethod = repoClass.getMethod("getCurrentBranchName")

                val base = project.basePath
                val chosen = if (base != null) {
                    repos.minByOrNull { repo ->
                        val rootField = repoClass.getMethod("getRoot")
                        val root = rootField.invoke(repo) as? VirtualFile
                        val repoPath = root?.path ?: ""
                        abs(repoPath.length - base.length)
                    }
                } else {
                    repos.first()
                }

                branchMethod.invoke(chosen) as? String
            } else null
        }.getOrNull()

        val branchName = if (!raw.isNullOrBlank()) {
            raw
        } else {
            val basePath = project.basePath ?: return null
            val gitDir = resolveGitDir(File(basePath, ".git")) ?: return null
            val head = File(gitDir, "HEAD")
            if (!head.isFile) return null

            val content = runCatching { head.readText() }.getOrNull()?.trim().orEmpty()
            if (content.isBlank()) return null

            if (content.startsWith("ref:", ignoreCase = true)) {
                content.removePrefix("ref:").trim().removePrefix("refs/heads/").trim()
            } else {
                content.take(7)
            }
        }

        return normalizeBranch(branchName)
    }

    private fun resolveGitDir(dotGit: File): File? {
        return when {
            dotGit.isDirectory -> dotGit
            dotGit.isFile -> {
                val line = runCatching { dotGit.readText().trim() }.getOrNull() ?: return null
                if (!line.startsWith("gitdir:", ignoreCase = true)) return null
                val raw = line.removePrefix("gitdir:").trim()
                val candidate = File(raw)
                val resolved = if (candidate.isAbsolute) {
                    candidate
                } else {
                    File(dotGit.parentFile, raw)
                }
                runCatching { resolved.canonicalFile }.getOrElse { resolved }.takeIf { it.exists() }
            }
            else -> null
        }
    }

    fun extractBranchKey(name: String?): String? {
        if (name.isNullOrBlank()) return null
        val clean = name.removePrefix("refs/heads/")

        val match = ISSUE_REGEX.find(clean)
        if (match != null) return match.value

        if (clean.lowercase() in DEFAULT_BRANCHES) return clean

        return null
    }

    fun normalizeBranch(name: String): String {
        return extractBranchKey(name) ?: run {
            val parts = name.split("/")
            if (parts.isNotEmpty()) parts.last() else name
        }
    }
}
