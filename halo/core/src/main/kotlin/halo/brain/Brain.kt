package halo.brain

import halo.memory.Exchange
import halo.prompts.SystemPrompt
import halo.tools.ToolRegistry

/** Thinks of Halo's reply. Implemented by Claude in the app and by fakes in tests. */
interface Brain {
    /**
     * Answers [userText] given the conversation so far, using [tools] as needed.
     * Returns the text Halo will say.
     */
    fun reply(system: SystemPrompt, history: List<Exchange>, userText: String, tools: ToolRegistry): String
}
