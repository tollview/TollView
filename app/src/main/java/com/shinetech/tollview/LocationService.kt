package com.shinetech.tollview

import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Address
import android.location.Geocoder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.shinetech.tollview.models.Gate
import com.shinetech.tollview.models.Toll
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
import java.lang.Math.PI
import java.lang.Math.atan2
import java.lang.Math.cos
import java.lang.Math.sqrt
import java.lang.Math.toDegrees
import java.lang.Math.toRadians
import java.sql.Timestamp
import java.util.Locale
import kotlin.math.pow
import java.lang.Math.sin

class LocationService: Service() {
    private var MINIMUM_TOLL_REENTRY_TIME: Double = 0.5
    private var DISTANCE_TUNING_PARAMETER: Double = 0.008
    private var PING_SPEED: Long = 1_300L

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var utility: Utility
    private lateinit var locationClient: LocationClient

    private var numPings: Int = 0

    private var prevLongitude: Double = 0.0
    private var prevLatitude: Double = 0.0
    private var currLongitude: Double = 0.0
    private var currLatitude: Double = 0.0

    private var bearing: Double = 0.0

    private var userTolls: ArrayList<Toll> = ArrayList<Toll>()

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val usersReference: DatabaseReference = database.reference.child("users")
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()


    private var todayTotalCost: Double = 0.0

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {

            val distToToll = intent.getDoubleExtra("distToToll", 0.25)
            DISTANCE_TUNING_PARAMETER = distToToll

            val reentryTime = intent.getDoubleExtra("reentryTime", 1.0)
            MINIMUM_TOLL_REENTRY_TIME = reentryTime

            val pingSpeed: Long = intent.getLongExtra("pingSpeed", 2000)
            PING_SPEED = pingSpeed
        }
    }

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

        utility.getTollsForUser {
            userTolls = it
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
            .setContentTitle("Welcome to TollView")
            .setContentText("Viewing Tolls...")
            .setSmallIcon(com.google.android.material.R.drawable.design_password_eye)
//            .setOngoing(true)
        // TODO: break this notification off into two. Tracking checker is its own ongoing; tolls are viewed on another.

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val filter = IntentFilter()
        filter.addAction("com.shinetech.tollview.DEBUG_UPDATE_SLIDERS")
        registerReceiver(receiver, filter)

        locationClient
            .getLocationUpdates(PING_SPEED)
            .catch { e -> e.printStackTrace()}
            .onEach { location ->
                numPings += 1
                currLatitude = location.latitude
                currLongitude = location.longitude

                val closestGate = utility.getClosestGate(currLatitude, currLongitude)

                if (isAtGate(closestGate) && timeoutExpired() && numPings >= 2) {
                    incurToll(closestGate, notification, notificationManager)
                }

                val intent = Intent("com.shinetech.tollview.DEBUG_UPDATE")
                intent.putExtra("closestToll", closestGate.name)
                intent.putExtra("todayTotalCost", "$todayTotalCost")
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

                prevLatitude = currLatitude
                prevLongitude = currLongitude
            }
            .launchIn(serviceScope)

        startForeground(1, notification.build())
    }

    private fun incurToll(
        closestGate: Gate,
        notification: NotificationCompat.Builder,
        notificationManager: NotificationManager
    ) {
        val tollIncurred = Toll(closestGate.id, Timestamp(System.currentTimeMillis()))
        val userId: String = auth.currentUser!!.uid
        userTolls.add(tollIncurred)
        todayTotalCost += closestGate.cost

        val intent = Intent("com.shinetech.tollview.ACTION_GATE_TEXT")
        intent.putExtra(
            LocationServiceBroadcast.KEY_GATE_TEXT,
            "$${closestGate.cost} at ${closestGate.name}"
        )
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

        val updatedNotification = notification.setContentText(
            "at ${closestGate.name}"
        )
        notification.setContentTitle("$${closestGate.cost}")
        notificationManager.notify(1, updatedNotification.build())

        utility.getTollsForUser { tolls ->
            tolls.add(tollIncurred)
            usersReference.child(userId).child("tolls").setValue(tolls)
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        utility.toast("Error Adding Toll to Database")
                    }
                }
        }
    }

    private fun distanceBetweenPoints(otherLat: Double, otherLong: Double): Double {
        val earthRadius = 3958.8 // in mi
        val lat1 = currLatitude * PI / 180
        val long1 = currLongitude * PI / 180
        val lat2 = otherLat * PI / 180
        val lon2 = otherLong * PI / 180

        val distLat = lat2 - lat1
        val distLong = lon2 - long1

        val a = sin(distLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(distLong / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }

    private fun timeoutExpired(): Boolean {

        if (!userTolls.isNullOrEmpty()) {
            val latestTollTimestamp = userTolls[userTolls.lastIndex].timestamp
            val currentTimestamp: Timestamp = Timestamp(System.currentTimeMillis())


            latestTollTimestamp?.let {
                val timeDelta: Double = (currentTimestamp.time - latestTollTimestamp.time)/60_000.0
                return timeDelta >= MINIMUM_TOLL_REENTRY_TIME
            }
        }

        return false
    }

    private fun isAtGate(closestGate: Gate): Boolean {
        val distance = distanceBetweenPoints(closestGate.latitude, closestGate.longitude)
        return distance <= DISTANCE_TUNING_PARAMETER
    }

    private fun getRoadName(latitude: Double, longitude: Double, context: Context): String {

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
        utility.getTollsForUser { tolls ->
            val userId: String = auth.currentUser!!.uid
            val tollsToPush: ArrayList<Toll> = tolls
            tollsToPush.addAll(userTolls)
            usersReference.child(userId).child("tolls").setValue(tollsToPush).addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    utility.toast("Error Synchronizing Tolls")
                }
            }
        }
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }

    object LocationServiceBroadcast {
        const val KEY_GATE_TEXT = "KEY_GATE_TEXT"
    }
}
