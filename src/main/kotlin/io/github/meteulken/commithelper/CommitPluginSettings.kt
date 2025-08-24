package io.github.meteulken.commithelper

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service

@State(name = "CommitPluginSettings", storages = [Storage("CommitPluginSettings.xml")])
class CommitPluginSettings : PersistentStateComponent<CommitPluginSettings.State> {

    companion object { fun getInstance(): CommitPluginSettings = service() }

    class State {
        var provider: String = "Gemini"
        var commitStyle: String = "normal"
        var commitRandomness: String = "Stable"
        var commitLanguage: String = "English"
        var prependBranch: Boolean = true
    }

    private var state = State()

    override fun getState(): State = state
    override fun loadState(state: State) { this.state = state }

    var provider: String
        get() = state.provider
        set(value) { state.provider = value }

    var commitStyle: String
        get() = state.commitStyle
        set(value) { state.commitStyle = value }

    var commitRandomness: String
        get() = state.commitRandomness
        set(value) { state.commitRandomness = value }

    var commitLanguage: String
        get() = state.commitLanguage
        set(value) { state.commitLanguage = value }

    var prependBranch: Boolean
        get() = state.prependBranch
        set(value) { state.prependBranch = value }
}