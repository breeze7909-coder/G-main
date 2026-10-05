package halo.config

/** How deeply Claude thinks before answering. Lower is faster to the first word. */
enum class Effort { LOW, MEDIUM, HIGH }

/** Settings that decide how Halo thinks and talks. */
data class HaloConfig(
    /** Claude model that answers. Cost and speed differ per model; see docs/halo-design.md. */
    val model: String = "claude-opus-5-5",
    /** Upper bound on one reply, thinking included. */
    val maxTokens: Long = 16_000,
    /** Spoken replies should start quickly, so everyday talk thinks lightly. */
    val effort: Effort = Effort.LOW,
    /** How many past exchanges are sent along with each new message. */
    val historyExchanges: Int = 10,
    /** Most tool rounds Claude may take for one message before Halo stops. */
    val maxToolRounds: Int = 5,
)
