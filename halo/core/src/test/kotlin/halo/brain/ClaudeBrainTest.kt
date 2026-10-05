package halo.brain

import com.anthropic.client.okhttp.AnthropicOkHttpClient
import com.sun.net.httpserver.HttpServer
import halo.config.HaloConfig
import halo.memory.Exchange
import halo.prompts.SystemPrompt
import halo.tools.ToolRegistry
import halo.tools.builtin.CurrentTimeTool
import java.net.InetSocketAddress
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Runs the real Claude client against a local server that answers like the Messages API. */
class ClaudeBrainTest {
    private lateinit var server: HttpServer
    private val requests = mutableListOf<String>()
    private val responses = ArrayDeque<String>()

    private val tools = ToolRegistry(
        listOf(CurrentTimeTool(Clock.fixed(Instant.parse("2026-10-05T13:51:00Z"), ZoneId.of("Asia/Seoul")))),
    )
    private val system = SystemPrompt(stable = "# 너는 Halo야", notebook = "- 고양이를 키운다")

    @BeforeTest
    fun start() {
        server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/v1/messages") { exchange ->
            requests += exchange.requestBody.readBytes().decodeToString()
            val body = responses.removeFirst().encodeToByteArray()
            exchange.responseHeaders.add("Content-Type", "application/json")
            exchange.sendResponseHeaders(200, body.size.toLong())
            exchange.responseBody.use { it.write(body) }
        }
        server.start()
    }

    @AfterTest
    fun stop() = server.stop(0)

    private fun brain() = ClaudeBrain(
        AnthropicOkHttpClient.builder()
            .apiKey("test-key")
            .baseUrl("http://127.0.0.1:${server.address.port}")
            .maxRetries(0)
            .build(),
        HaloConfig(),
    )

    private fun message(stopReason: String, content: String) =
        """{"id":"msg_1","type":"message","role":"assistant","model":"claude-opus-5-5",
           "content":[$content],"stop_reason":"$stopReason","stop_sequence":null,
           "usage":{"input_tokens":10,"output_tokens":5}}"""

    @Test
    fun `runs a tool and sends its result back before answering`() {
        responses += message("tool_use", """{"type":"tool_use","id":"toolu_1","name":"current_time","input":{}}""")
        responses += message("end_turn", """{"type":"text","text":"지금은 오후 10시 51분이에요."}""")

        val reply = brain().reply(system, listOf(Exchange("안녕", "안녕하세요.")), "지금 몇 시야?", tools)

        assertEquals("지금은 오후 10시 51분이에요.", reply)
        assertEquals(2, requests.size)
        val first = requests[0]
        assertTrue(first.contains("\"model\":\"claude-opus-5-5\""), first)
        assertTrue(first.contains("\"effort\":\"low\""), first)
        assertTrue(first.contains("\"cache_control\""), first)
        assertTrue(first.contains("# Halo 수첩"), first)
        assertTrue(first.contains("\"name\":\"current_time\""), first)
        assertTrue(first.contains("안녕하세요."), first)
        val second = requests[1]
        assertTrue(second.contains("\"tool_use_id\":\"toolu_1\""), second)
        assertTrue(second.contains("2026년 10월 5일 월요일 오후 10:51"), second)
    }

    @Test
    fun `a refusal becomes a polite spoken reply`() {
        responses += message("refusal", "")

        val reply = brain().reply(system, emptyList(), "...", tools)

        assertTrue(reply.startsWith("그 이야기는 제가 도와드리기 어려워요"))
    }

    @Test
    fun `stops after too many tool rounds`() {
        val toolUse = message("tool_use", """{"type":"tool_use","id":"toolu_x","name":"current_time","input":{}}""")
        repeat(HaloConfig().maxToolRounds + 1) { responses += toolUse }

        val reply = brain().reply(system, emptyList(), "계속 시간 봐줘", tools)

        assertTrue(reply.startsWith("생각이 너무 길어져서"))
        assertEquals(HaloConfig().maxToolRounds + 1, requests.size)
    }
}
