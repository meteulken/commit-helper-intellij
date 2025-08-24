package io.github.meteulken.commithelper

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.ChangeListManager

object DiffUtil {
    private const val MAX_DIFF_CHARS = 6_000


    fun getSelectedChanges(project: Project): List<Change> {
        val manager = ChangeListManager.getInstance(project)
        return manager.defaultChangeList.changes.toList()
    }


    fun getDiffForChange(change: Change): String {
        val before = change.beforeRevision?.content
        val after = change.afterRevision?.content
        val path = change.virtualFile?.path ?: change.beforeRevision?.file?.path ?: "[unknown path]"

        return buildString {
            append("File: $path\n")
            append("Status: ${change.type}\n\n")
            append("--- Before ---\n")
            append(before ?: "[empty]\n")
            append("\n--- After ---\n")
            append(after ?: "[empty]\n")
            append("\n\n")
        }.take(MAX_DIFF_CHARS)
    }


    fun collectSelectedDiffs(project: Project): String {
        val selected = getSelectedChanges(project)
        return selected.joinToString("\n") { getDiffForChange(it) }
    }
}
