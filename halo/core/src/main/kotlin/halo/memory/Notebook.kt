package halo.memory

/** The Halo notebook: lasting facts about the user, sent with every conversation. */
interface Notebook {
    fun entries(): List<String>

    /** Adds [fact]; returns false when it was empty or already written. */
    fun write(fact: String): Boolean

    fun render(): String = entries().joinToString("\n") { "- $it" }
}

/** Keeps the notebook in memory only. The Android app will provide a persistent one. */
class InMemoryNotebook(initial: List<String> = emptyList()) : Notebook {
    private val facts = initial.map { it.trim() }.filter { it.isNotEmpty() }.distinct().toMutableList()

    override fun entries(): List<String> = facts.toList()

    override fun write(fact: String): Boolean {
        val clean = fact.trim()
        if (clean.isEmpty() || clean in facts) return false
        facts += clean
        return true
    }
}
