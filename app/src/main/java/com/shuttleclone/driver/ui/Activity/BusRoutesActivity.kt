package com.shuttleclone.driver.ui.Activity

import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil
import com.shuttleclone.driver.Model.RoutesItem
import com.shuttleclone.driver.R
import com.shuttleclone.driver.Util.*
import com.shuttleclone.driver.ui.Adapters.ViewStopsAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class BusRoutesActivity : AppCompatActivity(), OnMapReadyCallback {

    private var ivBack: ImageView? = null
    private var ivNotification: ImageView? = null
    private var tvStopName: TextView? = null
    private var tripsData: RoutesItem? = null
    private var mapView: MapView? = null
    private val markers: MutableList<Marker> = ArrayList()
    private var map: GoogleMap? = null
    private var rvBusRoutes: RecyclerView? = null
    private lateinit var currentMarker: Marker
    private lateinit var smoothScroller: LinearSmoothScroller
    private var stopsAdapter: ViewStopsAdapter? = null

    private val GOOGLE_MAPS_API_KEY = "AIzaSyAmB3N1lgruRy6NsYHNb9xGMm-_E7sf1CU"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocaleManager().setLocale(this)
        setContentView(R.layout.activity_bus_routes)

        if (intent != null) {
            try {
                tripsData = intent.getSerializableExtra("tripsData") as RoutesItem
            } catch (e: Exception) {
                myLog("BusRoutesActivity", "onCreate: Error=${e.localizedMessage}")
            }
        }

        initLayouts()
        doOperationOnLayouts()

        mapView = findViewById<View>(R.id.mapview) as MapView
        mapView!!.onCreate(savedInstanceState)
        mapView!!.getMapAsync(this)
    }

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
    }

    private fun doOperationOnLayouts() {
        ivBack!!.setOnClickListener { finish() }
        ivNotification!!.setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onMapReady(gmap: GoogleMap) {
        map = gmap
        map!!.uiSettings.isMapToolbarEnabled = false
        map!!.uiSettings.isZoomControlsEnabled = false
        map!!.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
        if (tripsData?.stops?.isNotEmpty() == true) {
            fetchAndDrawRoute()
        }
    }

    private fun fetchAndDrawRoute() {
        val stops = tripsData?.stops
        // Null and emptiness check for stops list before any usage
        if (stops == null || stops.isEmpty()) {
            myLog("BusRoutesActivity", "fetchAndDrawRoute: stops are null or empty.")
            return
        }

        stopsAdapter = ViewStopsAdapter(this@BusRoutesActivity, stops)
        rvBusRoutes?.apply {
            layoutManager = LinearLayoutManager(this@BusRoutesActivity, RecyclerView.VERTICAL, false)
            adapter = stopsAdapter
            setHasFixedSize(true)
        }
        RunLayoutAnimation(this, rvBusRoutes!!)

        val stopCoords = stops.map { "${it.lat},${it.lng}" }
        // These calls are now safe
        val origin = stopCoords.first()
        val destination = stopCoords.last()
        val waypoints = if (stopCoords.size > 2)
            stopCoords.drop(1).dropLast(1).joinToString("|")
        else
            ""

        val url = "https://maps.googleapis.com/maps/api/directions/json" +
                "?origin=$origin" +
                "&destination=$destination" +
                (if (waypoints.isNotEmpty()) "&waypoints=$waypoints" else "") +
                "&mode=driving" +
                "&key=$GOOGLE_MAPS_API_KEY"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    val json = JSONObject(body)
                    val routesArray = json.getJSONArray("routes")
                    if (routesArray.length() > 0) {
                        val overviewPolyline = routesArray.getJSONObject(0)
                            .getJSONObject("overview_polyline").getString("points")
                        val routePoints: List<LatLng> = PolyUtil.decode(overviewPolyline)
                        withContext(Dispatchers.Main) {
                            drawRouteOnMap(routePoints)
                        }
                    }
                }
            } catch (e: Exception) {
                myLog("BusRoutesActivity", "fetchAndDrawRoute: Error=${e.localizedMessage}")
            }
        }
    }


    private fun drawRouteOnMap(routePoints: List<LatLng>) {
        if (routePoints.isEmpty()) return
        map?.clear()
        markers.clear()

        // Add bus stop markers at your backend stops
        tripsData?.stops?.forEach {
            if (it?.lat != null && it.lng != null) {
                val marker = map!!.addMarker(
                    MarkerOptions()
                        .position(LatLng(it.lat!!, it.lng!!))
                        .title(it.name)
                        .icon(getMarkerIconFromDrawable(getDrawable(R.drawable.bus_stop_pin)!!))
                        .anchor(0.5f, 0.5f)
                )
                markers.add(marker!!)
            }
        }

        // Draw route polyline on map
        val polylineOptions = PolylineOptions()
            .addAll(routePoints)
            .width(5f)
            .color(Color.RED)
        map!!.addPolyline(polylineOptions)

        // Move camera to show all points at once
        val builder = LatLngBounds.Builder()
        routePoints.forEach { builder.include(it) }
        val bounds = builder.build()
        val cu = CameraUpdateFactory.newLatLngBounds(bounds, 100)
        map!!.moveCamera(cu)

        // Place bus marker exactly at first backend stop (NOT polyline start if they ever differ)
        val firstBackendStop = tripsData!!.stops!!.first()
        val initialLatLng = LatLng(firstBackendStop.lat!!, firstBackendStop.lng!!)
        currentMarker = map!!.addMarker(
            MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.map_bus))
                .position(initialLatLng)
                .flat(true)
                .anchor(0.5f, 0.5f)
        )!!

        // Animate the bus marker smoothly along the route (if you want animation)
        //animateBusAlongRoute(routePoints, currentMarker, duration = 20000L)
        LiveUpdate.updateLocation.observe(this, androidx.lifecycle.Observer { location ->
            val currentLatLng = LatLng(location.latitude, location.longitude)
            currentMarker.position = currentLatLng
            // Optionally move camera to current bus location
            map?.animateCamera(CameraUpdateFactory.newLatLng(currentLatLng))
        })

    }

    private fun animateBusAlongRoute(routePoints: List<LatLng>, marker: Marker, duration: Long = 15000L) {
        if (routePoints.size < 2) return
        val valueAnimator = ValueAnimator.ofInt(0, routePoints.size - 1)
        valueAnimator.duration = duration
        valueAnimator.interpolator = LinearInterpolator()
        valueAnimator.addUpdateListener { animation ->
            val pointIndex = animation.animatedValue as Int
            marker.position = routePoints[pointIndex]
            if (pointIndex + 1 < routePoints.size) {
                marker.rotation = getBearing(routePoints[pointIndex], routePoints[pointIndex + 1])
            }
        }
        valueAnimator.start()
    }

    private fun getBearing(from: LatLng, to: LatLng): Float {
        val lat1 = Math.toRadians(from.latitude)
        val lon1 = Math.toRadians(from.longitude)
        val lat2 = Math.toRadians(to.latitude)
        val lon2 = Math.toRadians(to.longitude)
        val dLon = lon2 - lon1
        val y = Math.sin(dLon) * Math.cos(lat2)
        val x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon)
        return ((Math.toDegrees(Math.atan2(y, x)) + 360) % 360).toFloat()
    }

    // Standard lifecycle calls to manage MapView
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
