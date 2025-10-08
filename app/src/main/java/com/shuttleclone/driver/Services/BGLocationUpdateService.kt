package com.shuttleclone.driver.Services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.Nullable
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.shuttleclone.driver.ui.Activity.MainActivity
import com.shuttleclone.driver.RetrofitRepository.RetrofitClient
import com.shuttleclone.driver.Util.*
import com.google.android.gms.location.*
import com.google.gson.Gson
import com.shuttleclone.driver.Model.DefaultResponse
import com.shuttleclone.driver.Util.AppConstants.APP_NAME
import com.shuttleclone.driver.Util.AppConstants.CHANNEL_NAME
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import java.text.DecimalFormat
import com.shuttleclone.driver.R


class BGLocationUpdateService : Service() {

    private val TAG = "BGLocationUpdateService"

    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var locationRequest: LocationRequest? = null

    var isLocationSet = false
    var previousLat = 0.0
    var previousLng = 0.0
    var locationDistance = 0.0
    var CHANNEL_ID = APP_NAME

    companion object {
        private var UPDATE_INTERVAL_IN_MILLISECONDS: Long = 8000
        private const val MAX_UPDATE_DELAY_IN_MILLISECONDS: Long = 5000
    }

    override fun onCreate() {
        super.onCreate()
        initData()
    }


    //Location Callback
    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            val currentLocation: Location = locationResult.lastLocation!!
            myLog(TAG,"Latitude=${currentLocation.latitude},Longitude=${currentLocation.longitude}")
            //Share/Publish Location
            onLocationUpdated(currentLocation)
        }
    }

    private fun onLocationUpdated(location: Location) {
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
            if (isNetworkAvailable(this)) {
                if (isPreference(this,AppConstants.IS_BOOKING_ASSIGNED) && isPreference(this,AppConstants.IS_TRIP_STARTED)
                    &&!getPreference(this,AppConstants.ASSIGNED_ID).equals(""))

                    RetrofitClient.getClient()
                        .updateDriverLocationStatus(
                            getPreference(this, AppConstants.TOKEN)!!,
                            getPreference(this,AppConstants.ASSIGNED_ID).toString(),
                            getPreference(this,AppConstants.DRIVER_STATUS).toString(),
                            "${getLocationName(location.latitude,location.longitude,this)}",
                            "${location.bearing}",
                            "${location.latitude}",
                            "${location.longitude}"
                        ).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableSingleObserver<DefaultResponse?>() {
                            override fun onSuccess(response: DefaultResponse) {
                                myLog(TAG, "updateDriverLocationStatus: Response=${Gson().toJson(response)}")
                                LiveUpdate.updateTrackStatus.postValue(response)
                            }

                            override fun onError(e: Throwable) {
                                myLog(TAG, "onError: locationChanged=" + e.localizedMessage)
                            }
                        })

            }
        } catch (e: Exception) {
            myLog(TAG, "updateDriverLocationStatus: Error=${e.localizedMessage}")
        }
    }

    /*private fun updateDriverLiveLocation(location: Location) {
        try {
            if (isNetworkAvailable(this)) {
                if (isPreference(this,AppConstants.IS_BOOKING_ASSIGNED) && isPreference(this,AppConstants.IS_TRIP_STARTED))

                    RetrofitClient.getClient()
                        .updateTrackingStatus(
                            getPreference(this, AppConstants.TOKEN)!!,
                            getPreference(this,AppConstants.ASSIGNED_ID).toString(),
                            getPreference(this,AppConstants.TRIP_STATUS).toString(),
                            "${location.latitude}",
                            "${location.longitude}",
                            "${location.bearing}"
                        ).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableSingleObserver<TrackingStatusResponse?>() {
                            override fun onSuccess(response: TrackingStatusResponse) {
                                myLog(TAG, "updateDriverLiveLocation: Response=${Gson().toJson(response)}")
                                LiveUpdate.updateTrackStatus.postValue(response)
                            }

                            override fun onError(e: Throwable) {
                                myLog(TAG, "onError: locationChanged=" + e.localizedMessage)
                            }
                        })

            }
        } catch (e: Exception) {
            myLog(TAG, "updateDriverLiveLocation: Error=${e.localizedMessage}")
        }
    }*/

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        prepareForegroundNotification()
        startLocationUpdates()
        return START_STICKY
    }

    private fun startLocationUpdates() {

        myLog(TAG, "startLocationUpdates: called")

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

        if (locationRequest == null || mFusedLocationClient == null) {
            myLog(TAG, "startLocationUpdates: locationRequest is null")
            initData()
            startLocationUpdates()
        } else {
            if (isPreference(this,AppConstants.IS_BOOKING_ASSIGNED))
                mFusedLocationClient!!.requestLocationUpdates(
                    locationRequest!!,
                    locationCallback, Looper.getMainLooper()!!
                )
        }
    }

    private fun prepareForegroundNotification() {

        myLog(TAG, "prepareForegroundNotification: called")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
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
                    .setContentTitle("Trip Tracking Location Service")
                    .setContentText("Getting background location.")
                    .setSmallIcon(R.drawable.ic_notification_logo)
                    .setColor(ContextCompat.getColor(this,R.color.green))
                    .setContentIntent(pendingIntent)
                    .setSound(null)
                    .build()
            startForeground(AppConstants.FRG_SERVICE_NF_ID, notification)

        } catch (e: Exception) {
            myLog(TAG, "onCreate: ${e.localizedMessage}")
        }
    }

    @Nullable
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun initData() {
        myLog(TAG, "initData: Called")
        prepareForegroundNotification()

        try {
            val commonData=getCommonDataDetails(this)
            UPDATE_INTERVAL_IN_MILLISECONDS= commonData.backgroundLocationUpdateInterval!!
        }catch (e:java.lang.Exception){
            Log.i(TAG, "initData: Error=${e.localizedMessage}")
        }

        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL_IN_MILLISECONDS)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(UPDATE_INTERVAL_IN_MILLISECONDS)
            .setMaxUpdateDelayMillis(MAX_UPDATE_DELAY_IN_MILLISECONDS)
            .build()

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mFusedLocationClient!=null&&locationCallback!=null)
            mFusedLocationClient!!.removeLocationUpdates(locationCallback)

        myLog(TAG, "onDestroy: Called")
    }
}