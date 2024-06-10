package com.shinetech.tollview.util

import org.junit.Assert.*
import org.junit.Test

class PointTest {

    @Test
    fun testDistanceToSamePoint() {
        val point = Point(0.0, 0.0)
        val distance = point.distanceToOtherPoint(point)
        assertEquals(0.0, distance, 0.0)
    }

    @Test
    fun testDistanceToAnotherPoint() {
        val point1 = Point(0.0, 0.0)
        val point2 = Point(3.0, 4.0)
        val distance = point1.distanceToOtherPoint(point2)
        assertEquals(5.0, distance, 0.0)
    }

    @Test
    fun testDistanceToAnotherPointWithNegativeCoordinates() {
        val point1 = Point(-1.0, -1.0)
        val point2 = Point(-4.0, -5.0)
        val distance = point1.distanceToOtherPoint(point2)
        assertEquals(5.0, distance, 0.0)
    }

    @Test
    fun testDistanceToAnotherPointWithDecimalCoordinates() {
        val point1 = Point(1.5, 2.5)
        val point2 = Point(4.0, 6.0)
        val distance = point1.distanceToOtherPoint(point2)
        assertEquals(4.6097722286464435, distance, 0.5)
    }
}
