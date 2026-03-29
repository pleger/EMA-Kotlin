package org.emakotlin

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SignalCompHigherOrderTest {
    @Test
    fun create1() {
        val s1 = Signal(10, "a")
        val cond1 = SignalComp("a > 10", listOf(s1))
        val cond2 = SignalComp("r > 10", listOf(cond1), "r")

        assertNotNull(cond1)
        assertNotNull(cond2)
    }

    @Test
    fun evaluate1() {
        val s1 = Signal(15, "a")
        val cond1 = SignalComp("a > 10", listOf(s1), "r")
        val cond2 = SignalComp("r", listOf(cond1))

        cond1.evaluate()
        assertEquals(true, cond1.value)
        assertEquals(true, cond2.value)
    }

    @Test
    fun evaluate2() {
        val s1 = Signal(15, "a")
        val cond1 = SignalComp("a > 10", listOf(s1), "r")
        val cond2 = SignalComp("r", listOf(cond1))

        s1.mutableValue = 9
        assertEquals(false, cond1.value)
        assertEquals(false, cond2.value)
    }

    @Test
    fun evaluate3() {
        val s1 = Signal(15, "a")
        val cond1 = SignalComp("a > 10", listOf(s1), "r")
        val cond2 = SignalComp("!r", listOf(cond1))

        s1.mutableValue = 9
        assertEquals(false, cond1.value)
        assertEquals(true, cond2.value)
    }

    @Test
    fun evaluate4() {
        val s1 = Signal(15, "a")
        val cond1 = SignalComp("a + 10", listOf(s1), "r")
        val cond2 = SignalComp("r > 20", listOf(cond1))

        s1.mutableValue = 100
        assertEquals(110.0, cond1.value.asDouble())
        assertEquals(true, cond2.value)
    }

    @Test
    fun evaluateWithThreeLevels() {
        val s1 = Signal(15, "a")
        val cond1 = SignalComp("a + 10", listOf(s1), "r")
        val cond2 = SignalComp("r > 20", listOf(cond1), "h")
        val cond3 = SignalComp("h && a > 5", listOf(s1, cond2))

        s1.mutableValue = 100
        assertEquals(110.0, cond1.value.asDouble())
        assertEquals(true, cond2.value)
        assertEquals(true, cond3.value)
    }

    @Test
    fun evaluateWithMultipleCondition() {
        val s1 = Signal(15, "a")
        val cond1 = SignalComp("a + 10", listOf(s1), "r")
        val cond2 = SignalComp("r > 20", listOf(cond1), "h")
        val cond3 = SignalComp("h && r > 5", listOf(cond1, cond2))

        s1.mutableValue = 100
        assertEquals(110.0, cond1.value.asDouble())
        assertEquals(true, cond2.value)
        assertEquals(true, cond3.value)
    }

    @Test
    fun recursion() {
        val flags = mutableListOf<Any?>()
        val t = Signal(0, "t")
        val ht = SignalComp("t > 10", listOf(t), "ht")
        val hto = SignalComp("hto || ht", listOf(ht), "hto")

        t.mutableValue = 15

        flags += ht.value
        flags += hto.value

        t.mutableValue = 8
        flags += ht.value
        flags += hto.value

        assertEquals(listOf<Any?>(true, true, false, true), flags)
    }

    @Test
    fun recursion2() {
        val flags = mutableListOf<Any?>()
        val t = Signal(0, "t")
        val h = Signal(0, "h")
        val ht = SignalComp("t > 10", listOf(t), "ht")
        val hh = SignalComp("h > 50", listOf(h), "hh")
        val hto = SignalComp("hto || ht", listOf(ht), "hto")
        val hho = SignalComp("hho || (hh && hto)", listOf(hh, hto), "hho")

        t.mutableValue = 15
        flags += ht.value
        flags += hto.value
        flags += hho.value

        h.mutableValue = 65
        flags += ht.value
        flags += hto.value
        flags += hho.value

        t.mutableValue = 6
        h.mutableValue = 9
        flags += hto.value
        flags += hho.value

        assertEquals(listOf<Any?>(true, true, false, true, true, true, true, true), flags)
    }

    @Test
    fun recursion3() {
        val flags1 = mutableListOf<Any?>()
        val flags2 = mutableListOf<Any?>()

        val t = Signal(0, "t")
        val h = Signal(0, "h")

        val ht = SignalComp("t > 10", listOf(t), "ht")
        val hh = SignalComp("h > 50", listOf(h), "hh")

        val state1 = SignalComp("(state1 || ht) && !start && !final", listOf(ht), "state1")
        val state2 = SignalComp("(start && hh)", listOf(hh), "state2")

        val start = SignalComp("(start || state1) && !final", listOf(state1), "start")
        val final = SignalComp("(final || state2)", listOf(state2), "final")

        state1.addSignal(start)
        state1.addSignal(final)
        state2.addSignal(start)
        start.addSignal(final)

        t.mutableValue = 15
        flags1 += state1.value
        flags1 += state2.value
        flags1 += start.value
        flags1 += final.value

        h.mutableValue = 65
        flags2 += state1.value
        flags2 += state2.value
        flags2 += start.value
        flags2 += final.value

        assertEquals(listOf<Any?>(false, false, true, false), flags1)
        assertEquals(listOf<Any?>(false, false, false, true), flags2)
    }

    @Test
    fun recursion4() {
        val flags1 = mutableListOf<Any?>()
        val flags2 = mutableListOf<Any?>()
        val flags3 = mutableListOf<Any?>()

        val t = Signal(0, "t")
        val h = Signal(0, "h")

        val ht = SignalComp("t > 10", listOf(t), "ht")
        val hh = SignalComp("h > 50", listOf(h), "hh")

        val state1 = SignalComp("(state1 || ht) && !start && !final", listOf(ht), "state1")
        val state2 = SignalComp("(start && hh)", listOf(hh), "state2")

        val start = SignalComp("(start || state1) && !final", listOf(state1), "start")
        val final = SignalComp("(final || state2)", listOf(state2), "final")

        state1.addSignal(start)
        state1.addSignal(final)
        state2.addSignal(start)
        start.addSignal(final)

        t.mutableValue = 15
        flags1 += state1.value
        flags1 += state2.value
        flags1 += start.value
        flags1 += final.value

        h.mutableValue = 65
        flags2 += state1.value
        flags2 += state2.value
        flags2 += start.value
        flags2 += final.value

        t.mutableValue = 56
        h.mutableValue = 90
        flags3 += state1.value
        flags3 += state2.value
        flags3 += start.value
        flags3 += final.value

        assertEquals(listOf<Any?>(false, false, true, false), flags1)
        assertEquals(listOf<Any?>(false, false, false, true), flags2)
        assertEquals(listOf<Any?>(false, false, false, true), flags3)
    }

    @Test
    fun recursion5() {
        val t = Signal(0, "t")
        val h = Signal(0, "h")

        val cond1 = SignalComp("t > 10", listOf(t), "cond1")
        val cond2 = SignalComp("h > 50", listOf(h), "cond2")

        val restart = SignalComp("(cond1 && final)", id = "restart")
        val state1 = SignalComp("(state1 || cond1) && !start", id = "state1")
        val state2 = SignalComp("(state2 || cond2) && start", id = "state2")

        val start = SignalComp("(start || state1) && !restart", id = "start")
        val final = SignalComp("(state2)", id = "final")

        // Mirroring EMA-js test: this one only validates setup doesn't crash.
        assertEquals(true, cond1 != null && cond2 != null && restart != null && state1 != null && state2 != null && start != null && final != null)
    }
}
