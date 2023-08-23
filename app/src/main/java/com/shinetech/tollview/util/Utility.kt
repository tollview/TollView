package com.shinetech.tollview.util

import android.content.Context
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.shinetech.tollview.models.Gate
import com.shinetech.tollview.models.Toll
import java.sql.Timestamp


class Utility(private val applicationContext: Context) {
    private var gatesList: ArrayList<Gate> = ArrayList<Gate>()

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val gatesReference: DatabaseReference = database.reference.child("gates")
    private val usersReference: DatabaseReference = database.reference.child("users")

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

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

    private fun fetchGatesFromDatabase(callback: (ArrayList<Gate>) -> Unit) {
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

            val otherLat = other.latitude
            val otherLong = other.longitude

            // Compute distance
            val currentDist = currentGPS.distanceToOtherPoint(Point(otherLat, otherLong))

            if (currentDist < leastDist) {
                leastDist = currentDist
                leastDistGate = other
            }

        }

        return leastDistGate
    }

    fun getTollsForUser(callback: (ArrayList<Toll>) -> Unit) {
        val tollsList: ArrayList<Toll> = ArrayList<Toll>()

        println(auth.currentUser!!.uid)

        val tollsReference = usersReference.child(auth.currentUser!!.uid).child("tolls")

        tollsReference.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                tollsList.clear()

                for (tollSnapshot in dataSnapshot.children) {

                    val gateId = tollSnapshot.child("gateId").getValue(String::class.java) ?: ""
                    val timestampMap = tollSnapshot.child("timestamp").getValue() as? Map<String, Any?>

                    val timeLong = timestampMap?.get("time") as? Long
                    val nanos = (timestampMap?.get("nanos") as? Long)?.toInt()

                    val timestamp = if (timeLong != null) {
                        Timestamp(timeLong).apply {
                            this.nanos = nanos ?: 0
                        }
                    } else {
                        null
                    }

                    val toll = Toll(gateId, timestamp)

                    toll?.let {
                        tollsList.add(toll)
                    }
                }

                callback(tollsList)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

}
