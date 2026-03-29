// Example 2: layer enter/exit hooks
val x = Signal(0)
val calls = mutableListOf<String>()

val layer = LayerSpec(
    condition = "a > 10",
    enter = { calls += "enter" },
    exit = { calls += "exit" }
)

EMA.deploy(layer)
EMA.exhibit("obj", mapOf("a" to x))
x.mutableValue = 15
x.mutableValue = 1

println(calls)
