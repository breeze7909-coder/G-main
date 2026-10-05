package halo.evaluation

/** A message to send Halo and what kind of reply it should get. */
data class TestCase(val id: String, val userText: String, val kind: Kind) {
    enum class Kind { THOUGHT, QUICK_QUESTION }
}

/** Sample messages for grading Halo against a real Claude reply. */
object TestCases {
    val all = listOf(
        TestCase("busy", "요즘 일이 너무 많아서 뭐부터 해야 할지 모르겠어. 그냥 다 하면 되겠지?", TestCase.Kind.THOUGHT),
        TestCase("habit", "운동을 매번 작심삼일로 끝내. 의지가 약한 것 같아.", TestCase.Kind.THOUGHT),
        TestCase("career", "회사를 그만두고 내 일을 해보고 싶은데 돈이 걱정돼.", TestCase.Kind.THOUGHT),
        TestCase("dictation", "오늘 회으에서 내 아이디어가 무시 당한거 같아 기분이 별로야", TestCase.Kind.THOUGHT),
        TestCase("time", "지금 몇 시야?", TestCase.Kind.QUICK_QUESTION),
    )
}
