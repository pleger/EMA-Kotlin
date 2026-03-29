package org.emakotlin

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PartialMethodsManyObjectsTest {
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
                flags += "original_1"
                null
            }
        }

        val adap = layerSpec(condition = "a > 1")

        EMA.exhibit(obj, mapOf("a" to obj.get<Signal>("x")))
        EMA.addPartialMethod(adap, listOf(obj), "m") {
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
        val obj1 = dynamicObject {
            set("x", Signal(9))
            defineMethod("m") {
                flags += "original_1"
                null
            }
        }

        val obj2 = dynamicObject {
            defineMethod("m") {
                flags += "original_2"
                null
            }
        }

        val adap = layerSpec(condition = SignalComp("a > 1"))

        EMA.exhibit(obj1, mapOf("a" to obj1.get<Signal>("x")))
        EMA.addPartialMethod(adap, listOf(obj1, obj2), "m") {
            flags += "variation"
            null
        }
        obj1.call("m")

        EMA.deploy(adap)

        assertEquals(listOf("original_1"), flags)
    }

    @Test
    fun create3() {
        val flags = mutableListOf<String>()
        val obj1 = dynamicObject {
            set("name", "1")
            set("x", Signal(9))
            defineMethod("m") {
                flags += "original_${get<String>("name")}"
                null
            }
        }

        val obj2 = dynamicObject {
            set("name", "2")
            defineMethod("m") {
                flags += "original_${get<String>("name")}"
                null
            }
        }

        val adap = layerSpec(condition = SignalComp("a > 1"))

        EMA.exhibit(obj1, mapOf("a" to obj1.get<Signal>("x")))
        EMA.addPartialMethod(adap, listOf(obj1, obj2), "m") {
            flags += "variation"
            null
        }

        obj1.call("m")
        obj2.call("m")

        EMA.deploy(adap)

        assertEquals(listOf("original_1", "original_2"), flags)
    }

    @Test
    fun variation1() {
        val flags = mutableListOf<String>()
        val obj1 = dynamicObject {
            set("name", "1")
            set("x", Signal(-1))
            defineMethod("m") {
                flags += "original_${get<String>("name")}"
                null
            }
        }

        val obj2 = dynamicObject {
            set("name", "2")
            defineMethod("m") {
                flags += "original_${get<String>("name")}"
                null
            }
        }

        val adap = layerSpec(condition = SignalComp("a > 1"))

        EMA.exhibit(obj1, mapOf("a" to obj1.get<Signal>("x")))
        EMA.addPartialMethod(adap, listOf(obj1, obj2), "m") {
            flags += "variation_${get<String>("name")}"
            null
        }

        EMA.deploy(adap)
        obj1.get<Signal>("x").mutableValue = 10
        obj1.call("m")

        assertEquals(listOf("variation_1"), flags)
    }

    @Test
    fun variation2() {
        val flags = mutableListOf<String>()
        val obj1 = dynamicObject {
            set("name", "1")
            set("x", Signal(-1))
            defineMethod("m") {
                flags += "original_${get<String>("name")}"
                null
            }
        }

        val obj2 = dynamicObject {
            set("name", "2")
            defineMethod("m") {
                flags += "original_${get<String>("name")}"
                null
            }
        }

        val adap = layerSpec(condition = SignalComp("a > 1"))

        EMA.exhibit(obj1, mapOf("a" to obj1.get<Signal>("x")))
        EMA.addPartialMethod(adap, listOf(obj1, obj2), "m") {
            flags += "variation_${get<String>("name")}"
            null
        }

        EMA.deploy(adap)
        obj1.get<Signal>("x").mutableValue = 10
        obj1.call("m")
        obj2.call("m")

        assertEquals(listOf("variation_1", "variation_2"), flags)
    }

    @Test
    fun variation3() {
        val flags = mutableListOf<String>()
        val obj1 = dynamicObject {
            set("name", "1")
            set("x", Signal(-1))
            defineMethod("m") {
                flags += "original_${get<String>("name")}"
                null
            }
        }

        val obj2 = dynamicObject {
            set("name", "2")
            defineMethod("m") {
                flags += "original_${get<String>("name")}"
                null
            }
        }

        val adap = layerSpec(condition = SignalComp("a > 1"))

        EMA.exhibit(obj1, mapOf("a" to obj1.get<Signal>("x")))
        EMA.addPartialMethod(adap, listOf(obj2, obj1), "m") {
            flags += "variation_${get<String>("name")}"
            null
        }

        EMA.deploy(adap)
        obj1.get<Signal>("x").mutableValue = 10
        obj1.call("m")
        obj2.call("m")

        assertEquals(listOf("variation_1", "variation_2"), flags)
    }
}
