package com.shuttleclone.driver.ui.Activity

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.budiyev.android.codescanner.*
import com.bumptech.glide.Glide
import com.shuttleclone.driver.ui.Adapters.BusStopsAdapter
import com.shuttleclone.driver.Model.ScanTicketResponseModel
import com.shuttleclone.driver.Util.*
import com.shuttleclone.driver.ViewModel.MainViewModel
import com.shuttleclone.driver.events.UpdateBookingStatusEvents
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.ncorti.slidetoact.SlideToActView
import com.nhaarman.supertooltips.ToolTip
import com.nhaarman.supertooltips.ToolTipRelativeLayout
import com.nhaarman.supertooltips.ToolTipView
import com.shuttleclone.driver.Model.RoutesItem
import org.greenrobot.eventbus.EventBus
import com.shuttleclone.driver.R

class ActiveRideDetailsActivity : BaseActivity(), ToolTipView.OnToolTipViewClickedListener {

    val TAG = "ActiveRideDetails"
    val REQUEST_CAMER_CODE = 210
    var rvBusStops: RecyclerView? = null
    var btnStartRide: SlideToActView? = null
    var btnNavigateRide: Button? = null
    var btnFinishRide: SlideToActView? = null
    var busStopsAdapter: BusStopsAdapter? = null
    var ivBack: ImageView? = null
    var ivNotification: ImageView? = null
    var fabScanTicket: FloatingActionButton? = null
    lateinit var scanTicketLayout: ToolTipRelativeLayout
    lateinit var scanTicketView: ToolTipView
    lateinit var tvTripStatus: TextView
    lateinit var tvTotalSeats: TextView
    lateinit var tvPassengers: TextView
    lateinit var tvCurrentTime: TextView
    lateinit var tvStartAt: TextView
    lateinit var tvEndAt: TextView
    var tripsData: RoutesItem? = null
    private val mainViewModel: MainViewModel by viewModels()
    private var mDriverOfflineDialog: Dialog? = null
    private var mPassangerDetailsDialog: Dialog? = null

    var totalPassengers = ""
    var totalSeats = ""
    var dateTime = ""
    var routeId = ""
    var assignId = ""
    var tripStatus = ""

