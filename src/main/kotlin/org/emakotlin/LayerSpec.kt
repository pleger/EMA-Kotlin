package org.emakotlin

class LayerSpec(
    val condition: Any? = null,
    val enter: () -> Unit = {},
    val exit: () -> Unit = {},
    val name: String = "_"
)
