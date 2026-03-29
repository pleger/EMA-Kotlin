package org.emakotlin

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PartialMethodLayerTest {
    @BeforeTest
    fun setUp() {
        EMA.init()
    }

    @Test
    fun create() {
        val flags = mutableListOf<String>()
        val obj = dynamicObject {
            set("x", Signal(9))
            defineMethod("m") {
                flags += "original"
                null
            }
        }

        val adap = layerSpec(condition = "a > 1")

        EMA.exhibit(obj, mapOf("a" to obj.get<Signal>("x")))
        EMA.addPartialMethod(adap, obj, "m") {
            flags += "variation"
            null
        }
        EMA.deploy(adap)
        obj.call("m")

        assertEquals(listOf("variation"), flags)
    }

    @Test
    fun create2() {
        val flags = mutableListOf<String>()
        val obj = dynamicObject {
            set("x", Signal(9))
            defineMethod("m") {
                flags += "original"
                null
            }
        }

        val adap = layerSpec(condition = SignalComp("a > 1"))

        EMA.exhibit(obj, mapOf("a" to obj.get<Signal>("x")))
        EMA.addPartialMethod(adap, obj, "m") {
            flags += "variation"
            null
        }
        obj.call("m")
        EMA.deploy(adap)

        assertEquals(listOf("original"), flags)
    }

    @Test
    fun variation1() {
        val flags = mutableListOf<String>()
        val obj = dynamicObject {
            set("x", Signal(-1))
            defineMethod("m") {
                flags += "original"
                null
            }
        }

        val adap = layerSpec(condition = SignalComp("a > 1"))

        EMA.exhibit(obj, mapOf("a" to obj.get<Signal>("x")))
        EMA.addPartialMethod(adap, obj, "m") {
            flags += "variation"
            null
        }
        EMA.deploy(adap)
        obj.get<Signal>("x").mutableValue = 10
        obj.call("m")

        assertEquals(listOf("variation"), flags)
    }

    @Test
    fun variation2ChangingDeploymentPlace() {
        val flags = mutableListOf<String>()
        val obj = dynamicObject {
            set("x", Signal(-1))
            defineMethod("m") {
                flags += "original"
                null
            }
        }

        val adap = layerSpec(condition = SignalComp("a > 1"))

        obj.get<Signal>("x").mutableValue = 10
        EMA.exhibit(obj, mapOf("a" to obj.get<Signal>("x")))
        EMA.addPartialMethod(adap, obj, "m") {
            flags += "variation"
            null
        }
        EMA.deploy(adap)
        obj.call("m")

        assertEquals(listOf("variation"), flags)
    }

    @Test
    fun variation3ChangingDeploymentPlace() {
        val flags = mutableListOf<String>()
        val obj = dynamicObject {
            set("x", Signal(-1))
            defineMethod("m") {
                flags += "original"
                null
            }
        }

        val adap = layerSpec(condition = SignalComp("a > 1"))

        obj.get<Signal>("x").mutableValue = 10
        EMA.exhibit(obj, mapOf("a" to obj.get<Signal>("x")))
        EMA.addPartialMethod(adap, obj, "m") {
            flags += "variation"
            null
        }
        obj.call("m")
        EMA.deploy(adap)

        assertEquals(listOf("original"), flags)
    }

    @Test
    fun variation4AddingRemovingLayer() {
        val flags = mutableListOf<String>()
        val obj = dynamicObject {
            set("x", Signal(-1))
            defineMethod("m") {
                flags += "original"
                null
            }
        }

        val adap = layerSpec(condition = SignalComp("a > 1"))

        obj.get<Signal>("x").mutableValue = 10
        EMA.exhibit(obj, mapOf("a" to obj.get<Signal>("x")))
        EMA.addPartialMethod(adap, obj, "m") {
            flags += "variation"
            null
        }
        EMA.deploy(adap)

        obj.call("m")
        obj.get<Signal>("x").mutableValue = 0
        obj.call("m")

        assertEquals(listOf("variation", "original"), flags)
    }

    @Test
    fun callingProceed() {
        val flags = mutableListOf<String>()
        val obj = dynamicObject {
            set("x", Signal(-1))
            defineMethod("m") {
                flags += "original"
                null
            }
        }

        val adap = layerSpec(condition = SignalComp("a > 1"))

        obj.get<Signal>("x").mutableValue = 10
        EMA.exhibit(obj, mapOf("a" to obj.get<Signal>("x")))
        EMA.addPartialMethod(adap, obj, "m") {
            flags += "variation"
            Layer.proceed()
            null
        }
        EMA.deploy(adap)
        obj.call("m")

        assertEquals(listOf("variation", "original"), flags)
    }

    @Test
    fun layerMethodWithReturn() {
        val flags = mutableListOf<String>()
        val obj = dynamicObject {
            set("x", Signal(-1))
            defineMethod("m") {
                "original"
            }
        }

        val adap = layerSpec(condition = SignalComp("a > 1"))

        obj.get<Signal>("x").mutableValue = 10
        EMA.exhibit(obj, mapOf("a" to obj.get<Signal>("x")))
        EMA.addPartialMethod(adap, obj, "m") {
            "variation"
        }
        flags += obj.call("m") as String
        EMA.deploy(adap)
        flags += obj.call("m") as String

        assertEquals(listOf("original", "variation"), flags)
    }

    @Test
    fun layerMethodWithReturnProceed() {
        val flags = mutableListOf<String>()
        val obj = dynamicObject {
            set("x", Signal(-1))
            defineMethod("m") {
                "original"
            }
        }

        val adap = layerSpec(condition = SignalComp("a > 1"))

        obj.get<Signal>("x").mutableValue = 10
        EMA.exhibit(obj, mapOf("a" to obj.get<Signal>("x")))
        EMA.addPartialMethod(adap, obj, "m") {
            "variation-${Layer.proceed()}"
        }
        flags += obj.call("m") as String
        EMA.deploy(adap)
        flags += obj.call("m") as String

        assertEquals(listOf("original", "variation-original"), flags)
    }

    @Test
    fun layerMethodWithReturnProceedUninstall() {
        val flags = mutableListOf<String>()
        val obj = dynamicObject {
            set("x", Signal(-1))
            defineMethod("m") {
                "original"
            }
        }

        val adap = layerSpec(condition = SignalComp("a > 1"))

        obj.get<Signal>("x").mutableValue = 10
        EMA.exhibit(obj, mapOf("a" to obj.get<Signal>("x")))
        EMA.addPartialMethod(adap, obj, "m") {
            "variation-${Layer.proceed()}"
        }
        flags += obj.call("m") as String
        EMA.deploy(adap)
        flags += obj.call("m") as String
        obj.get<Signal>("x").mutableValue = 0
        flags += obj.call("m") as String

        assertEquals(listOf("original", "variation-original", "original"), flags)
    }

    @Test
    fun layerMethodWithArgs1() {
        val flags = mutableListOf<String>()
        val obj = dynamicObject {
            set("x", Signal(-1))
            defineMethod("m") { args ->
                flags += "original_${args[0]}"
                null
            }
        }

        val layer = layerSpec(condition = SignalComp("a > 1"))

        obj.get<Signal>("x").mutableValue = 10
        EMA.exhibit(obj, mapOf("a" to obj.get<Signal>("x")))
        EMA.addPartialMethod(layer, obj, "m") { args ->
            flags += "variation_${args[0]}"
            null
        }

        obj.call("m", 1)
        EMA.deploy(layer)
        obj.call("m", 2)
        obj.get<Signal>("x").mutableValue = 0
        obj.call("m", 3)

        assertEquals(listOf("original_1", "variation_2", "original_3"), flags)
    }

    @Test
    fun layerMethodWithArgsProceed() {
        val flags = mutableListOf<String>()
        val obj = dynamicObject {
            set("x", Signal(-1))
            defineMethod("m") { args ->
                flags += "original_${args[0]}"
                null
            }
        }

        val layer = layerSpec(condition = SignalComp("a > 1"))

        obj.get<Signal>("x").mutableValue = 10
        EMA.exhibit(obj, mapOf("a" to obj.get<Signal>("x")))
        EMA.addPartialMethod(layer, obj, "m") { args ->
            val arg = args[0] as Int
            flags += "variation_$arg"
            Layer.proceed(arg + 1)
            null
        }

        obj.call("m", 1)
        EMA.deploy(layer)
        obj.call("m", 2)
        obj.get<Signal>("x").mutableValue = 0
        obj.call("m", 3)

        assertEquals(listOf("original_1", "variation_2", "original_3", "original_3"), flags)
    }
}
