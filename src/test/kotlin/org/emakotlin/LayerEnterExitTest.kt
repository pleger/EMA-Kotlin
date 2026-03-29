package org.emakotlin

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LayerEnterExitTest {
    @BeforeTest
    fun setUp() {
        EMA.init()
    }

    @Test
    fun enterExit1() {
        val flags = mutableListOf<String>()

        val adap = layerSpec(
            enter = { flags += "enter" },
            exit = { flags += "exit" },
            condition = "a > 10"
        )

        EMA.deploy(adap)

        val obj = dynamicObject {
            set("x", Signal(2))
            set("y", 20)
        }

        EMA.exhibit(obj, mapOf("a" to obj.get<Signal>("x")))

        assertEquals(emptyList(), flags)
    }

    @Test
    fun enterExit2() {
        val flags = mutableListOf<String>()

        val adap = layerSpec(
            enter = { flags += "enter" },
            exit = { flags += "exit" },
            condition = "a > 10"
        )

        EMA.deploy(adap)
        val obj = dynamicObject {
            set("x", Signal(2))
            set("y", 20)
        }

        EMA.exhibit(obj, mapOf("a" to obj.get<Signal>("x")))
        obj.get<Signal>("x").mutableValue = 20

        assertEquals(listOf("enter"), flags)
    }

    @Test
    fun enterExit3() {
        val flags = mutableListOf<String>()

        val adap = layerSpec(
            enter = { flags += "enter" },
            exit = { flags += "exit" },
            condition = "a > 10"
        )

        EMA.deploy(adap)
        val obj = dynamicObject {
            set("x", Signal(2))
            set("y", 20)
        }

        EMA.exhibit(obj, mapOf("a" to obj.get<Signal>("x")))
        obj.get<Signal>("x").mutableValue = 20
        obj.get<Signal>("x").mutableValue = 5

        assertEquals(listOf("enter", "exit"), flags)
    }

    @Test
    fun enterExit4() {
        val flags = mutableListOf<String>()

        val adap = layerSpec(
            enter = { flags += "enter" },
            exit = { flags += "exit" },
            condition = "a > 10"
        )

        EMA.deploy(adap)
        val obj = dynamicObject {
            set("x", Signal(2))
            set("y", 20)
        }

        EMA.exhibit(obj, mapOf("a" to obj.get<Signal>("x")))
        obj.get<Signal>("x").mutableValue = 20
        obj.get<Signal>("x").mutableValue = 1000

        assertEquals(listOf("enter"), flags)
    }

    @Test
    fun enterExit5() {
        val flags = mutableListOf<String>()

        val adap = layerSpec(
            enter = { flags += "enter" },
            exit = { flags += "exit" },
            condition = "a > 10"
        )

        EMA.deploy(adap)
        val obj = dynamicObject {
            set("x", Signal(2))
            set("y", 20)
        }

        EMA.exhibit(obj, mapOf("a" to obj.get<Signal>("x")))
        obj.get<Signal>("x").mutableValue = 20
        obj.get<Signal>("x").mutableValue = 1
        obj.get<Signal>("x").mutableValue = 50
        obj.get<Signal>("x").mutableValue = 150

        assertEquals(listOf("enter", "exit", "enter"), flags)
    }
}
