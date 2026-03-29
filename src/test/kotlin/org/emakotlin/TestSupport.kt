package org.emakotlin

internal fun layerSpec(
    condition: Any? = null,
    enter: () -> Unit = {},
    exit: () -> Unit = {},
    name: String = "_"
): LayerSpec = LayerSpec(condition = condition, enter = enter, exit = exit, name = name)

internal fun dynamicObject(init: DynamicObject.() -> Unit): DynamicObject {
    return DynamicObject().apply(init)
}

internal fun Any?.asDouble(): Double {
    return when (this) {
        is Number -> this.toDouble()
        is Boolean -> if (this) 1.0 else 0.0
        else -> error("Cannot cast value '$this' to Double")
    }
}
