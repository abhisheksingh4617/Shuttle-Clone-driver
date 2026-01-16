package com.shuttleclone.driver.Services

import android.Manifest
import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.*
import okhttp3.*
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.shuttleclone.driver.ui.Activity.MainActivity
import com.shuttleclone.driver.Util.*
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONException

import java.text.DecimalFormat



class BackGroundLocationService : Service(), ConnectionCallbacks,
    OnConnectionFailedListener, LocationListener {

    private val TAG = "BGLocationService"
    private val mainHandler = Handler(Looper.getMainLooper())
    var mLocationRequest: LocationRequest? = null
    var mGoogleApiClient: GoogleApiClient? = null

    var isLocationSet = false
    var previousLat = 0.0
    var previousLng = 0.0
    var locationDistance = 0.0
    var CHANNEL_ID = "Shuttle Driver"


    override fun onCreate() {
        super.onCreate()
        try {

            prepareForegroundNotification()
            mGoogleApiClient = GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()
            mGoogleApiClient?.connect()

        } catch (e: Exception) {
            myLog(TAG, "onCreate: Serivce create error=${e.localizedMessage}")
        }
    }


    override fun onBind(intent: Intent): IBinder? {
        // : Return the communication channel to the service.
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        try {
            myLog(TAG, "onStartCommand: Calledededed")
            try {
                prepareForegroundNotification()

                if (mGoogleApiClient == null) {
                    mGoogleApiClient = GoogleApiClient.Builder(this)
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .build()
                    mGoogleApiClient!!.connect()
                }

            } catch (e: Exception) {
                myLog(TAG, "onStartCommand: ${e.localizedMessage}")
            }

        } catch (e: Exception) {
            myLog(TAG, "onStartCommand: catch block Error=${e.localizedMessage}")
        }

        return START_REDELIVER_INTENT
    }

    private fun prepareForegroundNotification() {

        myLog(TAG, "prepareForegroundNotification: called")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Smart Shuttle Driver Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(serviceChannel)
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            2121, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        try {
            val notification =
                NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Location Service")
                    .setContentText("Getting background location.")
                    .setSmallIcon(com.shuttleclone.driver.R.drawable.ic_notification_logo)
                    .setContentIntent(pendingIntent)
                    .setSound(null)
                    .build()
            startForeground(AppConstants.FRG_SERVICE_NF_ID, notification)

        } catch (e: Exception) {
            myLog(TAG, "onCreate: ${e.localizedMessage}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        try {
            if (mGoogleApiClient != null)
                mGoogleApiClient!!.disconnect()

            myLog(TAG, "onDestroy: Socket Called")

        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }


    override fun onConnected(bundle: Bundle?) {
        try {
            mLocationRequest = LocationRequest.create()
            mLocationRequest?.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            mLocationRequest?.setInterval(INTERVAL) // Update location every second
            mLocationRequest?.setFastestInterval(FASTEST_INTERVAL)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                return
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient!!, mLocationRequest!!, this
            )
        } catch (e: Exception) {
            myLog(TAG, "onConnected: Error=" + e.localizedMessage)
        }
    }

    override fun onConnectionSuspended(i: Int) {
        myLog(TAG, "GoogleApiClient connection has been suspend")
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        myLog(TAG, "GoogleApiClient connection has failed")
    }

    override fun onLocationChanged(location: Location) {
        try {

            LiveUpdate.updateLocation.postValue(location)
            savePreference(this, AppConstants.DRIVER_LATITUDE, location.latitude.toString())
            savePreference(this, AppConstants.DRIVER_LONGITUDE, location.longitude.toString())
            savePreference(this, AppConstants.DRIVER_ANGLE, location.bearing.toString())
            myLog(TAG, "onLocationUpdated: angle=${getPreference(this, AppConstants.DRIVER_ANGLE)}")

            if (!isLocationSet) {
                previousLat = location.latitude
                previousLng = location.longitude
                updateDriverLiveLocation(location)
                isLocationSet = true
            }

            locationDistance = getDistanceFromLatLonInM(
                previousLat,
                previousLng,
                location.latitude,
                location.longitude
            )

            myLog(TAG, "DIFFERENCE=${locationDistance}")
            val speed = DecimalFormat().format(location.speed.toLong()).toFloat()
            myLog(TAG, "SPEED=$speed")

            if (locationDistance >= AppConstants.MIN_DIST_FOR_LOCATION_UPDATE) {
//                bearingBetweenLocations(LatLng(previousLat,previousLng),LatLng(location.latitude,location.longitude))
                updateDriverLiveLocation(location)
                previousLat = location.latitude
                previousLng = location.longitude
            }


        } catch (e: Exception) {
            myLog(TAG, "onLocationUpdated: Error=${e.localizedMessage}")
        }
    }

    private fun updateDriverLiveLocation(location: Location) {
        try {
            if (!isNetworkAvailable(this)) return

            val token = getPreference(this, AppConstants.TOKEN) ?: return
            val assignedId = getPreference(this, AppConstants.ASSIGNED_ID) ?: ""

            val json = org.json.JSONObject().apply {
                put("assignedId", assignedId)
                put("status", "RIDING")
                put("latitude", location.latitude)
                put("longitude", location.longitude)
                put("bearing", location.bearing)
                put("speed", location.speed)
            }

            val client = okhttp3.OkHttpClient()
            val requestBody = okhttp3.RequestBody.create(
                "application/json".toMediaTypeOrNull(),
                json.toString()
            )

            val request = okhttp3.Request.Builder()
                .url("https://yourapi.com/api/tracking-status")  // अपना Backend URL
                .addHeader("Authorization", "Bearer $token")
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                    // ✅ Handler use करें - NO runOnUiThread
                    mainHandler.post {
                        myLog(TAG, "❌ Location API failed: ${e.message}")
                    }
                }

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    mainHandler.post {
                        myLog(TAG, "✅ LIVE LOCATION SENT: ${location.latitude}, ${location.longitude}")
                    }
                }
            })

        } catch (e: Exception) {
            mainHandler.post {
                myLog(TAG, "Live tracking error: ${e.localizedMessage}")
            }
        }
    }






    companion object {
        private const val INTERVAL = 5000.toLong()
        private const val FASTEST_INTERVAL = 3000.toLong()
    }


}