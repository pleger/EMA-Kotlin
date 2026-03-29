package org.emakotlin

internal object OriginalMethodsPool {
    private val originalMethods = mutableListOf<Triple<DynamicObject, String, DynamicMethod>>()

    fun init() {
        originalMethods.clear()
    }

    fun add(obj: DynamicObject, methodName: String) {
        if (get(obj, methodName) == null) {
            val originalMethod = obj.getMethod(methodName) ?: error("Method '$methodName' is not defined")
            originalMethods.add(Triple(obj, methodName, originalMethod))
        }
    }

    fun get(obj: DynamicObject, methodName: String): DynamicMethod? {
        return originalMethods.firstOrNull { (target, name, _) ->
            target === obj && name == methodName
        }?.third
    }
}
