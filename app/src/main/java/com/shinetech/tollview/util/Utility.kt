package com.shinetech.tollview.util

import android.content.Context
import android.widget.Toast
import com.shinetech.tollview.models.Gate

class Utility(
    val applicationContext: Context
) {

    fun toastln(s: String) {
        Toast.makeText(
            applicationContext,
            s,
            Toast.LENGTH_SHORT
        ).show()
    }

    fun woof(name: String, s: String) {
        println("$name: $s")
        toastln("$name: $s")
    }

    fun getClosestGate(lat: Double, long: Double): Gate {
        val CurrentGPS:Point = Point(lat, long)
        return Gate()
    }
}