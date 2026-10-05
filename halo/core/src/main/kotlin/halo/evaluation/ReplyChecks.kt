package halo.evaluation

/** One rule a spoken reply should follow, and whether a reply passed it. */
data class CheckResult(val rule: String, val passed: Boolean)

/**
 * Automatic checks for the conversation rules in prompts/conversation.md.
 * They grade a real reply from Claude; they cannot judge how insightful it is.
 */
object ReplyChecks {
    private val markdown = Regex("""(^|\n)\s*([-*#]|\d+\.)\s|\*\*|`""")
    private val emoji = Regex("""[\x{1F300}-\x{1FAFF}\x{2600}-\x{27BF}]""")
    private val sentenceEnd = Regex("""[.?!]+(?=\s|$)""")

    fun endsWithQuestion(reply: String) = CheckResult("질문으로 끝남", reply.trimEnd().endsWith("?"))

    fun noMarkdown(reply: String) = CheckResult("목록·마크다운 없음", !markdown.containsMatchIn(reply))

    fun noEmoji(reply: String) = CheckResult("이모지 없음", !emoji.containsMatchIn(reply))

    fun sentenceCount(reply: String): Int = sentenceEnd.findAll(reply.trim()).count()

    fun lengthFits(reply: String, range: IntRange = 3..5) =
        CheckResult("${range.first}~${range.last}문장", sentenceCount(reply) in range)

    /** Rules for a reply to the user sharing a thought. */
    fun forThought(reply: String): List<CheckResult> =
        listOf(endsWithQuestion(reply), noMarkdown(reply), noEmoji(reply), lengthFits(reply))
}
