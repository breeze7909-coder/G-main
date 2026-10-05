package halo.prompts

/**
 * Halo's instructions to Claude.
 *
 * [stable] is the persona and rules, identical on every request so it can be cached.
 * [notebook] is what Halo remembers about the user and changes over time.
 */
data class SystemPrompt(val stable: String, val notebook: String)

/** Builds Halo's system prompt from the prompt files under resources/prompts. */
object HaloPrompts {
    /** Bump when persona.md or conversation.md changes, so evaluations can tell versions apart. */
    const val VERSION = 1

    val persona: String by lazy { load("persona.md") }
    val conversation: String by lazy { load("conversation.md") }

    fun system(notebook: String): SystemPrompt =
        SystemPrompt(stable = persona + "\n\n" + conversation, notebook = notebook)

    private fun load(name: String): String {
        val stream = HaloPrompts::class.java.getResourceAsStream("/prompts/$name")
            ?: error("Missing prompt file: prompts/$name")
        return stream.bufferedReader(Charsets.UTF_8).use { it.readText() }.trim()
    }
}
