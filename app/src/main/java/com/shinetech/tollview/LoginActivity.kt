package com.shinetech.tollview

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    lateinit var editTextEmail: EditText
    lateinit var editTextPassword: EditText
    lateinit var btnLogIn: Button
    lateinit var btnSignUp: Button

    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        editTextEmail = findViewById(R.id.editTextLoginEmail)
        editTextPassword = findViewById(R.id.editTextLoginPassword)
        btnLogIn = findViewById(R.id.loginButtonLogIn)
        btnSignUp = findViewById(R.id.loginButtonSignUp)

        var email = editTextEmail.text.toString()
        var password = editTextPassword.text.toString()

        btnLogIn.setOnClickListener{
            email = editTextEmail.text.toString()
            password = editTextPassword.text.toString()
            logInUser(email, password)
        }
        btnSignUp.setOnClickListener{
            email = editTextEmail.text.toString()
            password = editTextPassword.text.toString()
            signUpWithFirebase(email, password)
        }
    }

    private fun signUpWithFirebase(email: String, password: String) {
        btnSignUp.isClickable = false

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if(task.isSuccessful){
                logInUser(email, password)
            }else{
                toastln("There was an error")
                println("${task.exception?.localizedMessage}")
            }
        }

    }

    private fun logInUser(userEmail: String, userPassword: String) {
        auth.signInWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(applicationContext, "Welcome", Toast.LENGTH_SHORT).show()

                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(applicationContext,"There was an error",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val user = auth.currentUser
        if (user != null){
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
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