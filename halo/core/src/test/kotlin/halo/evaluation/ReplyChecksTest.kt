package halo.evaluation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReplyChecksTest {
    private val good =
        "다 하면 된다는 말에는 시간이 충분하다는 전제가 숨어 있네요. 막막하다는 건 그 전제가 흔들린다는 신호예요. " +
            "저는 뺄 것부터 정하는 게 먼저라고 봐요. 조용히 사라져도 아무도 모를 일이 하나 있다면 뭘까요?"

    @Test
    fun `a reply that follows the rules passes every check`() {
        assertEquals(4, ReplyChecks.sentenceCount(good))
        assertTrue(ReplyChecks.forThought(good).all { it.passed })
    }

    @Test
    fun `lists, emoji and missing questions are caught`() {
        val bad = "정리해볼게요.\n- 첫째, 일을 줄이세요.\n- 둘째, 쉬세요. 😊"

        val failed = ReplyChecks.forThought(bad).filterNot { it.passed }.map { it.rule }

        assertEquals(listOf("질문으로 끝남", "목록·마크다운 없음", "이모지 없음"), failed)
    }

    @Test
    fun `test cases have unique ids`() {
        val ids = TestCases.all.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }
}
