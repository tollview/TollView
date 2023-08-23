package com.shinetech.tollview

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.core.app.ActivityCompat
import com.google.firebase.auth.FirebaseAuth
import com.shinetech.tollview.util.Utility
import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.widget.SeekBar
import android.widget.TextView
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class MainActivity : AppCompatActivity() {

    private lateinit var utility: Utility
    private lateinit var btnSignOut: Button
    private lateinit var btnUpdateValues: Button
    private lateinit var sbDistToToll: SeekBar
    private lateinit var sbReentryTime: SeekBar
    private lateinit var sbPingSpeed: SeekBar
    lateinit var tvTollTerminal: TextView
    lateinit var tvTodayTotalCost: TextView
    lateinit var tvDistToTollValue: TextView
    lateinit var tvReentryTimeValue: TextView
    lateinit var tvPingSpeedValue: TextView
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            when (intent.action) {
                "com.shinetech.tollview.ACTION_GATE_TEXT" -> {
                    val latestToll = intent.getStringExtra(LocationService.LocationServiceBroadcast.KEY_GATE_TEXT)
                    tvTollTerminal.append("\n$latestToll")
                }
                "com.shinetech.tollview.DEBUG_UPDATE" -> {
                    val todayTotalCost = intent.getStringExtra("todayTotalCost")
                    tvTodayTotalCost.text = todayTotalCost
                    val latestToll = intent.getStringExtra("latestToll")
                    latestToll?.let {
                        tvTollTerminal.append("\n$it")
                    }
                }
            }
        }
    }
    @SuppressLint("SetTextI18n")
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

        tvTodayTotalCost = findViewById(R.id.tvTodayTotalCost)

        sbDistToToll = findViewById(R.id.sbDistToToll)
        sbReentryTime = findViewById(R.id.sbReentryTime)
        sbPingSpeed = findViewById(R.id.sbPingSpeed)
        tvDistToTollValue = findViewById(R.id.tvDistToTollValue)
        tvReentryTimeValue = findViewById(R.id.tvReentryTimeValue)
        tvPingSpeedValue = findViewById(R.id.tvPingSpeedValue)

        btnUpdateValues = findViewById(R.id.btnUpdateValues)

        tvDistToTollValue.text = String.format("%.3f", 0.001 + sbDistToToll.progress / 1000.0)
        tvReentryTimeValue.text = sbReentryTime.progress.toString()
        tvPingSpeedValue.text = ((sbPingSpeed.progress / 100.0) + 1.0).toString()

        tvTollTerminal = findViewById(R.id.tvTollTerminal)

        btnUpdateValues.setOnClickListener {
            val intent = Intent("com.shinetech.tollview.DEBUG_UPDATE_SLIDERS")
            intent.putExtra("distToToll", 0.001 + sbDistToToll.progress / 1000.0)
            intent.putExtra("reentryTime", sbReentryTime.progress / 100.0)
            intent.putExtra("pingSpeed", ((sbPingSpeed.progress / 100.0) + 1.0) * 1000L)
            sendBroadcast(intent)
        }


        btnSignOut.setOnClickListener{
            btnSignOut.isClickable = false
            auth.signOut()
            utility.woof("Signed Out", "Stupid")
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        val filter = IntentFilter()
        filter.addAction("com.shinetech.tollview.DEBUG_UPDATE")

        sbDistToToll.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvDistToTollValue.text = String.format("%.3f", 0.001 + progress / 1000.0)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        sbReentryTime.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvReentryTimeValue.text = (progress / 100.0).toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        sbPingSpeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvPingSpeedValue.text = ((progress / 100.0) + 1.0).toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

    }
    override fun onResume() {
        super.onResume()
        val filter = IntentFilter()
        filter.addAction("com.shinetech.tollview.DEBUG_UPDATE")
        filter.addAction("com.shinetech.tollview.ACTION_GATE_TEXT")
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter)
    }
    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    }
    override fun onDestroy() {
        Intent(applicationContext, LocationService::class.java).apply {
            action = LocationService.ACTION_STOP
            startService(this)
        }
        super.onDestroy()
        unregisterReceiver(receiver)
    }
}
