package halo.memory

/** One back-and-forth: what the user said and what Halo answered. */
data class Exchange(val user: String, val halo: String)

/** The recent conversation, oldest first, capped at [maxExchanges]. */
class ShortTermMemory(private val maxExchanges: Int) {
    init {
        require(maxExchanges > 0) { "maxExchanges must be positive" }
    }

    private val exchanges = ArrayDeque<Exchange>()

    fun add(exchange: Exchange) {
        exchanges.addLast(exchange)
        while (exchanges.size > maxExchanges) exchanges.removeFirst()
    }

    fun recent(): List<Exchange> = exchanges.toList()

    fun clear() = exchanges.clear()
}
