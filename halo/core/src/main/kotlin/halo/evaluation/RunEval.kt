package halo.evaluation

import halo.agent.HaloAgent
import halo.brain.ClaudeBrain
import halo.memory.InMemoryNotebook
import halo.memory.ShortTermMemory
import kotlin.system.exitProcess

/**
 * Sends every test case to real Claude and grades the replies with [ReplyChecks].
 * Run with: ANTHROPIC_API_KEY=... ./gradlew :core:eval
 * Each run spends API credit.
 */
fun main() {
    val apiKey = System.getenv("ANTHROPIC_API_KEY")
    if (apiKey.isNullOrBlank()) {
        System.err.println("Set ANTHROPIC_API_KEY to run the evaluation.")
        exitProcess(1)
    }
    val brain = ClaudeBrain(apiKey)
    var passed = 0
    var total = 0
    for (case in TestCases.all) {
        // A fresh Halo per case, so cases don't influence each other.
        val agent = HaloAgent(brain, InMemoryNotebook(), ShortTermMemory(maxExchanges = 10))
        val reply = agent.ask(case.userText)
        val results = when (case.kind) {
            TestCase.Kind.THOUGHT -> ReplyChecks.forThought(reply)
            TestCase.Kind.QUICK_QUESTION -> listOf(ReplyChecks.noMarkdown(reply), ReplyChecks.noEmoji(reply))
        }
        println("[${case.id}] ${case.userText}\n  Halo: $reply")
        results.forEach { println("  ${if (it.passed) "PASS" else "FAIL"} ${it.rule}") }
        passed += results.count { it.passed }
        total += results.size
    }
    println("\n$passed / $total checks passed")
}
