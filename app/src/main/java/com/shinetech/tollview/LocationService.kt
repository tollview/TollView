package com.shinetech.tollview

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationServices
import com.shinetech.tollview.util.LocationClient
import com.shinetech.tollview.util.Utility
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.lang.Exception
import java.lang.Math.atan2
import java.lang.Math.cos
import java.lang.Math.sin
import java.lang.Math.toDegrees
import java.lang.Math.toRadians
import java.util.Locale

class LocationService: Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var utility: Utility
    private lateinit var locationClient: LocationClient

    private val PING_SPEED: Long = 500L
    private var num_pings: Int = 0

    private var prevLongitude: Double = 0.0
    private var prevLatitude: Double = 0.0
    private var currLongitude: Double = 0.0
    private var currLatitude: Double = 0.0

    private var bearing: Double = 0.0

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        locationClient= DefaultLocationClient(
            applicationContext,
            LocationServices.getFusedLocationProviderClient(applicationContext)
        )
        utility= Utility(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun start() {
        val notification = NotificationCompat.Builder(this,"location")
            .setContentTitle("Tracking location...")
            .setContentText("Location: null")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setOngoing(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        locationClient
            .getLocationUpdates(PING_SPEED)
            .catch { e -> e.printStackTrace()}
            .onEach { location ->

                num_pings += 1

                println("------------------------------")
                println("NEW PING: $num_pings")
                println("------------------------------")

                currLatitude = location.latitude
                currLongitude = location.longitude

                utility.getClosestGate(currLatitude, currLongitude)

                var roadName = ""
                if (num_pings >= 2) {
                    roadName = getRoadName(currLatitude, currLongitude, applicationContext)
                    updateBearing()
                    println("Speed: ${location.speed}")
                    println("Bearing: $bearing")
                }

                val closestGate = utility.getClosestGate(currLatitude, currLongitude)

                val updatedNotification = notification.setContentText(
                    "Road Name: $roadName, going ${location.speed} mps, bearing: $bearing"
                )

                println("Closest Gate: ${closestGate.name}")

                notificationManager.notify(1, updatedNotification.build())

                prevLatitude = currLatitude
                prevLongitude = currLongitude
            }
            .launchIn(serviceScope)

        startForeground(1, notification.build())
    }

    fun getRoadName(latitude: Double, longitude: Double, context: Context): String {

        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses: List<Address>?

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1)
        } catch (e: Exception) {
            e.printStackTrace()
            return "Error in Geocoding"
        }

        if (addresses != null && addresses.isNotEmpty()) {
            val address = addresses[0]
            return address.thoroughfare ?: "Unknown Road"
        }
        return "Addresses was null or empty"
    }


    private fun updateBearing() {

        val validLat = prevLatitude != 0.0 && currLatitude != 0.0
        val validLong = prevLongitude != 0.0 && currLongitude != 0.0

        if (validLat && validLong) {
            bearing = calculateBearing(prevLatitude, prevLongitude, currLatitude, currLongitude).toDouble()
        }
    }

    private fun calculateBearing(startLatitude: Double, startLongitude: Double, endLatitude: Double, endLongitude: Double): Float {

        val deltaLongitude = endLongitude - startLongitude


        println("Delta Long: $deltaLongitude")

        val y = sin(toRadians(deltaLongitude)) * cos(toRadians(endLatitude))
        val x = cos(toRadians(startLatitude)) * sin(toRadians(endLatitude)) -
                sin(toRadians(startLatitude)) * cos(toRadians(endLatitude)) *
                cos(toRadians(deltaLongitude))

        val bearing = toDegrees(atan2(y, x)).toFloat()
        return (bearing + 360) % 360
    }


    private fun stop() {
        stopForeground(true)
        stopSelf()

    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }
}
