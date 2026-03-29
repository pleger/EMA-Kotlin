package org.emakotlin

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class LayerConditionTest {
    @Test
    fun create1() {
        val adap = Layer(layerSpec(condition = "a > 10"))

        assertNotNull(adap.condition)
    }

    @Test
    fun eval1() {
        val adap = Layer(layerSpec(condition = "a > 10"))

        assertEquals(false, adap.isActive())
    }

    @Test
    fun eval2() {
        val s = Signal(0, "a")
        val cond = SignalComp("a > 10", listOf(s))
        val adap = Layer(layerSpec(condition = cond))

        assertEquals(false, adap.isActive())
        s.mutableValue = 20
        assertEquals(true, adap.isActive())
    }

    @Test
    fun eval3() {
        val s1 = Signal(0, "a")
        val s2 = Signal(0, "b")
        val s3 = Signal(0, "c")

        val cond = SignalComp("a > 10 && b > c", listOf(s1, s2, s3))
        val adap = Layer(layerSpec(condition = cond))

        assertEquals(false, adap.isActive())
        s1.mutableValue = 20
        assertEquals(false, adap.isActive())
        s2.mutableValue = 300
        assertEquals(true, adap.isActive())
        s3.mutableValue = 200
        assertEquals(true, adap.isActive())
    }
}
