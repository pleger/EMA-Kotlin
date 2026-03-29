package org.emakotlin

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LayerActivationMutualRecursionTest {
    @BeforeTest
    fun setUp() {
        EMA.init()
    }

    @Test
    fun mutualRecursion1() {
        val flags = mutableListOf<String>()
        val x = Signal(0)
        val adap1Cond = SignalComp("a > 5")
        val adap1 = layerSpec(
            condition = adap1Cond,
            enter = { flags += "adap1" }
        )
        val adap2Cond = SignalComp("a < 10")
        val adap2 = layerSpec(
            condition = adap2Cond,
            enter = { flags += "adap2" }
        )

        EMA.exhibit(listOf(adap1, adap2), mapOf("a" to x))
        EMA.deploy(adap1)
        EMA.deploy(adap2)
        x.mutableValue = 9

        assertEquals(listOf("adap2", "adap1"), flags)
    }

    @Test
    fun mutualRecursion2() {
        val flags = mutableListOf<String>()
        val x = Signal(0)
        val adap1Cond = SignalComp("a > 5")
        val adap1 = layerSpec(
            condition = adap1Cond,
            enter = { flags += "adap1" }
        )
        val adap2Cond = SignalComp("a < 10 && r")
        val adap2 = layerSpec(
            condition = adap2Cond,
            enter = { flags += "adap2" }
        )

        EMA.exhibit(emptyList<Any>(), mapOf("a" to x))
        EMA.exhibit(adap1, mapOf("r" to adap1Cond))
        EMA.exhibit(adap2, mapOf("h" to adap2Cond))

        EMA.deploy(adap1)
        EMA.deploy(adap2)
        x.mutableValue = 9

        assertEquals(listOf("adap1", "adap2"), flags)
    }
}
