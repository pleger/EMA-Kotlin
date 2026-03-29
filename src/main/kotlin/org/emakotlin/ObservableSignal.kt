package org.emakotlin

fun interface SignalSubscriber {
    fun on(value: Any?, id: String)
}

interface ObservableSignal {
    var id: String
    val value: Any?
    val timestamp: Long
    fun on(subscriber: SignalSubscriber)
}
