package com.shuttleclone.driver.ui.Activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shuttleclone.driver.ui.Adapters.PassengerListAdapter
import com.shuttleclone.driver.Model.PassengerDataItem
import com.shuttleclone.driver.Model.StopsItem
import com.shuttleclone.driver.R
import com.shuttleclone.driver.Util.AppConstants
import com.shuttleclone.driver.Util.LoadingDialog
import com.shuttleclone.driver.Util.LocaleManager
import com.shuttleclone.driver.Util.RunLayoutAnimation
import com.shuttleclone.driver.Util.alertDialog
import com.shuttleclone.driver.Util.getPreference
import com.shuttleclone.driver.Util.isInternetConnection
import com.shuttleclone.driver.Util.myLog
import com.shuttleclone.driver.Util.sessionExpireDialog
import com.shuttleclone.driver.Util.toast
import com.shuttleclone.driver.ViewModel.MainViewModel


class StopsPassengerListActivity : BaseActivity() {

    val TAG = "StopsPassengerList"
    var rvPassengerList: RecyclerView? = null
    var passengerListAdapter: PassengerListAdapter? = null
    var ivBack: ImageView? = null
    var ivNotification: ImageView? = null
    var tvStopName: TextView? = null
    var tvStopTiming: TextView? = null
    var tvNoOfPassenger: TextView? = null
    var tvNoOfOutPassenger: TextView? = null
    var layNoPassengerAvailable: LinearLayout? = null
    private val mainViewModel: MainViewModel by viewModels()
    var type = ""
    var stopId = ""
    var routeId = ""
    var bookingDate = ""
    var stopName = ""
    var stopTime = ""
    var passengersIn = ""
    var passengersOut = ""
    var bookings = ArrayList<String>()
    var stopsData: StopsItem? = null

    override fun onResume() {
        super.onResume()
        LocaleManager().setLocale(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocaleManager().setLocale(this)
        setContentView(R.layout.activity_stopes_passenger_list)

        try {
            if (intent != null) {
                stopsData = intent.getSerializableExtra("stopsData") as StopsItem
                routeId = intent.getStringExtra("routeId").toString()
                stopTime = intent.getStringExtra("stopTime").toString()
                bookingDate = intent.getStringExtra("bookingDate").toString()
                type = intent.getStringExtra("type").toString()

                if (null != stopsData) {
                    stopId = stopsData!!.id.toString()
                    stopName = stopsData!!.name.toString()
                    passengersIn = stopsData!!.pickupCount.toString()
                    passengersOut = stopsData!!.dropCount.toString()

                    if (type.equals("pickup"))
                        bookings = stopsData!!.pickupBookings!!
                    else bookings = stopsData!!.dropBookings!!
                }
                Log.i(TAG, "onCreate: bookings=$bookings")
            }
        } catch (e: Exception) {
            myLog(TAG, "onCreate: Error=${e.localizedMessage}")
        }

        initLayouts()
        handelViewModel()
        doOperationOnLayouts()

    }

    /* init layout */
    private fun initLayouts() {
        rvPassengerList = findViewById(R.id.rvPassengerList)
        ivBack = findViewById(R.id.ivBack)
        ivNotification = findViewById(R.id.ivNotification)
        layNoPassengerAvailable = findViewById(R.id.layNoPassengerAvailable)

        tvStopName = findViewById(R.id.tvStopName)
        tvStopTiming = findViewById(R.id.tvStopTiming)
        tvNoOfPassenger = findViewById(R.id.tvNoOfPassenger)
        tvNoOfOutPassenger = findViewById(R.id.tvNoOfOutPassenger)
    }

    /* add functionality to layout */
    private fun doOperationOnLayouts() {
        try {
            tvStopName!!.text = stopName
            tvStopTiming!!.text = stopTime
            tvNoOfPassenger!!.text = passengersIn
            tvNoOfOutPassenger!!.text = passengersOut

            ivBack!!.setOnClickListener { finish() }
            ivNotification!!.setOnClickListener { startActivity(NotificationActivity::class.java) }
        } catch (e: Exception) {
            myLog(TAG, "doOperationOnLayouts: Error=${e.localizedMessage}")
        }

    }

    private fun handelViewModel() {
        if (isInternetConnection(this)) {
            LoadingDialog.showLoadingDialog(this, getString(R.string.pls_wait_loading))
            mainViewModel!!.getPassengersDetails(
                getPreference(this, AppConstants.TOKEN)!!,
                stopId,
                routeId,
                bookingDate,
                bookings
            ).observe(this, androidx.lifecycle.Observer {
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

                    if (it.status!!)
                        setDataToAdapter(it!!.data)
                    else {
                        showView(layNoPassengerAvailable!!)
                        hideView(rvPassengerList!!)
                        if (!it.message.equals("booking ids not found"))
                            alertDialog(this, it.message.toString())
                    }

                } catch (e: Exception) {
                    alertDialog(this, e.localizedMessage.toString())
                }
            })
        } else toast(this)
    }

    // UPDATED FUNCTION: Correctly initializes the adapter with a click lambda
    private fun setDataToAdapter(data: List<PassengerDataItem>?) {
        if (data.isNullOrEmpty()) {
            showView(layNoPassengerAvailable!!)
            hideView(rvPassengerList!!)
            return
        }

        // USE 'this@StopsPassengerListActivity' NOT 'baseContext'
        passengerListAdapter = PassengerListAdapter(this@StopsPassengerListActivity, data) { phoneNumber ->
            myLog(TAG, "Initiating call to: $phoneNumber") // Check your Logcat to see if this prints
            makePhoneCall(phoneNumber)
        }

        rvPassengerList?.apply {
            layoutManager = LinearLayoutManager(this@StopsPassengerListActivity)
            setHasFixedSize(true)
            adapter = passengerListAdapter
        }
        RunLayoutAnimation(this, rvPassengerList!!)
    }

    // NEW FUNCTION: Handles the actual dialing of the phone number
    private fun makePhoneCall(number: String) {
        try {
            if (number.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:$number")
                startActivity(intent)
            } else {
                toast(this, "Phone number not available")
            }
        } catch (e: Exception) {
            myLog(TAG, "makePhoneCall: Error=${e.localizedMessage}")
        }
    }
}