package org.emakotlin

internal object PartialMethodsPool {
    private data class PartialMethod(
        val obj: DynamicObject,
        val methodName: String,
        val impl: DynamicMethod,
        val originalLayer: LayerSpec
    )

    private val partialMethods = mutableListOf<PartialMethod>()

    fun init() {
        partialMethods.clear()
    }

    fun add(obj: DynamicObject, methodName: String, partialMethodImpl: DynamicMethod, originalLayer: LayerSpec) {
        partialMethods.add(PartialMethod(obj, methodName, partialMethodImpl, originalLayer))
    }

    fun forEachByLayer(deployedLayer: Layer, functor: (DynamicObject, String, DynamicMethod, LayerSpec) -> Unit) {
        partialMethods
            .filter { partialMethod ->
                deployedLayer.originalLayer === partialMethod.originalLayer
            }
            .forEach { partialMethod ->
                functor(partialMethod.obj, partialMethod.methodName, partialMethod.impl, partialMethod.originalLayer)
            }
    }
}
