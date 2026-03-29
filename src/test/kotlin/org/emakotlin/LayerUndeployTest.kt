package org.emakotlin

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LayerUndeployTest {
    @BeforeTest
    fun setUp() {
        EMA.init()
    }

    @Test
    fun layerUndeploy1() {
        val flags = mutableListOf<String>()
        val obj = dynamicObject {
            set("x", Signal(-1))
            defineMethod("m") { "original" }
        }

        val adap = layerSpec(condition = SignalComp("a > 1"))

        obj.get<Signal>("x").mutableValue = 10
        EMA.exhibit(obj, mapOf("a" to obj.get<Signal>("x")))
        EMA.addPartialMethod(adap, obj, "m") { "variation" }

        flags += obj.call("m") as String
        EMA.deploy(adap)
        flags += obj.call("m") as String
        EMA.undeploy(adap)
        flags += obj.call("m") as String

        assertEquals(listOf("original", "variation", "original"), flags)
    }

    @Test
    fun layerUndeployDeploy() {
        val flags = mutableListOf<String>()
        val obj = dynamicObject {
            set("x", Signal(-1))
            defineMethod("m") { "original" }
        }

        val adap = layerSpec(condition = "a > 1")

        obj.get<Signal>("x").mutableValue = 10
        EMA.exhibit(obj, mapOf("a" to obj.get<Signal>("x")))
        EMA.addPartialMethod(adap, obj, "m") { "variation" }

        flags += obj.call("m") as String
        EMA.deploy(adap)
        flags += obj.call("m") as String
        EMA.undeploy(adap)
        flags += obj.call("m") as String
        EMA.deploy(adap)
        flags += obj.call("m") as String

        assertEquals(listOf("original", "variation", "original", "variation"), flags)
    }
}
