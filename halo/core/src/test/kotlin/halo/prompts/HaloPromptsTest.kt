package halo.prompts

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HaloPromptsTest {
    @Test
    fun `system prompt combines persona and rules and carries the notebook separately`() {
        val system = HaloPrompts.system("- 고양이를 키운다")

        assertTrue(system.stable.startsWith("# 너는 Halo야"))
        assertTrue(system.stable.contains("# 대화 규칙"))
        assertTrue(system.stable.contains("remember"))
        assertEquals("- 고양이를 키운다", system.notebook)
    }
}
