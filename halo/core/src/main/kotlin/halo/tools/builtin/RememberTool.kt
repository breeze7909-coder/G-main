package halo.tools.builtin

import halo.memory.Notebook
import halo.tools.HaloTool

/** Lets Claude write a lasting fact about the user into the Halo notebook. */
class RememberTool(private val notebook: Notebook) : HaloTool {
    override val name = "remember"
    override val description =
        "Writes one short, lasting fact about the user into the Halo notebook, " +
            "e.g. 사용자는 아침형 인간이다. Returns whether it was saved."
    override val parameters = mapOf(
        "fact" to mapOf<String, Any>("type" to "string", "description" to "The fact to remember, one sentence."),
    )
    override val required = listOf("fact")
    override val changesSomething = false

    override fun run(input: Map<String, Any?>): String {
        val fact = input["fact"] as? String ?: throw IllegalArgumentException("fact must be a string")
        return if (notebook.write(fact)) "Saved." else "Already in the notebook."
    }
}
