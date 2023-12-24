package com.shinetech.tollview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.shinetech.tollview.models.Toll
import com.shinetech.tollview.util.Utility
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset


class ViewActivity : AppCompatActivity() {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val usersReference: DatabaseReference = database.reference.child("users")
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var utility: Utility

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view)
        utility = Utility(applicationContext)
        utility.getTollsForUser { tolls: ArrayList<Toll> ->
            val startDate = LocalDate.of(2020, 1, 1)
            val endDate = LocalDate.of(2024, 1, 31)

            val tollsBetweenDates = tolls.filter { toll ->
                val tollTimestamp = toll.timestamp?.let {
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(it.time), ZoneId.systemDefault())
                }

                tollTimestamp?.let {
                    val tollDate = it.toLocalDate()
                    tollDate.isAfter(startDate) && tollDate.isBefore(endDate.plusDays(1))
                } ?: false // Handle cases where timestamp is null
            }
            println("tolls between dates: $tollsBetweenDates")
            println("number of tolls between dates: ${tollsBetweenDates.size}")

            // Use tollsBetweenDates as needed
        }
    }


}
