package com.shinetech.tollview.models

import org.junit.Assert.*
import org.junit.Test
import java.sql.Timestamp

class TollTest {

    @Test
    fun testTollInstantiation() {
        val timestamp = Timestamp(System.currentTimeMillis())
        val toll = Toll(
            gateId = "3",
            timestamp = timestamp
        )

        assertEquals("3", toll.gateId)
        assertEquals(timestamp, toll.timestamp)
    }
}
