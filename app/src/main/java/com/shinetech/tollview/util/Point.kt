package com.shinetech.tollview.util

import java.lang.Math.pow
import kotlin.math.sqrt

class Point(val lat: Double, val long: Double) {
    fun distanceToOtherPoint(other: Point): Double {
        return sqrt(pow(this.lat - other.lat, 2.0)+pow(this.long - other.long, 2.0))
    }
}

fun main() {
    val p1: Point = Point(33.4256256, -96.4569845267)
    val p2: Point = Point(36.2059862, -95.2348958234)
    val p1p2Diff = p1.distanceToOtherPoint(p2)
}
