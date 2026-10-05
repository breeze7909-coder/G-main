package halo.memory

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MemoryTest {
    @Test
    fun `short-term memory keeps only the latest exchanges`() {
        val memory = ShortTermMemory(maxExchanges = 2)
        memory.add(Exchange("1", "a"))
        memory.add(Exchange("2", "b"))
        memory.add(Exchange("3", "c"))

        assertEquals(listOf(Exchange("2", "b"), Exchange("3", "c")), memory.recent())
    }

    @Test
    fun `notebook ignores blank and repeated facts`() {
        val notebook = InMemoryNotebook()

        assertTrue(notebook.write("  아침형 인간  "))
        assertFalse(notebook.write("아침형 인간"))
        assertFalse(notebook.write("   "))
        assertTrue(notebook.write("고양이를 키운다"))

        assertEquals("- 아침형 인간\n- 고양이를 키운다", notebook.render())
    }
}
