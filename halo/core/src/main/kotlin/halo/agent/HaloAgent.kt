package halo.agent

import halo.brain.Brain
import halo.memory.Exchange
import halo.memory.Notebook
import halo.memory.ShortTermMemory
import halo.prompts.HaloPrompts
import halo.tools.HaloTool
import halo.tools.ToolRegistry
import halo.tools.builtin.CurrentTimeTool
import halo.tools.builtin.RememberTool

/** Halo itself: takes what the user said and returns what Halo answers, remembering the conversation. */
class HaloAgent(
    private val brain: Brain,
    private val notebook: Notebook,
    private val memory: ShortTermMemory,
    extraTools: List<HaloTool> = emptyList(),
) {
    private val tools = ToolRegistry(listOf(CurrentTimeTool(), RememberTool(notebook)) + extraTools)

    fun ask(userText: String): String {
        val text = userText.trim()
        require(text.isNotEmpty()) { "userText must not be blank" }
        val reply = brain.reply(HaloPrompts.system(notebook.render()), memory.recent(), text, tools)
        memory.add(Exchange(user = text, halo = reply))
        return reply
    }

    /** Starts a new conversation. The notebook is kept. */
    fun reset() = memory.clear()
}
