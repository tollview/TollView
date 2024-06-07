package com.shinetech.tollview.models

import org.junit.Assert.*
import org.junit.Test

class GateTest {

    @Test
    fun testGateInstantiation() {
        val gate = Gate(
            id = "1",
            name = "Test Gate",
            sourceId = 123,
            type = "entry",
            chargeType = "fixed",
            cost = 2.50,
            costWithoutTag = 3.00,
            latitude = 40.7128,
            longitude = -74.0060,
            cardinality = 'N'
        )

        assertEquals("1", gate.id)
        assertEquals("Test Gate", gate.name)
        assertEquals(123, gate.sourceId)
        assertEquals("entry", gate.type)
        assertEquals("fixed", gate.chargeType)
        assertEquals(2.50, gate.cost, 0.01)
        assertEquals(3.00, gate.costWithoutTag, 0.01)
        assertEquals(40.7128, gate.latitude, 0.0001)
        assertEquals(-74.0060, gate.longitude, 0.0001)
        assertEquals('N', gate.cardinality)
    }

    @Test
    fun testGateEquality() {
        val gate1 = Gate(id = "1", name = "Gate A")
        val gate2 = Gate(id = "1", name = "Gate A")

        assertEquals(gate1, gate2)
    }
}
