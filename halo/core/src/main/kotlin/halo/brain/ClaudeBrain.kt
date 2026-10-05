package halo.brain

import com.anthropic.client.AnthropicClient
import com.anthropic.client.okhttp.AnthropicOkHttpClient
import com.anthropic.core.JsonValue
import com.anthropic.models.messages.CacheControlEphemeral
import com.anthropic.models.messages.ContentBlockParam
import com.anthropic.models.messages.Message
import com.anthropic.models.messages.MessageCreateParams
import com.anthropic.models.messages.MessageParam
import com.anthropic.models.messages.OutputConfig
import com.anthropic.models.messages.StopReason
import com.anthropic.models.messages.TextBlockParam
import com.anthropic.models.messages.Tool
import com.anthropic.models.messages.ToolResultBlockParam
import halo.config.Effort
import halo.config.HaloConfig
import halo.memory.Exchange
import halo.prompts.SystemPrompt
import halo.tools.HaloTool
import halo.tools.ToolRegistry

/** Halo's brain on the Claude API, running the tool loop until Claude has an answer. */
class ClaudeBrain(
    private val client: AnthropicClient,
    private val config: HaloConfig = HaloConfig(),
) : Brain {
    constructor(apiKey: String, config: HaloConfig = HaloConfig()) :
        this(AnthropicOkHttpClient.builder().apiKey(apiKey).build(), config)

    override fun reply(system: SystemPrompt, history: List<Exchange>, userText: String, tools: ToolRegistry): String {
        val messages = mutableListOf<MessageParam>()
        for (exchange in history) {
            messages += textMessage(MessageParam.Role.USER, exchange.user)
            messages += textMessage(MessageParam.Role.ASSISTANT, exchange.halo)
        }
        messages += textMessage(MessageParam.Role.USER, userText)

        repeat(config.maxToolRounds + 1) {
            val response = client.messages().create(request(system, messages, tools))
            val stopReason = response.stopReason().orElse(null)
            if (stopReason == StopReason.REFUSAL) return REFUSAL_REPLY

            val toolUses = response.content().mapNotNull { it.toolUse().orElse(null) }
            if (stopReason != StopReason.TOOL_USE || toolUses.isEmpty()) return spokenText(response)

            // Append Claude's turn unchanged so its thinking blocks stay valid for the next round.
            messages += response.toParam()
            val results = toolUses.map { use ->
                @Suppress("UNCHECKED_CAST")
                val input = use._input().convert(Map::class.java) as? Map<String, Any?> ?: emptyMap()
                val outcome = tools.run(use.name(), input)
                ContentBlockParam.ofToolResult(
                    ToolResultBlockParam.builder()
                        .toolUseId(use.id())
                        .content(outcome.text)
                        .isError(outcome.isError)
                        .build(),
                )
            }
            messages += MessageParam.builder().role(MessageParam.Role.USER).contentOfBlockParams(results).build()
        }
        return TOO_MANY_ROUNDS_REPLY
    }

    private fun request(system: SystemPrompt, messages: List<MessageParam>, tools: ToolRegistry): MessageCreateParams {
        val builder = MessageCreateParams.builder()
            .model(config.model)
            .maxTokens(config.maxTokens)
            .outputConfig(OutputConfig.builder().effort(config.effort.toSdk()).build())
            .systemOfTextBlockParams(systemBlocks(system))
            .messages(messages)
        tools.all().forEach { builder.addTool(toolDefinition(it)) }
        return builder.build()
    }

    private fun systemBlocks(system: SystemPrompt): List<TextBlockParam> {
        // The persona and rules never change between requests, so they are cached;
        // the notebook comes after the cache point because it grows over time.
        val blocks = mutableListOf(
            TextBlockParam.builder()
                .text(system.stable)
                .cacheControl(CacheControlEphemeral.builder().build())
                .build(),
        )
        if (system.notebook.isNotBlank()) {
            blocks += TextBlockParam.builder().text("# Halo 수첩\n" + system.notebook).build()
        }
        return blocks
    }

    private fun toolDefinition(tool: HaloTool): Tool {
        val properties = Tool.InputSchema.Properties.builder()
        tool.parameters.forEach { (name, schema) -> properties.putAdditionalProperty(name, JsonValue.from(schema)) }
        return Tool.builder()
            .name(tool.name)
            .description(tool.description)
            .inputSchema(
                Tool.InputSchema.builder()
                    .properties(properties.build())
                    .required(tool.required)
                    .build(),
            )
            .build()
    }

    private fun spokenText(response: Message): String =
        response.content()
            .mapNotNull { it.text().orElse(null)?.text() }
            .joinToString("\n")
            .trim()
            .ifEmpty { EMPTY_REPLY }

    private fun textMessage(role: MessageParam.Role, text: String): MessageParam =
        MessageParam.builder().role(role).content(text).build()

    private fun Effort.toSdk(): OutputConfig.Effort = when (this) {
        Effort.LOW -> OutputConfig.Effort.LOW
        Effort.MEDIUM -> OutputConfig.Effort.MEDIUM
        Effort.HIGH -> OutputConfig.Effort.HIGH
    }

    private companion object {
        const val REFUSAL_REPLY = "그 이야기는 제가 도와드리기 어려워요. 다른 방향으로 이야기해볼까요?"
        const val TOO_MANY_ROUNDS_REPLY = "생각이 너무 길어져서 여기서 멈출게요. 조금 더 간단히 다시 말씀해주시겠어요?"
        const val EMPTY_REPLY = "음, 할 말을 찾지 못했어요. 다시 한번 말씀해주시겠어요?"
    }
}
