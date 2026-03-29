package org.emakotlin

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LayerActivationTest {
    @BeforeTest
    fun setUp() {
        EMA.init()
    }

    @Test
    fun create() {
        EMA.deploy(layerSpec(condition = "a > 10"))
        assertEquals(0, EMA.getActiveLayers().size)
    }

    @Test
    fun create2() {
        val obj = dynamicObject {
            set("x", Signal(9))
            set("y", 56)
        }

        EMA.deploy(layerSpec(condition = "a > 10"))
        EMA.exhibit(obj, mapOf("a" to obj.get<Signal>("x")))
        assertEquals(0, EMA.getActiveLayers().size)
    }

    @Test
    fun activate0() {
        val obj = dynamicObject {
            set("x", Signal(9))
            set("y", 56)
        }

        EMA.deploy(layerSpec(condition = "a > 10"))
        EMA.exhibit(obj, mapOf("a" to obj.get<Signal>("x")))
        obj.get<Signal>("x").mutableValue = 15

        assertEquals(1, EMA.getActiveLayers().size)
    }

    @Test
    fun activate1() {
        val obj = dynamicObject {
            set("x", Signal(9))
            set("y", Signal(0))
        }

        EMA.deploy(layerSpec(condition = "a > 10 && b > 10"))
        EMA.exhibit(obj, mapOf("a" to obj.get<Signal>("x"), "b" to obj.get<Signal>("y")))
        obj.get<Signal>("x").mutableValue = 15
        obj.get<Signal>("y").mutableValue = 20

        assertEquals(1, EMA.getActiveLayers().size)
    }

    @Test
    fun activate2() {
        val obj1 = dynamicObject {
            set("x", Signal(9))
            set("y", 62)
        }
        val obj2 = dynamicObject {
            set("x", Signal(5))
            set("y", 49)
        }

        EMA.deploy(layerSpec(condition = "a > 10 && b > 10"))
        EMA.exhibit(obj1, mapOf("a" to obj1.get<Signal>("x")))
        EMA.exhibit(obj2, mapOf("b" to obj2.get<Signal>("x")))
        obj1.get<Signal>("x").mutableValue = 15
        assertEquals(0, EMA.getActiveLayers().size)
        obj2.get<Signal>("x").mutableValue = 34
        assertEquals(1, EMA.getActiveLayers().size)
    }

    @Test
    fun activate3() {
        val obj1 = dynamicObject {
            set("x", Signal(9))
            set("y", 62)
        }
        val obj2 = dynamicObject {
            set("x", Signal(5))
            set("y", 49)
        }

        EMA.exhibit(obj1, mapOf("a" to obj1.get<Signal>("x")))
        EMA.exhibit(obj2, mapOf("b" to obj2.get<Signal>("x")))
        EMA.deploy(layerSpec(condition = "a > b"))

        assertEquals(1, EMA.getActiveLayers().size)
        obj2.get<Signal>("x").mutableValue = 34
        assertEquals(0, EMA.getActiveLayers().size)
    }

    @Test
    fun activate4() {
        val obj1 = dynamicObject {
            set("x", Signal(1))
            set("y", 62)
        }
        val obj2 = dynamicObject {
            set("x", Signal(2))
            set("y", 49)
        }

        EMA.deploy(layerSpec(condition = "a > 50"))
        EMA.exhibit(obj1, mapOf("a" to obj1.get<Signal>("x")))
        EMA.exhibit(obj2, mapOf("a" to obj2.get<Signal>("x")))

        assertEquals(0, EMA.getActiveLayers().size)

        obj1.get<Signal>("x").mutableValue = 100
        assertEquals(1, EMA.getActiveLayers().size)
        obj2.get<Signal>("x").mutableValue = 150
        assertEquals(1, EMA.getActiveLayers().size)
    }

    @Test
    fun activate5() {
        val obj = dynamicObject {
            set("x", Signal(1))
            set("y", 62)
        }

        EMA.deploy(layerSpec(condition = "a > 50"))
        EMA.deploy(layerSpec(condition = "a > 100"))
        EMA.exhibit(obj, mapOf("a" to obj.get<Signal>("x")))

        assertEquals(0, EMA.getActiveLayers().size)
        obj.get<Signal>("x").mutableValue = 60
        assertEquals(1, EMA.getActiveLayers().size)
        obj.get<Signal>("x").mutableValue = 110
        assertEquals(2, EMA.getActiveLayers().size)
    }

    @Test
    fun activate6() {
        val obj = dynamicObject {
            set("x", Signal(1))
            set("y", 62)
        }

        EMA.deploy(layerSpec(condition = "a > 50"))
        EMA.deploy(layerSpec(condition = "a > 100"))
        EMA.exhibit(obj, mapOf("a" to obj.get<Signal>("x")))

        assertEquals(0, EMA.getActiveLayers().size)
        obj.get<Signal>("x").mutableValue = 60
        assertEquals(1, EMA.getActiveLayers().size)
        obj.get<Signal>("x").mutableValue = 110
        assertEquals(2, EMA.getActiveLayers().size)
    }
}
