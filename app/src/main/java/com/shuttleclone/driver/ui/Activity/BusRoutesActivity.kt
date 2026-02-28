package com.shuttleclone.driver.ui.Activity

import android.animation.ValueAnimator
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
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
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.maps.android.PolyUtil
import com.shuttleclone.driver.Model.RoutesItem
import com.shuttleclone.driver.Model.StopsItem
import com.shuttleclone.driver.R
import com.shuttleclone.driver.Util.AppConstants
import com.shuttleclone.driver.Util.LiveUpdate
import com.shuttleclone.driver.Util.getPreference
import com.shuttleclone.driver.Util.isPreference
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
    private var tvStopName: TextView? = null
    private var tripsData: RoutesItem? = null
    private var mapView: MapView? = null
    private var map: GoogleMap? = null
    private var rvBusRoutes: RecyclerView? = null
    private var stopsAdapter: ViewStopsAdapter? = null
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var routePoints: List<LatLng>
    private var currentMarker: Marker? = null
    private var currentPolyline: Polyline? = null
    private lateinit var tvEta: TextView

    // SMOOTH SCROLLER
    private lateinit var smoothScroller: LinearSmoothScroller

    // Track whether we already adjusted the route to the "next stop" only
    private var hasAdjustedToNextStopRoute: Boolean = false

    // Whether at least one passenger is onboarded for this trip
    private var isPassengerOnboard: Boolean = false

    // Last GPS location used to compute smooth bearing like Google Maps
    private var lastLocationLatLng: LatLng? = null

    // Replace with your actual key
    private val GOOGLE_MAPS_API_KEY = "AIzaSyAmB3N1lgruRy6NsYHNb9xGMm-_E7sf1CU"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bus_routes)

        ivBack = findViewById(R.id.ivBack)
        tvStopName = findViewById(R.id.tvStopName)
        rvBusRoutes = findViewById(R.id.rvBusRoutes)
        mapView = findViewById(R.id.mapview)
        tvEta = findViewById(R.id.tvEta)

        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottomSheet))
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        bottomSheetBehavior.peekHeight = resources.getDimensionPixelSize(R.dimen.bottom_sheet_peek_height)

        ivBack?.setOnClickListener { finish() }

        // SmoothScroller initialization
        smoothScroller = object : LinearSmoothScroller(this) {
            override fun getVerticalSnapPreference(): Int = SNAP_TO_START
        }

        // Check if we already have an onboarded passenger for this trip
        isPassengerOnboard = isPreference(this, AppConstants.IS_PASSENGER_ONBOARDED)

        if (intent != null) {
            try {
                tripsData = intent.getSerializableExtra("tripsData") as RoutesItem
            } catch (e: Exception) {
                Log.e("BusRoutesActivity", "onCreate: Error=${e.localizedMessage}")
            }
        }

        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onMapReady(gmap: GoogleMap) {
        map = gmap
        map?.uiSettings?.isMapToolbarEnabled = false
        map?.uiSettings?.isZoomControlsEnabled = false
        // Keep navigation orientation always forward-facing like Google Maps
        map?.uiSettings?.isRotateGesturesEnabled = false
        map?.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
        if (tripsData?.stops?.isNotEmpty() == true) {
            // If passenger is already onboarded, directly show navigation to next stop only;
            // otherwise show full route.
            if (isPassengerOnboard) {
                val latStr = getPreference(this, AppConstants.DRIVER_LATITUDE)?.toString()
                val lngStr = getPreference(this, AppConstants.DRIVER_LONGITUDE)?.toString()
                if (!latStr.isNullOrBlank() && !lngStr.isNullOrBlank()) {
                    try {
                        val currentLat = latStr.toDouble()
                        val currentLng = lngStr.toDouble()
                        val currentLatLng = LatLng(currentLat, currentLng)
                        fetchAndDrawRouteToNextStop(currentLatLng)
                    } catch (e: Exception) {
                        // Fallback to full route if parsing fails
                        fetchAndDrawRoute()
                    }
                } else {
                    // No stored driver location, fallback to full route
                    fetchAndDrawRoute()
                }
            } else {
                fetchAndDrawRoute()
            }
        }
    }

    private fun fetchAndDrawRoute() {
        val stops = tripsData?.stops ?: return
        stopsAdapter = ViewStopsAdapter(this@BusRoutesActivity, stops)
        rvBusRoutes?.apply {
            layoutManager = LinearLayoutManager(this@BusRoutesActivity, RecyclerView.VERTICAL, false)
            adapter = stopsAdapter
            setHasFixedSize(true)
        }

        // Polyline route setup
        val stopCoords = stops.map { "${it.lat},${it.lng}" }
        val origin = stopCoords.first()
        val destination = stopCoords.last()
        val waypoints = if (stopCoords.size > 2) stopCoords.drop(1).dropLast(1).joinToString("|") else ""

        val url = "https://maps.googleapis.com/maps/api/directions/json" +
                "?origin=$origin" +
                "&destination=$destination" +
                (if (waypoints.isNotEmpty()) "&waypoints=optimize:false|$waypoints" else "") +
                "&mode=driving" +
                // Use traffic-aware routing
                "&departure_time=now" +
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
                        val routeObj = routesArray.getJSONObject(0)
                        val overviewPolyline = routeObj.getJSONObject("overview_polyline").getString("points")
                        val legs = routeObj.getJSONArray("legs")
                        var totalDurationSeconds = 0
                        var totalTrafficSeconds = 0
                        for (i in 0 until legs.length()) {
                            val leg = legs.getJSONObject(i)
                            val duration = leg.getJSONObject("duration")
                            totalDurationSeconds += duration.getInt("value")

                            // Prefer traffic-adjusted duration when available
                            val durationInTraffic =
                                leg.optJSONObject("duration_in_traffic")?.optInt("value")
                            totalTrafficSeconds += durationInTraffic ?: duration.getInt("value")
                        }
                        val totalDurationMinutes = totalDurationSeconds / 60
                        val etaText = "ETA: $totalDurationMinutes mins"

                        val trafficRatio =
                            if (totalDurationSeconds > 0) totalTrafficSeconds.toFloat() / totalDurationSeconds.toFloat()
                            else 1f
                        val trafficColor = getTrafficColor(trafficRatio)

                        val routePointsDecoded: List<LatLng> = PolyUtil.decode(overviewPolyline)
                        withContext(Dispatchers.Main) {
                            if (routePointsDecoded.isNotEmpty()) {
                                routePoints = routePointsDecoded
                                drawRouteOnMap(routePointsDecoded, true, trafficColor)
                                tvEta.text = etaText
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("BusRoutesActivity", "fetchAndDrawRoute: Error=${e.localizedMessage}")
            }
        }
    }

    private fun fetchAndDrawRouteFrom(currentLatLng: LatLng) {
        val stops = tripsData?.stops ?: return
        val destination = "${stops.last().lat},${stops.last().lng}"
        val waypoints = if (stops.size > 2) {
            stops.drop(1).dropLast(1).joinToString("|") { "${it.lat},${it.lng}" }
        } else {
            ""
        }
        val url = "https://maps.googleapis.com/maps/api/directions/json" +
                "?origin=${currentLatLng.latitude},${currentLatLng.longitude}" +
                "&destination=$destination" +
                (if (waypoints.isNotEmpty()) "&waypoints=optimize:false|$waypoints" else "") +
                "&mode=driving" +
                // Use traffic-aware routing
                "&departure_time=now" +
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
                        val routeObj = routesArray.getJSONObject(0)
                        val overviewPolyline = routeObj.getJSONObject("overview_polyline").getString("points")
                        val legs = routeObj.getJSONArray("legs")
                        var totalDurationSeconds = 0
                        var totalTrafficSeconds = 0
                        for (i in 0 until legs.length()) {
                            val leg = legs.getJSONObject(i)
                            val duration = leg.getJSONObject("duration")
                            totalDurationSeconds += duration.getInt("value")

                            val durationInTraffic =
                                leg.optJSONObject("duration_in_traffic")?.optInt("value")
                            totalTrafficSeconds += durationInTraffic ?: duration.getInt("value")
                        }
                        val totalDurationMinutes = totalDurationSeconds / 60
                        val etaText = "ETA: $totalDurationMinutes mins"

                        val trafficRatio =
                            if (totalDurationSeconds > 0) totalTrafficSeconds.toFloat() / totalDurationSeconds.toFloat()
                            else 1f
                        val trafficColor = getTrafficColor(trafficRatio)

                        val newRoutePoints: List<LatLng> = PolyUtil.decode(overviewPolyline)
                        withContext(Dispatchers.Main) {
                            if (newRoutePoints.isNotEmpty()) {
                                routePoints = newRoutePoints
                                drawRouteOnMap(newRoutePoints, false, trafficColor)
                                tvEta.text = etaText
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("BusRoutesActivity", "fetchAndDrawRouteFrom: Error=${e.localizedMessage}")
            }
        }
    }

    private fun drawRouteOnMap(
        routePointsParam: List<LatLng>,
        clearMap: Boolean = true,
        trafficColor: Int = Color.BLUE,
        polylineWidth: Float = 12f
    ) {
        if (routePointsParam.isEmpty()) return
        if (clearMap) {
            map?.clear()
            currentMarker = null
            if (currentPolyline != null) {
                currentPolyline?.remove()
                currentPolyline = null
            }
        }

        // STOP MARKERS - custom icon (change as per your drawable)
        tripsData?.stops?.forEachIndexed { index, stop ->
            val markerColor =
                when (index) {
                    0 -> BitmapDescriptorFactory.HUE_GREEN   // Start point
                    tripsData?.stops?.size?.minus(1) -> BitmapDescriptorFactory.HUE_RED // End point
                    else -> BitmapDescriptorFactory.HUE_YELLOW                  // Middle stops
                }
            if (stop?.lat != null && stop.lng != null) {
                map?.addMarker(
                    MarkerOptions()
                        .position(LatLng(stop.lat!!, stop.lng!!))
                        .title(stop.name)
                        .icon(BitmapDescriptorFactory.defaultMarker(markerColor))
                        .anchor(0.5f, 0.5f)
                )
            }
        }

        // Draw polyline for route
        if (currentPolyline != null) {
            currentPolyline?.remove()
        }
        val polylineOptions = PolylineOptions()
            .addAll(routePointsParam)
            // Thicker line for better visibility
            .width(polylineWidth.coerceAtLeast(14f))
            // Color reflects current traffic conditions
            .color(trafficColor)
        currentPolyline = map?.addPolyline(polylineOptions)

        // Camera bounds (see all markers+route)
        val builder = LatLngBounds.Builder()
        routePointsParam.forEach { builder.include(it) }
        val bounds = builder.build()
        val cu = CameraUpdateFactory.newLatLngBounds(bounds, 100)
        map?.moveCamera(cu)

        val initialLatLng = routePoints.first()
        if (currentMarker != null) {
            currentMarker?.remove()
        }
        currentMarker = map!!.addMarker(
            MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.map_bus))
                .position(routePoints.first())
                .flat(true)
                .anchor(0.5f, 0.5f)
        )!!

        LiveUpdate.updateLocation.observe(this, { location ->
            val currentLatLng = LatLng(location.latitude, location.longitude)
            if (currentMarker == null) {
                currentMarker = map?.addMarker(
                    MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.map_bus))
                        .position(currentLatLng)
                        .flat(true)
                        .anchor(0.5f, 0.5f)
                )
            } else {
                val startPosition = currentMarker?.position
                val endPosition = currentLatLng

                val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
                valueAnimator.duration = 1000 // 1 second animation
                valueAnimator.addUpdateListener { animation ->
                    val v = animation.animatedFraction
                    val lat = v * endPosition.latitude + (1 - v) * (startPosition?.latitude ?: 0.0)
                    val lng = v * endPosition.longitude + (1 - v) * (startPosition?.longitude ?: 0.0)
                    val newPos = LatLng(lat, lng)
                    currentMarker?.position = newPos
                }
                valueAnimator.start()
            }

            // Calculate bearing based on last GPS location so that navigation
            // orientation always remains forward-facing (like Google Maps)
            val previousLatLng = lastLocationLatLng ?: currentLatLng
            val bearing = getBearing(previousLatLng, currentLatLng)

            map?.animateCamera(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.Builder()
                        .target(currentLatLng)
                        .zoom(17f)
                        .tilt(45f)
                        .bearing(bearing)
                        .build()
                )
            )

            // Remember last GPS location for next bearing calculation
            lastLocationLatLng = currentLatLng

            // Once passenger is onboard, switch route to only the "next stop"
            // so that previous segments of the full route are hidden and focus stays ahead.
            if (isPassengerOnboard && !hasAdjustedToNextStopRoute) {
                hasAdjustedToNextStopRoute = true
                fetchAndDrawRouteToNextStop(currentLatLng)
            }

            // If the driver goes off the suggested path, recompute route from current position
            if (!PolyUtil.isLocationOnPath(
                    currentLatLng,
                    routePointsParam,
                    false,
                    30.0
                )
            ) {
                fetchAndDrawRouteFrom(currentLatLng)
            }
        })

        // OPTIONAL: Add logic to update the bus marker or animate
        // currentMarker = ...
    }

    // --- Smooth scroll function ---
    fun smoothScrollToStop(position: Int) {
        (rvBusRoutes?.layoutManager as? LinearLayoutManager)?.let { layoutManager ->
            smoothScroller.targetPosition = position
            layoutManager.startSmoothScroll(smoothScroller)
        }
    }

    override fun onStart() { super.onStart(); mapView?.onStart() }
    override fun onResume() { super.onResume(); mapView?.onResume() }
    override fun onStop() { super.onStop(); mapView?.onStop() }
    override fun onPause() { super.onPause(); mapView?.onPause() }
    override fun onDestroy() { super.onDestroy(); mapView?.onDestroy() }
    override fun onLowMemory() { super.onLowMemory(); mapView?.onLowMemory() }

    /**
     * Decide polyline color based on how much slower traffic is compared to normal.
     * trafficRatio ~= 1   -> Free flowing (Blue)
     * trafficRatio < 1.4  -> Moderate (Orange)
     * trafficRatio >= 1.4 -> Heavy (Red)
     */
    private fun getTrafficColor(trafficRatio: Float): Int {
        return when {
            trafficRatio < 1.1f -> Color.BLUE
            trafficRatio < 1.4f -> Color.parseColor("#FFA500") // Orange
            else -> Color.RED
        }
    }

    /**
     * Compute distance between two LatLngs in meters.
     */
    private fun distanceBetween(a: LatLng, b: LatLng): Float {
        val results = FloatArray(1)
        Location.distanceBetween(a.latitude, a.longitude, b.latitude, b.longitude, results)
        return results[0]
    }

    /**
     * Get the next logical stop based on current location.
     * We approximate "next" as the nearest stop ahead of the bus.
     */
    private fun getNextStopLatLng(currentLatLng: LatLng): LatLng? {
        val stops: List<StopsItem> = tripsData?.stops ?: return null
        var minDistance = Float.MAX_VALUE
        var nextStop: StopsItem? = null

        stops.forEach { stop ->
            val stopLat = stop.lat
            val stopLng = stop.lng
            if (stopLat != null && stopLng != null) {
                val stopLatLng = LatLng(stopLat, stopLng)
                val distance = distanceBetween(currentLatLng, stopLatLng)
                if (distance < minDistance) {
                    minDistance = distance
                    nextStop = stop
                }
            }
        }

        return if (nextStop?.lat != null && nextStop?.lng != null) {
            LatLng(nextStop!!.lat!!, nextStop!!.lng!!)
        } else null
    }

    /**
     * Fetch and draw route only from current location to the "next" stop.
     * This hides the polyline for previous stops / full route to keep focus ahead.
     */
    private fun fetchAndDrawRouteToNextStop(currentLatLng: LatLng) {
        val nextStopLatLng = getNextStopLatLng(currentLatLng) ?: return

        val url = "https://maps.googleapis.com/maps/api/directions/json" +
                "?origin=${currentLatLng.latitude},${currentLatLng.longitude}" +
                "&destination=${nextStopLatLng.latitude},${nextStopLatLng.longitude}" +
                "&mode=driving" +
                "&departure_time=now" +
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
                        val routeObj = routesArray.getJSONObject(0)
                        val overviewPolyline =
                            routeObj.getJSONObject("overview_polyline").getString("points")
                        val legs = routeObj.getJSONArray("legs")
                        var totalDurationSeconds = 0
                        var totalTrafficSeconds = 0
                        for (i in 0 until legs.length()) {
                            val leg = legs.getJSONObject(i)
                            val duration = leg.getJSONObject("duration")
                            totalDurationSeconds += duration.getInt("value")

                            val durationInTraffic =
                                leg.optJSONObject("duration_in_traffic")?.optInt("value")
                            totalTrafficSeconds += durationInTraffic ?: duration.getInt("value")
                        }
                        val totalDurationMinutes = totalDurationSeconds / 60
                        val etaText = "ETA: $totalDurationMinutes mins"

                        val trafficRatio =
                            if (totalDurationSeconds > 0) totalTrafficSeconds.toFloat() / totalDurationSeconds.toFloat()
                            else 1f
                        val trafficColor = getTrafficColor(trafficRatio)

                        val newRoutePoints: List<LatLng> = PolyUtil.decode(overviewPolyline)
                        withContext(Dispatchers.Main) {
                            if (newRoutePoints.isNotEmpty()) {
                                routePoints = newRoutePoints
                                // Clear map so that only the segment to next stop is visible
                                drawRouteOnMap(newRoutePoints, true, trafficColor)
                                tvEta.text = etaText
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("BusRoutesActivity", "fetchAndDrawRouteToNextStop: Error=${e.localizedMessage}")
            }
        }
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
}
