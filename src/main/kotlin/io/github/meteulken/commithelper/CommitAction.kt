package io.github.meteulken.commithelper

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.VcsDataKeys
import com.intellij.openapi.vcs.ui.CommitMessage
import io.github.meteulken.commithelper.provider.GeminiSpec
import io.github.meteulken.commithelper.provider.MistralSpec

private const val MAX_DIFF_CHARS = 10_000

class CommitAction : AnAction(), DumbAware {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val commitBox = e.getData(VcsDataKeys.COMMIT_MESSAGE_CONTROL) as? CommitMessage ?: return

        val settings = CommitPluginSettings.getInstance()
        val providerName = settings.provider
        val branch = BranchUtil.getCurrentBranch(project) ?: "no-branch"

        val rawDiff = DiffUtil.getGitDiff(project)
        val diff = rawDiff.takeIf { it.isNotBlank() }?.let {
            if (it.length <= MAX_DIFF_CHARS) it else it.take(MAX_DIFF_CHARS) + "\n[...diff truncated...]"
        } ?: ""

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Generating commit message…", true) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = true

                val (providerSpec, apiKey) = when (providerName) {
                    "Gemini"  -> GeminiSpec to KeyStorage.loadGemini()
                    "Mistral" -> MistralSpec to KeyStorage.loadMistral()
                    else      -> null to ""
                }

                if (providerSpec == null) {
                    notify(project, "No provider selected.")
                    return
                }
                if (apiKey.isBlank()) {
                    notify(project, "${providerSpec.name} API key is not set.")
                    return
                }

                if (diff.isBlank()) {
                    val fallbackSubject =
                        if (settings.commitStyle.equals("conventional", true)) "chore: update files"
                        else "Update files"

                    val fallbackMessage = if (settings.prependBranch) {
                        BranchUtil.extractBranchKey(branch)?.let { "$it - $fallbackSubject" } ?: fallbackSubject
                    } else {
                        fallbackSubject
                    }

                    ApplicationManager.getApplication().invokeLater {
                        commitBox.setCommitMessage(fallbackMessage)
                    }
                    return
                }

                val aiMessage = try {
                    CommitGenerator.generate(
                        provider = providerSpec,
                        diff = diff,
                        branch = branch,
                        apiKey = apiKey,
                        style = settings.commitStyle,
                        randomness = settings.commitRandomness,
                        language = settings.commitLanguage
                    )
                } catch (t: Throwable) {
                    "Error: ${t.message ?: "unknown"}"
                }

                if (aiMessage.startsWith("Error", ignoreCase = true)
                    || aiMessage.startsWith("Gemini request", ignoreCase = true)
                    || aiMessage.startsWith("Mistral request", ignoreCase = true)
                    || aiMessage.equals("Empty response", ignoreCase = true)
                ) {
                    notify(project, "Commit message could not be generated. $aiMessage")
                    return
                }

                val subject = aiMessage.trim()
                val commitMessage = if (settings.prependBranch) {
                    BranchUtil.extractBranchKey(branch)?.let { "$it - $subject" } ?: subject
                } else {
                    subject
                }

                ApplicationManager.getApplication().invokeLater {
                    commitBox.setCommitMessage(commitMessage)
                }
            }
        })
    }

    private fun notify(project: Project, content: String) {
        val group = NotificationGroupManager.getInstance().getNotificationGroup("Commit Helper")
        group.createNotification(content, NotificationType.WARNING).notify(project)
    }
}
