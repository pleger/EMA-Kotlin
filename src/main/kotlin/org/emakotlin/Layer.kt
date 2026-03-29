package org.emakotlin

class Layer internal constructor(internal val originalLayer: LayerSpec) {
    private var cond: SignalComp = when (val candidate = originalLayer.condition) {
        null -> SignalComp("false")
        is String -> SignalComp(candidate)
        is SignalComp -> candidate
        else -> throw IllegalArgumentException("Unsupported condition type: ${candidate::class.qualifiedName}")
    }

    private val enterAction: () -> Unit = originalLayer.enter
    private val exitAction: () -> Unit = originalLayer.exit

    private var active = false
    private var internalName = originalLayer.name

    var name: String
        get() = internalName
        set(value) {
            internalName = value
        }

    val condition: SignalComp
        get() = cond

    init {
        enableCondition()
    }

    fun cleanCondition() {
        cond = SignalComp(cond.expression)
        enableCondition()
    }

    fun addSignal(signal: ObservableSignal) {
        cond.addSignal(signal)
    }

    fun isActive(): Boolean = active

    internal fun installPartialMethod() {
        PartialMethodsPool.forEachByLayer(this) { obj, methodName, partialMethodImpl, _ ->
            obj.setMethod(methodName) { args ->
                proceedHandler = { proceedArgs -> executeOriginalMethod(obj, methodName, proceedArgs) }
                try {
                    partialMethodImpl.invoke(obj, args)
                } finally {
                    proceedHandler = null
                }
            }
        }
    }

    internal fun uninstallPartialMethods() {
        PartialMethodsPool.forEachByLayer(this) { obj, methodName, _, _ ->
            val originalMethod = OriginalMethodsPool.get(obj, methodName)
                ?: error("No original method found for '$methodName'")
            obj.setMethod(methodName, originalMethod)
        }
    }

    internal fun forceActivateForTesting() {
        if (!active) {
            active = true
            enterAction()
            installPartialMethod()
        }
    }

    internal fun forceDeactivateForTesting() {
        if (active) {
            active = false
            exitAction()
            cleanCondition()
            uninstallPartialMethods()
        }
    }

    private fun enableCondition() {
        cond.on(SignalSubscriber { value, _ ->
            val shouldActivate = JsTruthiness.isTruthy(value)
            if (shouldActivate != active) {
                active = shouldActivate
                if (active) {
                    enterAction()
                    installPartialMethod()
                } else {
                    exitAction()
                    uninstallPartialMethods()
                }
            }
        })
    }

    companion object {
        private var proceedHandler: ((Array<out Any?>) -> Any?)? = null

        fun proceed(vararg args: Any?): Any? {
            val handler = proceedHandler ?: error("No proceed function available")
            return handler(args)
        }

        private fun executeOriginalMethod(obj: DynamicObject, methodName: String, args: Array<out Any?>): Any? {
            val originalMethod = OriginalMethodsPool.get(obj, methodName) ?: error("No original method found")
            return originalMethod.invoke(obj, args)
        }
    }
}
