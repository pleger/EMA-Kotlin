package org.emakotlin

class Signal(initialValue: Any?, override var id: String = "_") : ObservableSignal {
    private val subscribers = mutableListOf<SignalSubscriber>()
    private var lastValue: Any? = UNSET
    private var currentValue: Any? = null

    override var timestamp: Long = System.nanoTime()
        private set

    override val value: Any?
        get() = currentValue

    var rawValue: Any?
        get() = currentValue
        set(newValue) {
            currentValue = newValue
        }

    var mutableValue: Any?
        get() = currentValue
        set(newValue) {
            if (lastValue != newValue) {
                currentValue = newValue
                timestamp = System.nanoTime()
                emit()
                lastValue = currentValue
            }
        }

    init {
        mutableValue = initialValue
    }

    override fun on(subscriber: SignalSubscriber) {
        subscribers.add(subscriber)
    }

    private fun emit() {
        val emittedValue = currentValue
        val emittedId = id
        subscribers.forEach { subscriber ->
            subscriber.on(emittedValue, emittedId)
        }
    }

    companion object {
        private val UNSET = Any()
    }
}
