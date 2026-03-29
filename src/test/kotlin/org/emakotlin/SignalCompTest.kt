package org.emakotlin

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SignalCompTest {
    @Test
    fun create() {
        val s1 = Signal(10, "a")
        val cond = SignalComp("a > 10", listOf(s1))

        assertNotNull(cond)
    }

    @Test
    fun evaluate() {
        val s1 = Signal(10, "a")
        val cond = SignalComp("a > 10", listOf(s1))

        assertEquals(false, cond.evaluate())
    }

    @Test
    fun changeValue() {
        val s1 = Signal(10, "a")
        val cond = SignalComp("a > 10", listOf(s1))

        assertEquals(false, cond.evaluate())
        s1.rawValue = 11
        assertEquals(true, cond.evaluate())
    }

    @Test
    fun undefinedVariables() {
        val cond = SignalComp("a > 10", listOf(Signal(10, "b")))

        assertEquals(false, cond.evaluate())
    }

    @Test
    fun undefinedVariables2() {
        val cond = SignalComp("true && a > 10", listOf(Signal(10, "b")))

        assertEquals(false, cond.evaluate())
    }

    @Test
    fun multipleCondition() {
        val s1 = Signal(0, "a")
        val cond1 = SignalComp("a > 10", listOf(s1))
        val cond2 = SignalComp("a > 100", listOf(s1))

        s1.rawValue = 19
        assertEquals(true, cond1.evaluate())
        assertEquals(false, cond2.evaluate())
    }

    @Test
    fun conditionMultiplesAdding() {
        val cond = SignalComp("a > 10 && b > 4 && c < 5 && d < 10")

        cond.addSignal(Signal(100, "a"))
        cond.addSignal(Signal(10, "b"))
        cond.addSignal(Signal(2, "c"))
        cond.addSignal(Signal(0, "d"))
    }

    @Test
    fun conditionMultiplesAdding2() {
        val cond = SignalComp("a > 10 && b > 4 && c < 5 && d < 10")

        cond.addSignal(Signal(100, "a"))
        cond.addSignal(Signal(10, "b"))
        cond.addSignal(Signal(2, "c"))
        cond.addSignal(Signal(0, "d"))
    }

    @Test
    fun conditionMultiplesAdding3() {
        val cond = SignalComp("a > 10 && b > 4 && c < 5 && d < 10")

        cond.addSignal(Signal(100, "a"))
        cond.addSignal(Signal(10, "b"))
        cond.addSignal(Signal(2, "c"))
        cond.addSignal(Signal(0, "d"))

        assertEquals(true, cond.evaluate())
    }

    @Test
    fun conditionDifferentSignals() {
        val s1 = Signal(0, "a")
        val s2 = Signal(11, "a")
        val s3 = Signal(5, "a")

        var activation = false
        val cond = SignalComp("a > 10")

        cond.on(SignalSubscriber { _, _ ->
            assertEquals(activation, cond.value)
        })

        activation = false
        cond.addSignal(s1)

        activation = true
        cond.addSignal(s2)

        activation = false
        cond.addSignal(s3)
    }

    @Test
    fun countingConditionSignal1() {
        var count = 0
        val s = Signal(0, "b")
        val cond = SignalComp("a > 10", listOf(s))

        cond.on(SignalSubscriber { _, _ ->
            assertEquals(false, cond.value)
            count += 1
        })

        s.mutableValue = 5
        s.mutableValue = 10
        s.mutableValue = 45

        assertEquals(1, count)
    }

    @Test
    fun countingConditionSignal2() {
        val s = Signal(0, "a")

        var count = 0
        val cond = SignalComp("a > 10", listOf(s))
        cond.on(SignalSubscriber { _, _ ->
            assertEquals(false, cond.value)
            count += 1
        })

        s.mutableValue = 5
        s.mutableValue = 1
        s.mutableValue = 4

        assertEquals(1, count)
    }

    @Test
    fun countingConditionSignal3() {
        var count = 0
        val s = Signal(0, "a")
        val cond = SignalComp("a > 10", listOf(s))
        cond.on(SignalSubscriber { _, _ ->
            count += 1
        })

        assertEquals(0, count)
    }

    @Test
    fun countingConditionSignal4() {
        var count = 0
        val s = Signal(0, "a")
        val cond = SignalComp("a > 10", listOf(s))
        cond.on(SignalSubscriber { _, _ ->
            count += 1
        })

        s.mutableValue = 12
        s.mutableValue = 8
        s.mutableValue = 15

        assertEquals(3, count)
    }
}
