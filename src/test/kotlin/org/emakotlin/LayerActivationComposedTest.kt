package org.emakotlin

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LayerActivationComposedTest {
    @BeforeTest
    fun setUp() {
        EMA.init()
    }

    @Test
    fun twoActivations() {
        val obj = dynamicObject {
            set("x", Signal(9))
            set("y", 56)
        }

        val adap1Condition = SignalComp("a > 1")
        val adap1 = layerSpec(condition = adap1Condition)

        val adap2Condition = SignalComp("h")
        val adap2 = layerSpec(condition = adap2Condition)

        EMA.deploy(adap1)
        EMA.deploy(adap2)
        EMA.exhibit(obj, mapOf("a" to obj.get<Signal>("x")))
        EMA.exhibit(adap1, mapOf("pp" to adap1Condition))
    }

    @Test
    fun twoActivations2() {
        val flags = mutableListOf<String>()
        val obj = dynamicObject {
            set("x", Signal(0))
            set("y", 56)
        }

        val adap1Condition = SignalComp("a > 1")
        val adap1 = layerSpec(
            condition = adap1Condition,
            enter = { flags += "enter-adap1" }
        )

        val adap2Condition = SignalComp("h")
        val adap2 = layerSpec(
            condition = adap2Condition,
            enter = { flags += "enter-adap2" }
        )

        EMA.deploy(adap1)
        EMA.deploy(adap2)
        EMA.exhibit(obj, mapOf("a" to obj.get<Signal>("x")))
        EMA.exhibit(adap1, mapOf("h" to adap1Condition))
        obj.get<Signal>("x").mutableValue = 10

        assertEquals(listOf("enter-adap1", "enter-adap2"), flags)
    }

    @Test
    fun twoActivations3() {
        val flags = mutableListOf<String>()
        val obj = dynamicObject {
            set("x", Signal(0))
            set("y", Signal(5))
        }

        val adap1Condition = SignalComp("a > 1")
        val adap1 = layerSpec(
            condition = adap1Condition,
            enter = { flags += "enter-adap1" }
        )

        val adap2Condition = SignalComp("b > 5")
        val adap2 = layerSpec(
            condition = adap2Condition,
            enter = { flags += "enter-adap2" }
        )

        val adap3Condition = SignalComp("h && r")
        val adap3 = layerSpec(
            condition = adap3Condition,
            enter = { flags += "enter-adap3" }
        )

        EMA.deploy(adap1)
        EMA.deploy(adap2)
        EMA.deploy(adap3)

        EMA.exhibit(obj, mapOf("a" to obj.get<Signal>("x"), "b" to obj.get<Signal>("y")))
        EMA.exhibit(adap1, mapOf("h" to adap1Condition))
        EMA.exhibit(adap2, mapOf("r" to adap2Condition))
        obj.get<Signal>("x").mutableValue = 10
        obj.get<Signal>("y").mutableValue = 100

        assertEquals(listOf("enter-adap1", "enter-adap2", "enter-adap3"), flags)
    }

    @Test
    fun twoActivations4() {
        val flags = mutableListOf<String>()
        val obj = dynamicObject {
            set("x", Signal(0))
            set("y", Signal(5))
        }

        val adap1Condition = SignalComp("a > 1")
        val adap1 = layerSpec(
            condition = adap1Condition,
            enter = { flags += "enter-adap1" },
            exit = { flags += "exit-adap1" }
        )

        val adap2Condition = SignalComp("b > 5")
        val adap2 = layerSpec(
            condition = adap2Condition,
            enter = { flags += "enter-adap2" },
            exit = { flags += "exit-adap2" }
        )

        val adap3Condition = SignalComp("h && r")
        val adap3 = layerSpec(
            condition = adap3Condition,
            enter = { flags += "enter-adap3" },
            exit = { flags += "exit-adap3" }
        )

        EMA.deploy(adap1)
        EMA.deploy(adap2)
        EMA.deploy(adap3)

        EMA.exhibit(obj, mapOf("a" to obj.get<Signal>("x"), "b" to obj.get<Signal>("y")))
        EMA.exhibit(adap1, mapOf("h" to adap1Condition))
        EMA.exhibit(adap2, mapOf("r" to adap2Condition))
        obj.get<Signal>("x").mutableValue = 10
        obj.get<Signal>("y").mutableValue = 100
        obj.get<Signal>("x").mutableValue = -1

        assertEquals(listOf("enter-adap1", "enter-adap2", "enter-adap3", "exit-adap1", "exit-adap3"), flags)
    }

    @Test
    fun twoActivations5() {
        val flags = mutableListOf<String>()
        val obj = dynamicObject {
            set("x", Signal(0))
            set("y", Signal(5))
        }

        val adap1Condition = SignalComp("a > 1")
        val adap1 = layerSpec(
            condition = adap1Condition,
            enter = { flags += "enter-adap1" },
            exit = { flags += "exit-adap1" }
        )

        val adap2Condition = SignalComp("b > 5")
        val adap2 = layerSpec(
            condition = adap2Condition,
            enter = { flags += "enter-adap2" },
            exit = { flags += "exit-adap2" }
        )

        val adap3Condition = SignalComp("h && r")
        val adap3 = layerSpec(
            condition = adap3Condition,
            enter = { flags += "enter-adap3" },
            exit = { flags += "exit-adap3" }
        )

        EMA.exhibit(obj, mapOf("a" to obj.get<Signal>("x"), "b" to obj.get<Signal>("y")))
        EMA.exhibit(adap1, mapOf("h" to adap1Condition))
        EMA.exhibit(adap2, mapOf("r" to adap2Condition))
        EMA.deploy(adap1)
        EMA.deploy(adap2)
        EMA.deploy(adap3)

        obj.get<Signal>("x").mutableValue = 10
        obj.get<Signal>("y").mutableValue = 100
        obj.get<Signal>("x").mutableValue = -1

        assertEquals(listOf("enter-adap1", "enter-adap2", "enter-adap3", "exit-adap1", "exit-adap3"), flags)
    }

    @Test
    fun twoActivationsConflicts() {
        val flags = mutableListOf<String>()
        val obj = dynamicObject {
            set("x", Signal(0))
            set("y", Signal(0))
            set("z", Signal(0))
        }

        val adap1Condition = SignalComp("a > 1 && b > 10 && !adap2")
        val adap1 = layerSpec(
            condition = adap1Condition,
            enter = { flags += "enter-adap1" },
            exit = { flags += "exit-adap1" }
        )

        val adap2Condition = SignalComp("c > 5")
        val adap2 = layerSpec(
            condition = adap2Condition,
            enter = { flags += "enter-adap2" },
            exit = { flags += "exit-adap2" }
        )

        EMA.exhibit(obj, mapOf("a" to obj.get<Signal>("x"), "b" to obj.get<Signal>("y"), "c" to obj.get<Signal>("z")))
        EMA.exhibit(adap2, mapOf("adap2" to adap2Condition))
        EMA.deploy(adap1)
        EMA.deploy(adap2)

        obj.get<Signal>("x").mutableValue = 10
        obj.get<Signal>("y").mutableValue = 100
        obj.get<Signal>("z").mutableValue = 10

        assertEquals(listOf("enter-adap1", "exit-adap1", "enter-adap2"), flags)
    }
}
