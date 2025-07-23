package com.shuttleclone.driver.ui.Activity

import android.animation.ValueAnimator
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SmoothScroller
import com.shuttleclone.driver.ui.Adapters.ViewStopsAdapter
import com.shuttleclone.driver.R
import com.shuttleclone.driver.Util.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.shuttleclone.driver.Model.RoutesItem


class BusRoutesActivity : AppCompatActivity(), OnMapReadyCallback {

    var ivBack: ImageView? = null
    var ivNotification: ImageView? = null
    var tvStopName: TextView? = null
    var tripsData: RoutesItem? = null
    val TAG = "BusRoutesActivity"
    private var mapView: MapView? = null
    val markers: MutableList<Marker> = ArrayList()
    private var map: GoogleMap? = null
    private var rvBusRoutes: RecyclerView? = null
    private lateinit var currentMarker: Marker
    private lateinit var smoothScroller: SmoothScroller
    private var nextStopTitle=""
    private var stopsAdapter: ViewStopsAdapter?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocaleManager().setLocale(this)
        setContentView(R.layout.activity_bus_routes)

        if (intent != null) {
            try {
                tripsData = intent.getSerializableExtra("tripsData") as RoutesItem
            } catch (e: Exception) {
                myLog(TAG, "onCreate: Error=${e.localizedMessage}")
            }

        }

        initLayouts()
        doOperationOnLayouts()

