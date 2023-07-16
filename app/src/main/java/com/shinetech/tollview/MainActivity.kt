package com.shinetech.tollview

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
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

class MainActivity : AppCompatActivity() {

    lateinit var utility: Utility

    lateinit var btnSignOut: Button
    lateinit var btnDebugGetAllGates: Button
    lateinit var btnDebugGiveUserToll: Button
    lateinit var btnDebugAssignRandomToll: Button

    private var gatesList: ArrayList<Gate> = ArrayList<Gate>()

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val gatesReference: DatabaseReference = database.reference.child("gates")
    private val usersReference: DatabaseReference = database.reference.child("users")

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        utility = Utility(applicationContext)

        btnSignOut = findViewById(R.id.btnSignOut)
        btnDebugGetAllGates = findViewById(R.id.btnDebugGetAllGates)
        btnDebugGiveUserToll = findViewById(R.id.btnDebugGiveDummyTolls)
        btnDebugAssignRandomToll = findViewById(R.id.btnDebugAssignRandomToll)

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
                        println(gate.name)
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

        val tolls: ArrayList<Toll> = ArrayList<Toll>()

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

        retrieveGatesFromDatabase { gates ->
            val userId: String = auth.currentUser!!.uid
            val randomIndex: Int = Random.nextInt(gates.lastIndex+1)
            val randomGate: Gate = gates[randomIndex]
            utility.toastln(gates[randomIndex].name)

            val tolls: ArrayList<Toll> = ArrayList<Toll>()
            val timestamp: Timestamp = Timestamp(System.currentTimeMillis())
            val toll: Toll = Toll(randomGate.id, timestamp)
            tolls.add(toll)

            val user: User = User(tolls)


            usersReference.child(userId).child("tolls").setValue(tolls).addOnCompleteListener {task ->
                if (task.isSuccessful) {
                    utility.toastln("Random toll added")
                } else {
                    utility.toastln("Error adding the user code 048x930-503750.png")
                }

            }
        }


    }

    fun retrieveGatesFromDatabase(callback: (ArrayList<Gate>) -> Unit) {
        val gatesList: ArrayList<Gate> = ArrayList<Gate>()

        gatesReference.addValueEventListener(object: ValueEventListener {
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
}