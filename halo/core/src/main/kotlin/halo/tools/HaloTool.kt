package halo.tools

/** Something Halo can do besides talking, offered to Claude as a tool. */
interface HaloTool {
    val name: String

    /** What the tool does and returns. This is all Claude knows about it. */
    val description: String

    /** JSON Schema for each input field, by field name. */
    val parameters: Map<String, Map<String, Any>>

    val required: List<String>

    /** True when running the tool changes the world outside the conversation, so Halo must ask first. */
    val changesSomething: Boolean

    /** Runs the tool. Throw to report a problem; Claude sees the message and carries on. */
    fun run(input: Map<String, Any?>): String
}

/** What a tool run produced, as sent back to Claude. */
data class ToolOutcome(val text: String, val isError: Boolean)

class ToolRegistry(tools: List<HaloTool>) {
    private val byName: Map<String, HaloTool> = tools.associateBy { it.name }

    init {
        require(byName.size == tools.size) { "Tool names must be unique" }
    }

    fun all(): List<HaloTool> = byName.values.toList()

    fun run(name: String, input: Map<String, Any?>): ToolOutcome {
        val tool = byName[name] ?: return ToolOutcome("Unknown tool: $name", isError = true)
        return try {
            ToolOutcome(tool.run(input), isError = false)
        } catch (e: Exception) {
            ToolOutcome("Error: ${e.message ?: e::class.simpleName}", isError = true)
        }
    }
}
