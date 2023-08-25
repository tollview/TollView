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
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val gatesReference: DatabaseReference = database.reference.child("gates")
    private val usersReference: DatabaseReference = database.reference.child("users")
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var gatesList: ArrayList<Gate> = ArrayList()

    init {
        fetchGatesFromDatabase {
            gatesList = it
        }
    }


    private fun fetchGatesFromDatabase(callback: (ArrayList<Gate>) -> Unit) {
        val gatesList: ArrayList<Gate> = ArrayList()

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
            }

        })
    }

    fun getClosestGate(lat: Double, long: Double): Gate {
        val currentGPS = Point(lat, long)

        var leastDist = 999999999.0
        var leastDistGate = Gate()

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
        val tollsList: ArrayList<Toll> = ArrayList()

        val tollsReference = usersReference.child(auth.currentUser!!.uid).child("tolls")

        tollsReference.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                tollsList.clear()

                for (tollSnapshot in dataSnapshot.children) {

                    val gateId = tollSnapshot.child("gateId").getValue(String::class.java) ?: ""
                    val timestampMap = tollSnapshot.child("timestamp").value as? Map<String, Any?>

                    timestampMap.let {
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

                        toll.let {
                            tollsList.add(toll)
                        }
                    }
                }

                callback(tollsList)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    fun toast(s: String) {
        Toast.makeText(
            applicationContext,
            s,
            Toast.LENGTH_SHORT
        ).show()
    }
    fun debug(name: String, s: String) {
        println("$name: $s")
        toast("$name: $s")
    }
}