package org.emakotlin

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AdditionalCoverageTest {
    @BeforeTest
    fun setUp() {
        EMA.init()
    }

    @Test
    fun expressionRespectsArithmeticPrecedence() {
        val cond = SignalComp("a + 2 * b", listOf(Signal(10, "a"), Signal(3, "b")))

        assertEquals(16.0, cond.evaluate().asDouble())
    }

    @Test
    fun expressionShortCircuitsUnknownVariableOnOr() {
        val cond = SignalComp("true || missing > 1")

        assertEquals(true, cond.evaluate())
    }

    @Test
    fun proceedFailsOutsidePartialMethod() {
        assertFailsWith<IllegalStateException> {
            Layer.proceed()
        }
    }

    @Test
    fun undeployLeavesOtherLayersActive() {
        val x = Signal(0, "a")
        val layer1 = layerSpec(condition = "a > 5")
        val layer2 = layerSpec(condition = "a > 10")

        EMA.exhibit("obj", mapOf("a" to x))
        EMA.deploy(layer1)
        EMA.deploy(layer2)
        x.mutableValue = 20

        assertEquals(2, EMA.getActiveLayers().size)

        EMA.undeploy(layer1)

        assertEquals(1, EMA.getActiveLayers().size)
    }
}
