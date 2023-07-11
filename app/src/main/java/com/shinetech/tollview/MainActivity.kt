package com.shinetech.tollview

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import com.shinetech.tollview.util.Utility

class MainActivity : AppCompatActivity() {

    lateinit var btnSignOut: Button
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    lateinit var utility: Utility
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnSignOut = findViewById(R.id.btnSignOut)

        utility = Utility(applicationContext)

        btnSignOut.setOnClickListener{
            btnSignOut.isClickable = false
            auth.signOut()
            utility.woof("Signed Out", "Stupid")
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}