package com.shinetech.tollview.util

import android.content.Context
import android.widget.Toast

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
}