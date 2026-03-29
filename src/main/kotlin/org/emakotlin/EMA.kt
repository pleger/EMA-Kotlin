package org.emakotlin

object EMA {
    private val deployedLayers = mutableListOf<Layer>()
    private val signalInterfacePool = mutableListOf<Pair<Any, Map<String, ObservableSignal>>>()

    fun init() {
        deployedLayers.clear()
        signalInterfacePool.clear()
        OriginalMethodsPool.init()
        PartialMethodsPool.init()
    }

    fun deploy(originalLayer: LayerSpec) {
        val layer = Layer(originalLayer)
        if (layer.name == "_") {
            layer.name = "Layer_${deployedLayers.size + 1}"
        }
        deployedLayers.add(layer)
        receiveSignalsForSignalInterfaces(layer)
    }

    fun undeploy(originalLayer: LayerSpec) {
        val iterator = deployedLayers.iterator()
        while (iterator.hasNext()) {
            val layer = iterator.next()
            if (layer.originalLayer === originalLayer) {
                layer.cleanCondition()
                layer.uninstallPartialMethods()
                iterator.remove()
            }
        }
    }

    fun exhibit(obj: Any, signalInterface: Map<String, ObservableSignal>) {
        signalInterfacePool.add(obj to signalInterface)
        addIdSignal(signalInterface)
        exhibitAnInterface(signalInterface)
    }

    fun addPartialMethod(
        originalLayer: LayerSpec,
        objs: DynamicObject,
        methodName: String,
        partialMethodImpl: DynamicMethod
    ) {
        addPartialMethod(originalLayer, listOf(objs), methodName, partialMethodImpl)
    }

    fun addPartialMethod(
        originalLayer: LayerSpec,
        objs: List<DynamicObject>,
        methodName: String,
        partialMethodImpl: DynamicMethod
    ) {
        objs.forEach { obj ->
            OriginalMethodsPool.add(obj, methodName)
            PartialMethodsPool.add(obj, methodName, partialMethodImpl, originalLayer)
        }
    }

    fun getLayers(filter: (Layer) -> Boolean = { true }): List<Layer> {
        return deployedLayers.filter(filter)
    }

    fun getActiveLayers(): List<Layer> = getLayers { layer -> layer.isActive() }

    fun getInactiveLayers(): List<Layer> = getLayers { layer -> !layer.isActive() }

    fun activate(originalLayer: LayerSpec) {
        getLayers { layer -> layer.name == originalLayer.name }
            .forEach { layer -> layer.forceActivateForTesting() }
    }

    fun deactivate(originalLayer: LayerSpec) {
        getLayers { layer -> layer.name == originalLayer.name }
            .forEach { layer -> layer.forceDeactivateForTesting() }
    }

    private fun receiveSignalsForSignalInterfaces(deployedLayer: Layer) {
        signalInterfacePool.forEach { (_, signalInterface) ->
            signalInterface.values.forEach { signal ->
                deployedLayer.addSignal(signal)
            }
        }
    }

    private fun addIdSignal(signalInterface: Map<String, ObservableSignal>) {
        signalInterface.forEach { (field, signal) ->
            signal.id = field
        }
    }

    private fun exhibitAnInterface(signalInterface: Map<String, ObservableSignal>) {
        signalInterface.values.forEach { signal ->
            deployedLayers.forEach { layer ->
                layer.addSignal(signal)
            }
        }
    }
}
