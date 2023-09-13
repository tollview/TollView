package com.shinetech.tollview.util

import kotlin.math.pow
import kotlin.math.sqrt

class Point(private val lat: Double, private val long: Double) {
    fun distanceToOtherPoint(other: Point): Double {
        return sqrt((this.lat - other.lat).pow(2.0) + (this.long - other.long).pow(2.0))
    }
}