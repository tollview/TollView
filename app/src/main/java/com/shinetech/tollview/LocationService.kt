package com.shinetech.tollview

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationServices
import com.shinetech.tollview.models.Gate
import com.shinetech.tollview.models.Toll
import com.shinetech.tollview.util.LocationClient
import com.shinetech.tollview.util.Point
import com.shinetech.tollview.util.Utility
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.lang.Exception
import java.lang.Math.abs
import java.lang.Math.atan2
import java.lang.Math.cos
import java.lang.Math.sin
import java.lang.Math.toDegrees
import java.lang.Math.toRadians
import java.sql.Timestamp
import java.util.Locale

class LocationService: Service() {
    private val MINIMUM_TOLL_REENTRY_TIME: Double = 1.0
    private val DISTANCE_TUNING_PARAMETER: Double = 0.278
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var utility: Utility
    private lateinit var locationClient: LocationClient

    private val PING_SPEED: Long = 2_000L
    private var num_pings: Int = 0

    private var prevLongitude: Double = 0.0
    private var prevLatitude: Double = 0.0
    private var currLongitude: Double = 0.0
    private var currLatitude: Double = 0.0

    private var bearing: Double = 0.0

    private var userTolls: ArrayList<Toll> = ArrayList<Toll>()

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        locationClient= DefaultLocationClient(
            applicationContext,
            LocationServices.getFusedLocationProviderClient(applicationContext)
        )
        utility = Utility(applicationContext)

        utility.getTollsForUser { tolls ->
            tolls.forEach {
                userTolls.add(it)
            }
        }
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
                    println("Speed: ${location.speed}")
                }
                if (num_pings >= 3) {
                    updateBearing()
                    println("Bearing: $bearing")

                }

                val closestGate = utility.getClosestGate(currLatitude, currLongitude)


                println("isAtGate: ${isAtGate(closestGate)}")


                println("isAtGate(): ${isAtGate(closestGate)}")
                println("timeoutExpired(): ${timeoutExpired()}")

                if (isAtGate(closestGate) && timeoutExpired()) {
                    // incur toll

                    println("YOU GOT A TOLL")

                }

                val updatedNotification = notification.setContentText(
                    "going ${location.speed} mps, bearing: $bearing D: ${prevLatitude - currLatitude}"
                )

                println("Closest Gate: ${closestGate.name}")

                println("userToll Size: ${userTolls.size}")

//                notificationManager.notify(1, updatedNotification.build())
                println("-------->> $prevLatitude , $currLatitude <<--------")

                val intent = Intent("com.shinetech.tollview.DEBUG_UPDATE")
                intent.putExtra("currentLocation", "$currLatitude , $currLongitude")
                intent.putExtra("previousLocation", "$prevLatitude , $prevLongitude")
                intent.putExtra("speed", "${location.speed}")
                intent.putExtra("bearing", "$bearing")
                intent.putExtra("roadName", "$roadName")
                intent.putExtra("closestToll", "${closestGate.name}")
                intent.putExtra("TollDist", "idk")
                sendBroadcast(intent)

                prevLatitude = currLatitude
                prevLongitude = currLongitude
            }
            .launchIn(serviceScope)

        startForeground(1, notification.build())
    }

    private fun timeoutExpired(): Boolean {

        if (!userTolls.isNullOrEmpty()) {
            println("Actually calculated it this time")
            val latestTollTimestamp = userTolls[userTolls.lastIndex].timestamp
            val currentTimestamp: Timestamp = Timestamp(System.currentTimeMillis())


            latestTollTimestamp?.let {
                print("Go past the let")
                val timeDelta: Double = (currentTimestamp.time - latestTollTimestamp.time)/60_000.0
                println("delta: ${timeDelta}")
                return timeDelta >= MINIMUM_TOLL_REENTRY_TIME
            }
        }

        return false
    }

    private fun isAtGate(closestGate: Gate): Boolean {
        val currentPosition: Point = Point(currLatitude, currLongitude)
        val closestGatePoint: Point = Point(closestGate.latitude, closestGate.longitude)
        val distance: Double = currentPosition.distanceToOtherPoint(closestGatePoint)

        return distance <= DISTANCE_TUNING_PARAMETER
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

        if (!addresses.isNullOrEmpty()) {
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
