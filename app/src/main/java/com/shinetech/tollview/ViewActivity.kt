package com.shinetech.tollview

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.shinetech.tollview.models.Gate
import com.shinetech.tollview.models.Toll
import com.shinetech.tollview.util.Utility
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class ViewActivity : AppCompatActivity() {
    private lateinit var tvViewTerminal: TextView
    private lateinit var btnViewtoHome: Button
    private lateinit var utility: Utility

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view)
        setupViewsById()
        setupButtons()
        utility = Utility(applicationContext)

        var gatesList = ArrayList<Gate>()
        utility.fetchGatesFromDatabase {
            gatesList = it
        }

        val gatesMap = gatesList.associateBy { it.id }

        utility.getTollsForUser { tolls: ArrayList<Toll> ->
            val endDate = LocalDate.now()
            val startDate = endDate.minusDays(90)

            val tollsByDate = tolls.groupBy { toll ->
                toll.timestamp?.let {
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(it.time), ZoneId.systemDefault()).toLocalDate()
                }
            }

            for ((date, tollsForDay) in tollsByDate) {
                date?.let {
                    if (it.isAfter(startDate) && it.isBefore(endDate.plusDays(1))) {
                        val totalCostForDay = tollsForDay.sumOf { toll ->
                            gatesMap[toll.gateId]?.cost ?: 0.0
                        }
                        tvViewTerminal.append("\n\nDate: $it, Total Cost: $totalCostForDay")
                    }
                }
            }
        }
    }

    private fun setupViewsById() {
        tvViewTerminal = findViewById(R.id.tvViewTerminal)
        btnViewtoHome = findViewById(R.id.btnViewToHome)
    }
    private fun setupButtons() {
        btnViewtoHome.setOnClickListener {
            val intent = Intent(this@ViewActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }


}
