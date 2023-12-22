package com.shinetech.tollview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.shinetech.tollview.models.Toll
import com.shinetech.tollview.util.Utility

class ViewActivity : AppCompatActivity() {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val usersReference: DatabaseReference = database.reference.child("users")
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var utility: Utility

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view)
        utility = Utility(applicationContext)

        val emptyTollsList: ArrayList<Toll> = ArrayList()
        val userId = auth.currentUser!!.uid

//        usersReference.child(userId).child("tolls").setValue(emptyTollsList)
//            .addOnCompleteListener { task ->
//                if (!task.isSuccessful){
//                    utility.toast("Error deleting all the everything")
//                }
//            }
//        usersReference.child(userId).child("tolls").removeValue()
    }
}
