package org.emakotlin

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotSame
import kotlin.test.assertTrue

class SignalTest {
    @Test
    fun create() {
        val signal = Signal(10, "a")

        assertEquals("a", signal.id)
        assertEquals(10, signal.value)
    }

    @Test
    fun changingValue() {
        val signal = Signal(10, "a")

        signal.rawValue = 5
        assertEquals(5, signal.rawValue)
    }

    @Test
    fun twoSignals() {
        val signal1 = Signal(10, "a")
        val signal2 = Signal(100, "b")

        assertNotSame(signal1, signal2)
        assertEquals("a", signal1.id)
        assertEquals(10, signal1.rawValue)
        assertEquals("b", signal2.id)
        assertEquals(100, signal2.rawValue)
    }

    @Test
    fun twoSignalsTwoValues() {
        val signal1 = Signal(10, "a")
        val signal2 = Signal(100, "b")

        assertNotSame(signal1, signal2)
        assertEquals("a", signal1.id)
        assertEquals(10, signal1.rawValue)
        assertEquals("b", signal2.id)
        assertEquals(100, signal2.rawValue)

        signal1.rawValue = 5
        signal2.rawValue = 5

        assertTrue(signal1.rawValue == signal2.rawValue)
    }

    @Test
    fun signalNoId() {
        val signal = Signal(10)

        signal.id = "a"
        assertEquals("a", signal.id)
    }

    @Test
    fun signalNoId2() {
        val signal = Signal(10)

        signal.id = "a"
        assertNotEquals("_", signal.id)
    }
}
