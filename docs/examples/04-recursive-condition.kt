// Example 4: higher-order / recursive condition
val motion = Signal(0, "m")
val occupied = SignalComp("m > 0", listOf(motion), "occ")
val sticky = SignalComp("sticky || occ", listOf(occupied), "sticky")

motion.mutableValue = 1
println("occupied=${occupied.value}, sticky=${sticky.value}")
motion.mutableValue = 0
println("occupied=${occupied.value}, sticky=${sticky.value}")