    val TRIP_STARTED = "STARTED"
    val TRIP_COMPLETED = "COMPLETED"
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onResume() {
        super.onResume()
        LocaleManager().setLocale(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocaleManager().setLocale(this)
        setContentView(R.layout.activity_active_ride_details)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        try {
            if (intent != null) {
//                tripsData =  intent.getSerializable("tripsData", RoutesItem::class.java)
                tripsData = intent.getSerializableExtra("tripsData") as RoutesItem
                assignId = intent.getStringExtra("assignId").toString()
                dateTime = intent.getStringExtra("dateTime").toString()
                tripStatus = intent.getStringExtra("tripStatus").toString()
                /* totalPassengers = intent.getStringExtra("totalPassengers").toString()
                 totalSeats = intent.getStringExtra("totalSeats").toString()
                 dateTime = intent.getStringExtra("dateTime").toString()
                 routeId = intent.getStringExtra("routeId").toString()*/
            }
        } catch (e: Exception) {
            myLog(TAG, "onCreate: Error=${e.localizedMessage}")
        }


        initLayouts()
        setListners()
        doOperationOnLayouts()


    }

    private fun setListners() {

        updateView(tripStatus)

        btnStartRide!!.onSlideCompleteListener = (object : SlideToActView.OnSlideCompleteListener {
            override fun onSlideComplete(@NonNull view: SlideToActView) {
                if (getPreference(
                        this@ActiveRideDetailsActivity,
                        AppConstants.DRIVER_STATUS
                    ).equals(AppConstants.Driver_Offline)
                )
                    driverOfflineAlertDialog(getString(R.string.driver_offline_trip_start_alert))
                else {

                        if (!isPreference(this@ActiveRideDetailsActivity, AppConstants.IS_TRIP_STARTED)
                            && getPreference(this@ActiveRideDetailsActivity,AppConstants.ASSIGNED_ID).equals("")) {

                           /* Log.i(TAG, "onSlideComplete: data=${tripsData!!.time}")
                            Log.i(TAG, "onSlideComplete: ISdata=${isTimeValidToStart(tripsData!!.time.toString())}")
                            btnStartRide!!.resetSlider()*/

                            if (isTimeValidToStart(tripsData!!.time.toString())) {
                                getLatestLocation()
                                vibratePhone(this@ActiveRideDetailsActivity)
                                updateBookingStatus(TRIP_STARTED)
                            }
                            else {
                                btnStartRide!!.resetSlider()
                                alertDialog(this@ActiveRideDetailsActivity, getString(R.string.you_cant_start_this_trip_before_time))
                            }

                        } else{
                            btnStartRide!!.resetSlider()
                            alertDialog(  this@ActiveRideDetailsActivity, getString(R.string.you_cant_start_this_trip))
                        }
                }
            }
        })

        btnNavigateRide!!.clickWithThrottle {
            vibratePhone(this@ActiveRideDetailsActivity)
            navigateRide()
        }


        btnFinishRide!!.onSlideCompleteListener = (object : SlideToActView.OnSlideCompleteListener {
            override fun onSlideComplete(@NonNull view: SlideToActView) {
                if (getPreference(
                        this@ActiveRideDetailsActivity,
                        AppConstants.DRIVER_STATUS
                    ).equals(AppConstants.Driver_Offline)
                )
                    driverOfflineAlertDialog(getString(R.string.driver_offline_trip_end_alert))
                else {
                    getLatestLocation()
                    vibratePhone(this@ActiveRideDetailsActivity)
                    updateBookingStatus(TRIP_COMPLETED)
                }
            }
        })


        ivNotification!!.setOnClickListener { startActivity(NotificationActivity::class.java) }
        ivBack!!.setOnClickListener { finish() }
        fabScanTicket!!.setOnClickListener {
            if (checkAndRequestCameraPermissions()) scanTicket()
        }
    }

    private fun driverOfflineAlertDialog(alert: String) {
        try {
            if (mDriverOfflineDialog == null)
                mDriverOfflineDialog = Dialog(this)

            mDriverOfflineDialog!!.setContentView(R.layout.alert_dailog_layout)
            mDriverOfflineDialog!!.setCancelable(true)
            mDriverOfflineDialog!!.window?.setBackgroundDrawable(ColorDrawable(0))

            mDriverOfflineDialog!!.findViewById<TextView>(R.id.tvMsg).text = alert

            mDriverOfflineDialog!!.findViewById<View>(R.id.btnOkay).setOnClickListener {
                btnStartRide!!.resetSlider()
                if (mDriverOfflineDialog!!.isShowing) mDriverOfflineDialog!!.dismiss()

                finish()
            }
            mDriverOfflineDialog!!.setCancelable(false)
            mDriverOfflineDialog!!.setCanceledOnTouchOutside(false)

            if (!mDriverOfflineDialog!!.isShowing)
                mDriverOfflineDialog!!.show()

        } catch (e: java.lang.Exception) {
            myLog(TAG, "driverOfflineAlertDialog: Error=${e.localizedMessage}")
        }
    }


    private fun getLatestLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                fusedLocationClient?.lastLocation!!.addOnSuccessListener { location: Location? ->
                    location?.let { it: Location ->
                        savePreference(
                            this,
                            AppConstants.DRIVER_LATITUDE,
                            location.latitude.toString()
                        )
                        savePreference(
                            this,
                            AppConstants.DRIVER_LONGITUDE,
                            location.longitude.toString()
                        )
                        myLog(TAG, "setListners: location=${it.latitude}+${it.longitude}")
                    } ?: kotlin.run {
                        myLog(TAG, "setListners: fusedLocationClient error")
                    }
                }
            } catch (e: Exception) {
                myLog(TAG, "getLatestLocation: Error=${e.localizedMessage}")
            }

        }
    }

    private fun updateView(tripStatus: String?) {
        try {

            savePreference(this, AppConstants.TRIP_STATUS, tripStatus)

            myLog(TAG, "updateView: tripStatus=$tripStatus")
            when (tripStatus) {
                "ASSIGNED" -> {
                    savePreference(this, AppConstants.IS_TRIP_STARTED, false)
                    showView(btnStartRide!!)
                }

                "STARTED", "RIDING" -> {

                    showView(btnFinishRide!!)
                    showView(btnNavigateRide!!)
                    hideView(btnStartRide!!)
                    savePreference(this, AppConstants.IS_TRIP_STARTED, true)
                    savePreference(this, AppConstants.DRIVER_STATUS, AppConstants.Driver_Tracking)

                    stopDriverLocationService(this)
                    startTripTrackingLocationService(this)
                }

                else -> {
                    hideView(btnFinishRide!!)
                    hideView(btnNavigateRide!!)
                    hideView(btnStartRide!!)
                    savePreference(this, AppConstants.IS_TRIP_STARTED, false)

                    if (tripStatus!!.equals(TRIP_COMPLETED))
                        savePreference(this, AppConstants.DRIVER_STATUS, AppConstants.Driver_Online)

                    when (getPreference(this, AppConstants.DRIVER_STATUS)) {
                        AppConstants.Driver_Online, AppConstants.Driver_Tracking -> {
                            stopTripTrackingLocationService(this)
                            startDriverLocationService(this)
                        }

                        AppConstants.Driver_Offline -> {
                            stopTripTrackingLocationService(this)
                            stopDriverLocationService(this)
                        }
                    }

                    finish()
                }
            }
            tvTripStatus.text = tripStatus
        } catch (e: Exception) {
            myLog(TAG, "updateView: Error=${e.localizedMessage}")
        }
    }

    private fun navigateRide() {
        try {
            val intent = Intent(this, BusRoutesActivity::class.java)
            val bundle = Bundle()
            bundle.putSerializable("tripsData", tripsData)
            intent.putExtras(bundle)
            startActivity(intent)
        } catch (e: Exception) {
            myLog(TAG, "navigateRide: Error=${e.localizedMessage}")
        }
    }

    private fun updateBookingStatus(tripStatus: String) {
        if (isInternetConnection(this)) {
            LoadingDialog.showLoadingDialog(this, getString(R.string.pls_wait_loading))

            mainViewModel!!.updateTrackingStatus(
                getPreference(this, AppConstants.TOKEN).toString(),
                assignId,
                tripStatus,
                getPreference(this, AppConstants.DRIVER_LATITUDE).toString(),
                getPreference(this, AppConstants.DRIVER_LONGITUDE).toString(),
                getPreference(this, AppConstants.DRIVER_ANGLE).toString()
            ).observe(this, Observer {
                LoadingDialog.cancelLoading()
                try {
                    if (it == null) {
                        sessionExpireDialog(this)
                        return@Observer
                    }

                    if (null != it.errorResponse) {
                        alertDialog(this, it.errorResponse.message.toString())
                        return@Observer
                    }

                    if (it.status!!) {

                        if (tripStatus.equals(TRIP_STARTED))
                            savePreference(this, AppConstants.ASSIGNED_ID, assignId)
                        else if (tripStatus.equals(TRIP_COMPLETED)){
                            savePreference(this, AppConstants.ASSIGNED_ID, "")
                            savePreference(this, AppConstants.IS_TRIP_STARTED, false)
                            savePreference(this, AppConstants.IS_TRIP_STARTED, false)
                        }

                        updateView(it.data!!.tripStatus)
                        EventBus.getDefault().post(UpdateBookingStatusEvents())
                    } else {
                        savePreference(this, AppConstants.IS_TRIP_STARTED, false)
                        alertDialog(this, it.message.toString())
                    }

                } catch (e: Exception) {
                    myLog(TAG, "startRide: Error=${e.localizedMessage}")
                    alertDialog(this, e.localizedMessage.toString())
                }
            })
        } else toast(this)

    }

    /* init layout */
    private fun initLayouts() {
        rvBusStops = findViewById(R.id.rvBusStops)
        ivBack = findViewById(R.id.ivBack)
        btnStartRide = findViewById(R.id.btnStartRide)
        btnFinishRide = findViewById(R.id.btnFinishRide)
        ivNotification = findViewById(R.id.ivNotification)
        scanTicketLayout = findViewById(R.id.tooltipView)
        fabScanTicket = findViewById(R.id.fabScanTicket)

        tvTripStatus = findViewById(R.id.tvTripStatus)
        tvTotalSeats = findViewById(R.id.tvTotalSeats)
        tvPassengers = findViewById(R.id.tvPassengers)
        tvCurrentTime = findViewById(R.id.tvCurrentTime)
        tvStartAt = findViewById(R.id.tvStartAt)
        tvEndAt = findViewById(R.id.tvEndAt)
        btnNavigateRide = findViewById(R.id.btnNavigateRide)
    }

    /* add functionality to layout */
    private fun doOperationOnLayouts() {

        if (null != tripsData) {

            tvTotalSeats.text = tripsData!!.bookingsInfo!!.totalSeats.toString()
//            tvTripStatus.text = tripsData!!.tripStatus
            tvPassengers.text = tripsData!!.bookingsInfo!!.totalPassengers.toString()
            tvCurrentTime.text = currentTime()
//            tvStartAt.text = tripsData!!.ticketStartAt
            tvStartAt.text = dateTime
//            tvEndAt.text = tripsData!!.ticketEndAt

            busStopsAdapter = BusStopsAdapter(
                baseContext,
                this,
                tripsData!!.stops,
                tripsData!!.busScheduleId,
                tripsData!!.bookingDate
            )
            rvBusStops!!.apply {
                layoutManager = LinearLayoutManager(this@ActiveRideDetailsActivity)
                setHasFixedSize(true)
                adapter = busStopsAdapter
            }
            RunLayoutAnimation(this, rvBusStops!!)

        }

        Handler().postDelayed({
            scanTicketToolTipView()
        }, 800)
    }

    fun passengerDetailsDialog(
        ticketData: ScanTicketResponseModel?,
        codeScanner: CodeScanner,
        scanTicketDialog: Dialog
    ) {
        try {
            if (mPassangerDetailsDialog == null)
                mPassangerDetailsDialog = Dialog(this)

            mPassangerDetailsDialog?.let { pDailog ->

                pDailog.setContentView(R.layout.ticket_scan_passanger_dtls_dialog)
                pDailog.setCancelable(true)
                pDailog.window?.setBackgroundDrawable(ColorDrawable(0))

                val btnOnBord = pDailog.findViewById<View>(R.id.btnOnBord)
                val btnCancel = pDailog.findViewById<View>(R.id.btnCancel)

                ticketData?.let {
                    pDailog.findViewById<TextView>(R.id.tvAmount).text = it.finalTotalFare
                    pDailog.findViewById<TextView>(R.id.tvPaymentMode).text =
                        "${getString(R.string.mode)} ${it.paymentMethod}"
                    pDailog.findViewById<TextView>(R.id.tvDate).text =
                        "${getString(R.string.date)} ${it.busDepatureDate}"
                    pDailog.findViewById<TextView>(R.id.tvBusNo).text = it.busModelNo
                    pDailog.findViewById<TextView>(R.id.tvBusName).text = it.busName
                    pDailog.findViewById<TextView>(R.id.tvSeatNo).text = it.seatNos.toString()
                    pDailog.findViewById<TextView>(R.id.tvPassengersNo).text = it.passengers
                    pDailog.findViewById<TextView>(R.id.tvPNRNo).text = it.pnrNo

                    Glide.with(this).load(it.profilePicture).placeholder(R.drawable.ic_bus)
                        .into(pDailog.findViewById<ImageView>(R.id.ivUserImg))

                    codeScanner.stopPreview()
                    codeScanner.releaseResources()
                    scanTicketDialog.dismiss()

                } ?: {
                    if (!pDailog.isShowing)
                        pDailog.show()

                    alertDialog(this, getString(R.string.something_wrong))
                }


                btnOnBord.setOnClickListener {
                    if (pDailog.isShowing) pDailog.dismiss()

                    ticketData?.let {
                        if (!ticketData!!.pnrNo.equals(""))
                            onboardPassenger(
                                ticketData!!.pnrNo.toString(),
                                codeScanner,
                                scanTicketDialog
                            )
                        else {
                            if (!pDailog.isShowing)
                                pDailog.show()

                            alertDialog(this, getString(R.string.something_wrong))
                        }
                    }
                }
                btnCancel.setOnClickListener {
                    if (pDailog.isShowing) pDailog.dismiss()
                }

                pDailog.setCancelable(false)
                pDailog.setCanceledOnTouchOutside(false)

                if (!pDailog.isShowing)
                    pDailog.show()

            } ?: alertDialog(this, getString(R.string.something_wrong))

        } catch (e: java.lang.Exception) {
            myLog(TAG, "passengerDetailsDialog: Error=${e.localizedMessage}")
        }
    }


    private fun scanTicket() {
        try {
            val view: View = layoutInflater.inflate(R.layout.scan_ticket_dialog, null)
            val scanTicketDialog = Dialog(this, R.style.CustomBottomSheetDialogTheme)

            val scannerView = view.findViewById<CodeScannerView>(R.id.scanner_view)
            val btnCloseScanner = view.findViewById<Button>(R.id.btnCloseScanner)

            val codeScanner = CodeScanner(this, scannerView)

            // Parameters (default values)
            codeScanner.camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
            codeScanner.formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat,
            // ex. listOf(BarcodeFormat.QR_CODE)
            codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
            codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
            codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
            codeScanner.isFlashEnabled = false // Whether to enable flash or not
            codeScanner.startPreview()

            // Callbacks
            codeScanner.decodeCallback = DecodeCallback {
                runOnUiThread {
                    myLog(TAG, "onCreate: Scan ticket response=${it.text}")
                    val ticketData = Gson().fromJson(it.text, ScanTicketResponseModel::class.java)

                    passengerDetailsDialog(ticketData, codeScanner, scanTicketDialog)
                }
            }
            codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
                runOnUiThread {
                    toast(this, "Camera initialization error: ${it.message}")
                }
            }

            scannerView.setOnClickListener {
                codeScanner.startPreview()
            }

            btnCloseScanner.setOnClickListener {
                if (scanTicketDialog != null)
                    scanTicketDialog?.dismiss()
                codeScanner.releaseResources()
            }


            scanTicketDialog?.setContentView(view)
            scanTicketDialog?.setCancelable(false)
            scanTicketDialog?.setCanceledOnTouchOutside(false)
            scanTicketDialog?.show()

        } catch (e: Exception) {
            myLog(TAG, "showscanTicketDialog: Error=" + e.localizedMessage)
        }
    }

    private fun onboardPassenger(
        pnrNo: String,
        codeScanner: CodeScanner,
        scanTicketDialog: Dialog
    ) {
        try {
            if (isInternetConnection(this!!)) {
                LoadingDialog.showLoadingDialog(this!!, getString(R.string.pls_wait_loading))
                mainViewModel!!.updateTicketStatus(
                    getPreference(this!!, AppConstants.TOKEN)!!.toString(),
                    pnrNo!!,
                    "ONBOARDED"
                ).observe(this, Observer {
                    LoadingDialog.cancelLoading()

                    vibratePhone(this)


                    if (it == null) {
                        sessionExpireDialog(this)
                        return@Observer
                    }

                    if (it.isStatus) {
                        toast(this, it.message)
                    } else alertDialog(this, it.message.toString())
                })

            } else toast(this)

        } catch (e: Exception) {
            myLog(TAG, e.localizedMessage)
            LoadingDialog.cancelLoading()
            codeScanner.stopPreview()
            codeScanner.releaseResources()
            scanTicketDialog.dismiss()
            alertDialog(this, e.localizedMessage.toString())
        }

    }


    fun checkAndRequestCameraPermissions(): Boolean {
        val camerapermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val listPermissionsNeeded = ArrayList<String>()
        if (camerapermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA)
        }
        if (!listPermissionsNeeded.isEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                    listPermissionsNeeded.toTypedArray(),
                    REQUEST_CAMER_CODE
                )
            } else {
                try {
                    ActivityCompat.requestPermissions(
                        this as Activity,
                        listPermissionsNeeded.toTypedArray(),
                        REQUEST_CAMER_CODE
                    )
                } catch (e: Exception) {
                    myLog(TAG, "checkAndRequestCameraPermissions: Error=" + e.localizedMessage)
                }

            }
            return false
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CAMER_CODE -> {
                scanTicket()
            }
        }
    }


    private fun scanTicketToolTipView() {
        val toolTip: ToolTip = ToolTip()
            .withText("Scan Ticket")
            .withTextColor(resources.getColor(R.color.white))
            .withColor(resources.getColor(R.color.colorAccent))
            .withAnimationType(ToolTip.AnimationType.FROM_TOP)
        scanTicketView = scanTicketLayout.showToolTipForView(toolTip, fabScanTicket)
        scanTicketView.setOnToolTipViewClickedListener(this)
    }

    override fun onToolTipViewClicked(toolTipView: ToolTipView?) {
        if (scanTicketView == toolTipView) {
            scanTicketView.remove()
        }
    }
}