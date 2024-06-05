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
import java.time.format.DateTimeFormatter

class ViewActivity : AppCompatActivity() {
    private lateinit var tvViewTerminal: TextView
    private lateinit var btnViewtoHome: Button
    private lateinit var utility: Utility
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyMMdd")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view)
        setupViewsById()
        setupButtons()
        utility = Utility(applicationContext)

        var gatesMap = emptyMap<String, Gate>()
        var gatesList = emptyList<Gate>()
        utility.fetchGatesFromDatabase {
            gatesList = it
            gatesMap = gatesList.associateBy { it.id }
        }


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
                        println("gatesList size = ${gatesList.size}")
                        val totalCostForDay = tollsForDay.sumOf { toll ->
                            gatesMap[toll.gateId]?.cost ?: 0.0
                        }
                        val formattedDate = it.format(dateFormatter)
                        val formattedTotalCost = String.format("%.2f", totalCostForDay)
                        tvViewTerminal.append("\n\n$formattedDate, $$formattedTotalCost")
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
