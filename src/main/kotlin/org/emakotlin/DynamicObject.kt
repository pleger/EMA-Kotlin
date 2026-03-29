package org.emakotlin

typealias DynamicMethod = DynamicObject.(Array<out Any?>) -> Any?

class DynamicObject {
    private val slots = linkedMapOf<String, Any?>()

    fun set(name: String, value: Any?): DynamicObject {
        slots[name] = value
        return this
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> get(name: String): T = slots[name] as T

    fun defineMethod(name: String, method: DynamicMethod): DynamicObject {
        slots[name] = method
        return this
    }

    fun getMethod(name: String): DynamicMethod? {
        return slots[name] as? DynamicMethod
    }

    fun setMethod(name: String, method: DynamicMethod) {
        slots[name] = method
    }

    fun call(name: String, vararg args: Any?): Any? {
        val method = getMethod(name) ?: error("Method '$name' not found")
        return method.invoke(this, args)
    }
}
