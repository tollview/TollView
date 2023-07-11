package com.shinetech.tollview

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    lateinit var btnSignOut: Button
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnSignOut = findViewById(R.id.btnSignOut)

        btnSignOut.setOnClickListener{
            btnSignOut.isClickable = false
            auth.signOut()
            woof("User, ", "GOODBYE")
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()

        }
    }
    private fun toastln(s: String) {
        Toast.makeText(
            applicationContext,
            s,
            Toast.LENGTH_SHORT
        ).show()
    }
    private fun woof(name: String, s: String) {
        println("$name: $s")
        toastln("$name: $s")
    }
}