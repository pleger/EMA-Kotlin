// Example 1: signal composition
val temperature = Signal(17, "t")
val humidity = Signal(55, "h")
val comfort = SignalComp("t > 20 && h < 70", listOf(temperature, humidity))

println("active? ${comfort.value}")
temperature.mutableValue = 24
println("active? ${comfort.value}")
