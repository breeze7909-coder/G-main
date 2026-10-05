package halo.tools

import halo.memory.InMemoryNotebook
import halo.tools.builtin.CurrentTimeTool
import halo.tools.builtin.RememberTool
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ToolsTest {
    private val seoulClock = Clock.fixed(Instant.parse("2026-10-05T13:51:00Z"), ZoneId.of("Asia/Seoul"))

    @Test
    fun `current time is spoken in Korean`() {
        assertEquals("2026년 10월 5일 월요일 오후 10:51", CurrentTimeTool(seoulClock).run(emptyMap()))
    }

    @Test
    fun `remember writes to the notebook`() {
        val notebook = InMemoryNotebook()
        val registry = ToolRegistry(listOf(RememberTool(notebook)))

        assertEquals(ToolOutcome("Saved.", isError = false), registry.run("remember", mapOf("fact" to "축구를 좋아한다")))
        assertEquals(listOf("축구를 좋아한다"), notebook.entries())
    }

    @Test
    fun `a failing tool becomes an error result instead of crashing`() {
        val registry = ToolRegistry(listOf(RememberTool(InMemoryNotebook())))

        val outcome = registry.run("remember", mapOf("fact" to 42))

        assertTrue(outcome.isError)
        assertEquals("Error: fact must be a string", outcome.text)
    }

    @Test
    fun `an unknown tool is reported as an error`() {
        val outcome = ToolRegistry(emptyList()).run("fly", emptyMap())
        assertEquals(ToolOutcome("Unknown tool: fly", isError = true), outcome)
    }
}
