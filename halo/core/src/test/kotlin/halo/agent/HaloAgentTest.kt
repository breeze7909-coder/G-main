package halo.agent

import halo.brain.Brain
import halo.memory.Exchange
import halo.memory.InMemoryNotebook
import halo.memory.ShortTermMemory
import halo.prompts.SystemPrompt
import halo.tools.ToolRegistry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

private class RecordingBrain : Brain {
    val calls = mutableListOf<Triple<SystemPrompt, List<Exchange>, String>>()
    var toolNames: List<String> = emptyList()

    override fun reply(system: SystemPrompt, history: List<Exchange>, userText: String, tools: ToolRegistry): String {
        calls += Triple(system, history, userText)
        toolNames = tools.all().map { it.name }
        return "답 ${calls.size}"
    }
}

class HaloAgentTest {
    @Test
    fun `sends earlier exchanges with each new message`() {
        val brain = RecordingBrain()
        val agent = HaloAgent(brain, InMemoryNotebook(), ShortTermMemory(maxExchanges = 10))

        assertEquals("답 1", agent.ask("안녕"))
        assertEquals("답 2", agent.ask("  오늘 어땠어?  "))

        assertEquals(emptyList(), brain.calls[0].second)
        assertEquals(listOf(Exchange("안녕", "답 1")), brain.calls[1].second)
        assertEquals("오늘 어땠어?", brain.calls[1].third)
    }

    @Test
    fun `includes the notebook in the system prompt`() {
        val brain = RecordingBrain()
        val agent = HaloAgent(brain, InMemoryNotebook(listOf("사용자는 커피를 좋아한다")), ShortTermMemory(10))

        agent.ask("뭐 마실까?")

        val system = brain.calls.single().first
        assertEquals("- 사용자는 커피를 좋아한다", system.notebook)
        assertTrue(system.stable.contains("Halo"))
    }

    @Test
    fun `offers the built-in tools`() {
        val brain = RecordingBrain()
        HaloAgent(brain, InMemoryNotebook(), ShortTermMemory(10)).ask("몇 시야?")

        assertEquals(setOf("current_time", "remember"), brain.toolNames.toSet())
    }

    @Test
    fun `reset forgets the conversation but keeps the notebook`() {
        val brain = RecordingBrain()
        val notebook = InMemoryNotebook(listOf("이름은 민수"))
        val agent = HaloAgent(brain, notebook, ShortTermMemory(10))

        agent.ask("첫 번째")
        agent.reset()
        agent.ask("두 번째")

        assertEquals(emptyList(), brain.calls[1].second)
        assertEquals("- 이름은 민수", brain.calls[1].first.notebook)
    }

    @Test
    fun `rejects blank messages`() {
        val agent = HaloAgent(RecordingBrain(), InMemoryNotebook(), ShortTermMemory(10))
        assertFailsWith<IllegalArgumentException> { agent.ask("   ") }
    }
}