        mapView = findViewById<View>(R.id.mapview) as MapView
        mapView!!.onCreate(savedInstanceState)
        mapView!!.getMapAsync(this)

    }

    /* init layout */
    private fun initLayouts() {
        ivBack = findViewById(R.id.ivBack)
        ivNotification = findViewById(R.id.ivNotification)
        rvBusRoutes = findViewById(R.id.rvBusRoutes)
        tvStopName = findViewById(R.id.tvStopName)
        smoothScroller = object : LinearSmoothScroller(this) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_START
            }
        }

        updateStop()

    }

    private fun updateStop() {
        try {
           /* LiveUpdate.updateTrackStatus.observe(this, Observer {
                it.data.let {
                    nextStopTitle = it?.nextStop?.title.toString()

                    if (!nextStopTitle.equals(""))
                        tvStopName?.text="Next Stop->$nextStopTitle"

                    myLog("updateTrackStatus", "Next Stop=$nextStopTitle")

                    tripsData?.stops.let {
                        var count=0
                        for (stop in it!!) {
                            count++
                            if (nextStopTitle.equals(stop.name)){
                                break
                            }
                        }
                        rvBusRoutes!!.layoutManager.let {
                            it?.scrollToPosition(count)
                            stopsAdapter?.let {
                                it.setNextStop(nextStopTitle)
                            }
                        }
                    }
                }
            })*/
        }catch (e:Exception){
            myLog(TAG, "updateStop: Error=${e.localizedMessage}")}
    }

    /* add functionality to layout */
    private fun doOperationOnLayouts() {

        ivBack!!.setOnClickListener { finish() }
        ivNotification!!.setOnClickListener { NotificationActivity::class.java }

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onMapReady(gmap: GoogleMap) {
        try {
            map = gmap
            map!!.uiSettings.isMapToolbarEnabled = false
            map!!.uiSettings.isZoomControlsEnabled = false
            map!!.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))

            if (tripsData!!.stops!!.size != 0) {

                val stops = tripsData!!.stops
                stopsAdapter = ViewStopsAdapter(this@BusRoutesActivity, stops)
                val polylineOptions = PolylineOptions()

                for (i in 0 until stops!!.size) {
                    markers.add(
                        createMarker(
                            stops!!.get(i)!!.lat!!,
                            stops.get(i)!!.lng!!,
                            stops.get(i)!!.name,
                            stops.get(i)!!.name,
                            R.drawable.bus_stop_pin
                        )!!
                    )
                    polylineOptions.add(LatLng(stops.get(i)!!.lat!!, stops.get(i)!!.lng!!))
                }


                val builder = LatLngBounds.Builder()
                for (marker in markers) {
                    builder.include(marker.position)
                }
                val bounds = builder.build()
                val cu = CameraUpdateFactory.newLatLngBounds(bounds, 100)
                map!!.moveCamera(cu)
                map!!.animateCamera(cu, 2000, null);

                polylineOptions
                    .width(5f)
                    .color(Color.RED)
                map!!.addPolyline(polylineOptions)

                rvBusRoutes!!.apply {
                    layoutManager =
                        LinearLayoutManager(this@BusRoutesActivity, RecyclerView.VERTICAL, false)
                    adapter = stopsAdapter
                    setHasFixedSize(true)
                }
                RunLayoutAnimation(this, rvBusRoutes!!)

            }

            val latlng = LatLng(
                getPreference(this, AppConstants.DRIVER_LATITUDE)!!.toDouble(),
                getPreference(this, AppConstants.DRIVER_LONGITUDE)!!.toDouble()
            )

            currentMarker = map!!.addMarker(
                MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.mipmap.map_bus))
                    .position(
                        latlng
                    ).anchor(0.50f, 0.50f)
            )!!

            map!!.animateCamera(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.Builder().target(latlng)
                        .bearing(0f)
                        .zoom(17f)
                        .tilt(45F).build()
                )
            )

            LiveUpdate.updateLocation.observe(this, Observer {
                myLog(TAG, "onMapReady: ${it.latitude}")

                animateMarker(it, currentMarker)

                map!!.animateCamera(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.Builder().target(LatLng(it.latitude, it.longitude))
                            .bearing(it.bearing)
                            .zoom(17f)
                            .tilt(45F).build()
                    )
                )
            })
        } catch (e: Exception) {
            myLog(TAG, "onMapReady: Error=${e.localizedMessage}")
        }
    }

    fun animateMarker(destination: Location, marker: Marker?) {
        if (marker != null) {
            val startPosition = marker.position
            val endPosition = LatLng(destination.getLatitude(), destination.getLongitude())
            val startRotation = marker.rotation
            val latLngInterpolator: LatLngInterpolator = LatLngInterpolator.LinearFixed()
            val valueAnimator: ValueAnimator = ValueAnimator.ofFloat(0f, 1f)
            valueAnimator.setDuration(1000) // duration 1 second
            valueAnimator.setInterpolator(LinearInterpolator())
            valueAnimator.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
                override fun onAnimationUpdate(animation: ValueAnimator) {
                    try {
                        val v: Float = animation.getAnimatedFraction()
                        val newPosition: LatLng =
                            latLngInterpolator.interpolate(v, startPosition, endPosition)!!
                        marker.setPosition(newPosition)
                        marker.rotation =
                            computeRotation(v, startRotation, destination.getBearing())
                    } catch (ex: java.lang.Exception) {
                        // I don't care atm..
                    }
                }
            })
            valueAnimator.start()
        }
    }

    private fun computeRotation(fraction: Float, start: Float, end: Float): Float {
        val normalizeEnd = end - start // rotate start to 0
        val normalizedEndAbs = (normalizeEnd + 360) % 360
        val direction: Float =
            if (normalizedEndAbs > 180) -1f else 1.toFloat() // -1 = anticlockwise, 1 = clockwise
        val rotation: Float
        rotation = if (direction > 0) {
            normalizedEndAbs
        } else {
            normalizedEndAbs - 360
        }
        val result = fraction * rotation + start
        return (result + 360) % 360
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    protected fun createMarker(
        latitude: Double,
        longitude: Double,
        title: String?,
        snippet: String?,
        iconResID: Int
    ): Marker? {
        return map!!.addMarker(
            MarkerOptions()
                .position(LatLng(latitude, longitude))
                .anchor(0.5f, 0.5f)
                .title(title)
                .icon(getMarkerIconFromDrawable(getDrawable(iconResID)!!))
        )
    }


    override fun onStart() {
        super.onStart()
        mapView!!.onStart()
    }


    override fun onResume() {
        super.onResume()
        mapView!!.onResume()
        LocaleManager().setLocale(this)
    }

    override fun onStop() {
        super.onStop()
        mapView!!.onStop()
    }

    override fun onPause() {
        mapView!!.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mapView!!.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView!!.onLowMemory()
    }


}