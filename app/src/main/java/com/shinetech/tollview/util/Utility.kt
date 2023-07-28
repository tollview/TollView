package com.shinetech.tollview.util

import android.content.Context
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.shinetech.tollview.models.Gate
import java.lang.Math.abs


class Utility(
    val applicationContext: Context
) {
    private var gatesList: ArrayList<Gate> = ArrayList<Gate>()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val gatesReference: DatabaseReference = database.reference.child("gates")

    init {
        fetchGatesFromDatabase { gates ->
            gates.forEach {
                gatesList.add(it)
            }
        }

    }

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

    fun fetchGatesFromDatabase(callback: (ArrayList<Gate>) -> Unit) {
        val gatesList: ArrayList<Gate> = ArrayList<Gate>()

        gatesReference.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                gatesList.clear()

                for (gateSnapshot in dataSnapshot.children) {
                    val gate = gateSnapshot.getValue(Gate::class.java)
                    gate?.let {
                        gatesList.add(it)
                    }
                }
                callback(gatesList)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    fun getClosestGate(lat: Double, long: Double): Gate {
        val currentGPS:Point = Point(lat, long)

        var leastDist: Double = 999999999.0
        var leastDistGate: Gate = Gate()

        gatesList.forEach { other ->

            val other_lat = other.latitude
            val other_long = other.longitude

            // Compute distance
            val currentDist = currentGPS.distanceToOtherPoint(Point(other_lat, other_long))

            if (currentDist < leastDist) {
                leastDist = currentDist
                leastDistGate = other
            }

        }

        return leastDistGate
    }
}
