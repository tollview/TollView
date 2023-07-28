package com.shinetech.tollview

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.renderscript.Sampler.Value
import android.widget.Button
import androidx.core.app.ActivityCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.shinetech.tollview.models.Gate
import com.shinetech.tollview.models.Toll
import com.shinetech.tollview.models.User
import com.shinetech.tollview.util.Utility
import java.sql.Timestamp
import kotlin.random.Random
import android.Manifest
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    lateinit var utility: Utility

    lateinit var btnSignOut: Button
    lateinit var btnDebugGetAllGates: Button
    lateinit var btnDebugGiveUserToll: Button
    lateinit var btnDebugAssignRandomToll: Button

    lateinit var tvCurrentLocation: TextView
    lateinit var tvPrevLocation: TextView
    lateinit var tvSpeed: TextView
    lateinit var tvBearing: TextView
    lateinit var tvRoadName: TextView
    lateinit var tvClosestToll: TextView
    lateinit var tvTollDistance: TextView

    private var gatesList: ArrayList<Gate> = ArrayList<Gate>()

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val gatesReference: DatabaseReference = database.reference.child("gates")
    private val usersReference: DatabaseReference = database.reference.child("users")

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            0
        )

        Intent(applicationContext, LocationService::class.java).apply {
            action = LocationService.ACTION_START
            startService(this)
        }

        utility = Utility(applicationContext)

        btnSignOut = findViewById(R.id.btnSignOut)
        btnDebugGetAllGates = findViewById(R.id.btnDebugGetAllGates)
        btnDebugGiveUserToll = findViewById(R.id.btnDebugGiveDummyTolls)
        btnDebugAssignRandomToll = findViewById(R.id.btnDebugAssignRandomToll)

        tvCurrentLocation = findViewById(R.id.tvCurrentVar)
        tvPrevLocation = findViewById(R.id.tvPrevVar)
        tvSpeed = findViewById(R.id.tvSpeedVar)
        tvBearing = findViewById(R.id.tvBearingVar)
        tvRoadName = findViewById(R.id.tvRoadVar)
        tvClosestToll = findViewById(R.id.tvClosestTollVar)
        tvTollDistance = findViewById(R.id.tvTollDistVar)


        btnSignOut.setOnClickListener{
            btnSignOut.isClickable = false
            auth.signOut()
            utility.woof("Signed Out", "Stupid")
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnDebugGetAllGates.setOnClickListener{
            getAllGatesFromDatabase()
        }

        btnDebugGiveUserToll.setOnClickListener {
            println("Current User ID: ${auth.currentUser?.uid}")

            addUserToDatabase()
        }

        btnDebugAssignRandomToll.setOnClickListener{
            assignRandomToll()
        }
    }

    private fun getAllGatesFromDatabase() {
        gatesReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (eachGate in snapshot.children) {
                    val gate: Gate? = eachGate.getValue(Gate::class.java)
                    gate?.let {
                        gatesList.add(gate)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    fun addUserToDatabase() {

        val id: String = auth.currentUser?.uid ?: return

        println("Generated ID: $id")
        println("Auth ID: ${auth.currentUser?.uid}")

        val tolls = ArrayList<Toll>()

        val timestamp: Timestamp = Timestamp(234827042)
        val toll: Toll = Toll("dummyString", timestamp)
        tolls.add(toll)
        tolls.add(toll)
        tolls.add(toll)

        val user: User = User(tolls)

        println("User: $user")

        usersReference.child(id).setValue(user).addOnCompleteListener { task ->

            if (task.isSuccessful) {
                utility.toastln("User Added Successfully")
            } else {
                utility.toastln("Error Adding User")
            }

        }
    }

    fun assignRandomToll() {
        println("Getting tolls hopefully")
        getTollsForUser { tolls ->

            val tollsList = ArrayList<Toll>()

            tolls.forEach { toll ->
                println(toll)
                tollsList.add(toll)
            }

            retrieveGatesFromDatabase { gates ->
                val userId: String = auth.currentUser!!.uid
                val randomIndex: Int = Random.nextInt(gates.lastIndex+1)
                val randomGate: Gate = gates[randomIndex]

                val timestamp: Timestamp = Timestamp(System.currentTimeMillis())
                val newToll: Toll = Toll(randomGate.id, timestamp)

                tollsList.add(newToll)

                usersReference.child(userId).child("tolls").setValue(tollsList).addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        utility.toastln("Error Adding Random Toll")
                    }
                }
                displayLatestToll()
            }
        }

    }

    private fun displayLatestToll() {
        retrieveGatesFromDatabase { gates ->
            getTollsForUser { tolls ->
                val latestTollIndex: Int = tolls.lastIndex
                val latestTollGateId: String = tolls[latestTollIndex].gateId
                for (gate in gates) {
                    if (gate.id == latestTollGateId) {
                        utility.toastln("$${gate.cost} at ${gate.name}")
                    }
                }

            }
        }

    }

    fun retrieveGatesFromDatabase(callback: (ArrayList<Gate>) -> Unit) {
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

    override fun onDestroy() {
        Intent(applicationContext, LocationService::class.java).apply {
            action = LocationService.ACTION_STOP
            startService(this)
        }
        super.onDestroy()
    }
}
