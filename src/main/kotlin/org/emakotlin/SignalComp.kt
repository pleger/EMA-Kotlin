package org.emakotlin

class SignalComp(
    val expression: String,
    signals: List<ObservableSignal> = emptyList(),
    override var id: String = "_"
) : ObservableSignal {
    private val subscribers = mutableListOf<SignalSubscriber>()
    private val trackedSignals = mutableListOf<ObservableSignal>()

    private var lastValue: Any? = UNSET

    override var timestamp: Long = System.nanoTime()
        private set

    override var value: Any? = null
        private set

    init {
        trackedSignals.addAll(signals)
        enableSignals()
        // EMA-js resets _lastVal after wiring initial signals.
        // This preserves identical emission behavior across the original test suite.
        lastValue = UNSET
    }

    fun addSignal(signal: ObservableSignal) {
        if (isInExpression(signal.id)) {
            trackedSignals.add(signal)
            enableSignal(signal)
        }
    }

    fun evaluate(): Any? {
        val context = prepareConditionContext()
        value = ExpressionInterpreter.evaluate(expression, context)
        timestamp = System.nanoTime()

        if (value != lastValue) {
            lastValue = value
            emit()
        }

        return value
    }

    override fun on(subscriber: SignalSubscriber) {
        subscribers.add(subscriber)
    }

    private fun emit() {
        val emittedValue = value
        val emittedId = id
        subscribers.forEach { subscriber ->
            subscriber.on(emittedValue, emittedId)
        }
    }

    private fun enableSignals() {
        trackedSignals.forEach { signal ->
            enableSignal(signal)
        }
    }

    private fun enableSignal(signal: ObservableSignal) {
        signal.on(SignalSubscriber { _, _ -> evaluate() })
        evaluate()
    }

    private fun isInExpression(candidateId: String): Boolean {
        val variables = VARIABLE_REGEX.findAll(expression).map { match -> match.value }.toList()
        return variables.contains(candidateId)
    }

    private fun prepareConditionContext(): Map<String, Any?> {
        val sortedSignals = trackedSignals.sortedBy { signal -> signal.timestamp }
        val context = linkedMapOf<String, Any?>()

        if (id != "_") {
            context[id] = value
        }

        sortedSignals.forEach { signal ->
            context[signal.id] = signal.value
        }

        return context
    }

    companion object {
        private val VARIABLE_REGEX = Regex("[a-zA-Z][a-zA-Z0-9_+\\-]*")
        private val UNSET = Any()
    }
}
