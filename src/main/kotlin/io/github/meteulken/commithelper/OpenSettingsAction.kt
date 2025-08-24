package io.github.meteulken.commithelper

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.DumbAware

class OpenSettingsAction : AnAction("Commit Helper Settings"), DumbAware {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        if (project != null) {
            ShowSettingsUtil.getInstance()
                .showSettingsDialog(project, CommitPluginConfigurable::class.java)
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isVisible = true
        e.presentation.isEnabled = true
    }
}
