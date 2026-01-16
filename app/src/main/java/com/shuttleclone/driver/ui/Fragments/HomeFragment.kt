package com.shuttleclone.driver.ui.Fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shuttleclone.driver.ui.Activity.BaseActivity
import com.shuttleclone.driver.ui.Adapters.DriverAssignedTripsAdapter
import com.shuttleclone.driver.ui.Adapters.TripsAdapter
import com.shuttleclone.driver.Model.TripsDataItem
import com.shuttleclone.driver.R
import com.shuttleclone.driver.Services.BackGroundLocationService
import com.shuttleclone.driver.Util.*
import com.shuttleclone.driver.ViewModel.MainViewModel

class HomeFragment : Fragment() {

    companion object {
        var mTitle = "Home"
    }

    var TAG = "HomeFragment"
    var rvTrips: RecyclerView? = null
    var tripsAdapter: TripsAdapter? = null
    var driverAssignedTripsAdapter: DriverAssignedTripsAdapter? = null
    private var mainViewModel: MainViewModel? = null
    var mContext: Context? = null
    var layNoTripsAvailable: LinearLayout? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        mContext = this.context
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        initView(view)
        return view
    }

    override fun onResume() {
        super.onResume()
        handelViewModel()

        // 👇 हमेशा BackGroundLocationService start करें जब online
        if (getPreference(mContext, AppConstants.DRIVER_STATUS) == AppConstants.Driver_Online) {
            startBackGroundLocationService(requireActivity())
        }
    }

    private fun initView(view: View) {
        rvTrips = view.findViewById(R.id.rvTrips)
        layNoTripsAvailable = view.findViewById(R.id.layNoTripsAvailable)
    }

    private fun handelViewModel() {
        try {
            if (isInternetConnection(requireActivity())) {
                LoadingDialog.showLoadingDialog(requireActivity(), getString(R.string.pls_wait_loading))
                mainViewModel!!.myTrips(getPreference(mContext, AppConstants.TOKEN)!!)
                    .observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                        LoadingDialog.cancelLoading()
                        if (it == null) {
                            sessionExpireDialog(mContext!!)
                            return@Observer
                        }

                        if (null != it.errorResponse) {
                            alertDialog(requireContext(), it.errorResponse.message.toString())
                            return@Observer
                        }

                        if (it.status!! && null != it.data) {
                            (mContext as BaseActivity).hideView(layNoTripsAvailable!!)
                            (mContext as BaseActivity).showView(rvTrips!!)

                            setListener(it!!.data)
                            savePreference(mContext, AppConstants.IS_BOOKING_ASSIGNED, true)

                            // 👇 हमेशा BackGroundLocationService चालू रखें
                            startBackGroundLocationService(requireActivity())

                            if (isPreference(mContext, AppConstants.IS_TRIP_STARTED)) {
                                stopDriverLocationService(requireActivity())
                                startTripTrackingLocationService(requireActivity())
                            } else if (getPreference(mContext, AppConstants.DRIVER_STATUS) == AppConstants.Driver_Online) {
                                stopTripTrackingLocationService(requireActivity())
                                // startDriverLocationService को comment किया - BackGroundLocationService use करेंगे
                            }

                        } else {
                            (mContext as BaseActivity).showView(layNoTripsAvailable!!)
                            (mContext as BaseActivity).hideView(rvTrips!!)
                            savePreference(mContext, AppConstants.IS_BOOKING_ASSIGNED, false)
                            savePreference(mContext, AppConstants.ASSIGNED_ID, "")

                            // 👇 No trips भी हो तो online driver के लिए service चालू रखें
                            if (!isPreference(mContext, AppConstants.IS_TRIP_STARTED)
                                && getPreference(mContext, AppConstants.DRIVER_STATUS) == AppConstants.Driver_Online
                            ) {
                                startBackGroundLocationService(requireActivity())
                                stopTripTrackingLocationService(requireActivity())
                            } else if (getPreference(mContext, AppConstants.DRIVER_STATUS) == AppConstants.Driver_Offline) {
                                stopTripTrackingLocationService(requireActivity())
                                stopDriverLocationService(requireActivity())
                            }

                            if (it.errorResponse == null)
                                alertDialog(requireContext(), it.message.toString())
                            else alertDialog(requireContext(), it.errorResponse.message.toString())
                        }
                    })
            } else toast(mContext)
        } catch (e: Exception) {
            savePreference(mContext, AppConstants.IS_BOOKING_ASSIGNED, false)
            alertDialog(requireContext(), e.localizedMessage)
            myLog(TAG, "handelViewModel: Error=${e.localizedMessage}")
        }
    }

    private fun setListener(data: List<TripsDataItem>?) {
        driverAssignedTripsAdapter = DriverAssignedTripsAdapter(requireActivity(), this, data)
        rvTrips!!.apply {
            layoutManager = LinearLayoutManager(activity)
            setHasFixedSize(true)
            adapter = driverAssignedTripsAdapter
        }
        RunLayoutAnimation(activity, rvTrips!!)
    }

    // 👇 नया function - BackGroundLocationService start करने के लिए
    private fun startBackGroundLocationService(context: Context) {
        try {
            val intent = Intent(context, BackGroundLocationService::class.java)

            // ✅ API 26+ के लिए startForegroundService, निचे के लिए startService
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                androidx.core.content.ContextCompat.startForegroundService(context, intent)
            } else {
                context.startService(intent)
            }

            myLog(TAG, "✅ BackGroundLocationService STARTED - Live Tracking ON")
        } catch (e: Exception) {
            myLog(TAG, "❌ BackGroundLocationService start error: ${e.message}")
        }
    }


    override fun onDestroy() {
        super.onDestroy()
    }
}
