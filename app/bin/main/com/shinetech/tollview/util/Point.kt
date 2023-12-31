package com.shinetech.tollview.util

import java.lang.Math.pow
import kotlin.math.sqrt

class Point(val lat: Double, val long: Double) {
    fun distanceToOtherPoint(other: Point): Double {
        return sqrt(pow(this.lat - other.lat, 2.0)+pow(this.long - other.long, 2.0))
    }
}