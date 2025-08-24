package io.github.meteulken.commithelper

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.options.Configurable
import com.intellij.ui.components.ActionLink
import com.intellij.ui.components.JBPasswordField
import com.intellij.util.ui.FormBuilder
import java.awt.Component
import java.awt.Container
import java.awt.FlowLayout
import javax.swing.*

class CommitPluginConfigurable : Configurable {
    private var panel: JPanel? = null

    private lateinit var providerBox: JComboBox<String>
    private lateinit var geminiKeyField: JBPasswordField
    private lateinit var mistralKeyField: JBPasswordField
    private lateinit var styleBox: JComboBox<String>
    private lateinit var randomnessBox: JComboBox<String>
    private lateinit var languageBox: JComboBox<String>
    private lateinit var prependBranchBox: JCheckBox

    override fun getDisplayName(): String = "Commit Helper"

    private fun fieldWithLink(field: JComponent, linkText: String, url: String): JPanel =
        JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)).apply {
            isOpaque = false
            add(field)
            add(Box.createHorizontalStrut(8))
            add(ActionLink(linkText) { BrowserUtil.browse(url) })
        }

    override fun createComponent(): JComponent? {
        val settings = CommitPluginSettings.getInstance()

        geminiKeyField = JBPasswordField().apply {
            columns = 30
            text = KeyStorage.loadGemini()
        }
        mistralKeyField = JBPasswordField().apply {
            columns = 30
            text = KeyStorage.loadMistral()
        }

        providerBox = JComboBox(arrayOf("Gemini", "Mistral")).apply {
            selectedItem = settings.provider.takeIf { it == "Gemini" || it == "Mistral" } ?: "Gemini"
            prototypeDisplayValue = "Mistral"
        }
        styleBox = JComboBox(arrayOf("normal", "conventional")).apply {
            selectedItem = settings.commitStyle
            prototypeDisplayValue = "conventional"
        }
        randomnessBox = JComboBox(arrayOf("Stable", "Creative")).apply {
            selectedItem = settings.commitRandomness
            prototypeDisplayValue = "Creative"
        }
        languageBox = JComboBox(arrayOf("English", "Turkish")).apply {
            selectedItem = settings.commitLanguage
            prototypeDisplayValue = "English"
        }

        prependBranchBox = JCheckBox("Prepend branch ID to commit message").apply {
            isSelected = settings.prependBranch
        }

        val geminiRow  = fieldWithLink(geminiKeyField, "Get API key", "https://aistudio.google.com/app/apikey")
        val mistralRow = fieldWithLink(mistralKeyField, "Get API key", "https://console.mistral.ai/api-keys")

        fun setEnabledDeep(c: Component, enabled: Boolean) {
            c.isEnabled = enabled
            if (c is Container) c.components.forEach { setEnabledDeep(it, enabled) }
        }

        fun refreshEnable() {
            val p = providerBox.selectedItem as? String ?: "Gemini"
            val geminiOn = p == "Gemini"
            val mistralOn = p == "Mistral"
            setEnabledDeep(geminiRow, geminiOn)
            setEnabledDeep(geminiKeyField, geminiOn)
            setEnabledDeep(mistralRow, mistralOn)
            setEnabledDeep(mistralKeyField, mistralOn)
        }
        providerBox.addActionListener { refreshEnable() }
        refreshEnable()

        panel = FormBuilder.createFormBuilder()
            .addLabeledComponent("AI Provider:", providerBox)
            .addLabeledComponent("Gemini API Key:", geminiRow)
            .addLabeledComponent("Mistral API Key:", mistralRow)
            .addLabeledComponent("Commit Style:", styleBox)
            .addLabeledComponent("Commit Randomness:", randomnessBox)
            .addLabeledComponent("Commit Language:", languageBox)
            .addComponent(prependBranchBox)
            .addComponentFillVertically(JPanel(), 0)
            .panel

        return panel
    }

    override fun isModified(): Boolean {
        val s = CommitPluginSettings.getInstance()
        return providerBox.selectedItem != s.provider ||
                String(geminiKeyField.password) != KeyStorage.loadGemini() ||
                String(mistralKeyField.password) != KeyStorage.loadMistral() ||
                styleBox.selectedItem != s.commitStyle ||
                randomnessBox.selectedItem != s.commitRandomness ||
                languageBox.selectedItem != s.commitLanguage ||
                prependBranchBox.isSelected != s.prependBranch
    }

    override fun apply() {
        val s = CommitPluginSettings.getInstance()
        s.provider = providerBox.selectedItem as String
        KeyStorage.saveGemini(String(geminiKeyField.password))
        KeyStorage.saveMistral(String(mistralKeyField.password))
        s.commitStyle = styleBox.selectedItem as String
        s.commitRandomness = randomnessBox.selectedItem as String
        s.commitLanguage = languageBox.selectedItem as String
        s.prependBranch = prependBranchBox.isSelected
    }

    override fun reset() {
        val s = CommitPluginSettings.getInstance()
        providerBox.selectedItem = s.provider.takeIf { it == "Gemini" || it == "Mistral" } ?: "Gemini"
        geminiKeyField.text = KeyStorage.loadGemini()
        mistralKeyField.text = KeyStorage.loadMistral()
        styleBox.selectedItem = s.commitStyle
        randomnessBox.selectedItem = s.commitRandomness
        languageBox.selectedItem = s.commitLanguage
        prependBranchBox.isSelected = s.prependBranch
    }
}
