package halo.tools.builtin

import halo.tools.HaloTool
import java.time.Clock
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Tells Claude the current date and time on the phone. */
class CurrentTimeTool(private val clock: Clock = Clock.systemDefaultZone()) : HaloTool {
    override val name = "current_time"
    override val description =
        "Returns the current date, weekday and time where the user is, e.g. 2026년 10월 5일 월요일 오후 10:51. " +
            "Use it for anything that depends on today or now."
    override val parameters = emptyMap<String, Map<String, Any>>()
    override val required = emptyList<String>()
    override val changesSomething = false

    override fun run(input: Map<String, Any?>): String =
        ZonedDateTime.now(clock).format(FORMAT)

    private companion object {
        val FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일 EEEE a h:mm", Locale.KOREAN)
    }
}
