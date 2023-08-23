package com.shinetech.tollview

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.shinetech.tollview.util.Utility

class LoginActivity : AppCompatActivity() {
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var btnLogIn: Button
    private lateinit var btnSignUp: Button

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var utility: Utility

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        editTextEmail = findViewById(R.id.editTextLoginEmail)
        editTextPassword = findViewById(R.id.editTextLoginPassword)
        btnLogIn = findViewById(R.id.loginButtonLogIn)
        btnSignUp = findViewById(R.id.loginButtonSignUp)

        var email: String
        var password: String

        utility = Utility(applicationContext)

        btnLogIn.setOnClickListener{
            email = editTextEmail.text.toString()
            password = editTextPassword.text.toString()
            logInWithFirebase(email, password)
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
                logInWithFirebase(email, password)
            }else{
                utility.toastln("There was an error")
                println("${task.exception?.localizedMessage}")
            }
        }

    }

    private fun logInWithFirebase(userEmail: String, userPassword: String) {
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
}