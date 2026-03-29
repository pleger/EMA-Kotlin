// Example 3: partial method with proceed
val player = DynamicObject().apply {
    set("energy", Signal(40))
    defineMethod("speed") { 10 }
}

val turbo = LayerSpec(condition = SignalComp("e > 30"))
EMA.exhibit(player, mapOf("e" to player.get<Signal>("energy")))
EMA.addPartialMethod(turbo, player, "speed") {
    val base = Layer.proceed() as Int
    base + 7
}

println(player.call("speed"))
EMA.deploy(turbo)
println(player.call("speed"))
