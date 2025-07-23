package com.shuttleclone.driver.ui.Activity

import android.Manifest
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewTreeObserver
import android.view.animation.AccelerateInterpolator
import android.widget.*
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.github.angads25.toggle.interfaces.OnToggledListener
import com.github.angads25.toggle.widget.LabeledSwitch
import com.shuttleclone.driver.ui.Adapters.TripsAdapter
import com.shuttleclone.driver.ui.Fragments.HomeFragment
import com.shuttleclone.driver.ui.Fragments.MenuFragment
import com.shuttleclone.driver.ui.Fragments.PassengerListFragment
import com.shuttleclone.driver.R
import com.shuttleclone.driver.Services.BGLocationUpdateService
import com.shuttleclone.driver.Util.hasPermission
import com.shuttleclone.driver.Util.myLog
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.shuttleclone.driver.Util.AppConstants
import com.shuttleclone.driver.Util.LiveUpdate
import com.shuttleclone.driver.Util.LoadingDialog
import com.shuttleclone.driver.Util.LocaleManager
import com.shuttleclone.driver.Util.alertDialog
import com.shuttleclone.driver.Util.getPreference
import com.shuttleclone.driver.Util.isInternetConnection
import com.shuttleclone.driver.Util.isPreference
import com.shuttleclone.driver.Util.savePreference
import com.shuttleclone.driver.Util.sessionExpireDialog
import com.shuttleclone.driver.Util.startDriverLocationService
import com.shuttleclone.driver.Util.startTripTrackingLocationService
import com.shuttleclone.driver.Util.stopDriverLocationService
import com.shuttleclone.driver.Util.stopTripTrackingLocationService
import com.shuttleclone.driver.Util.toast
import com.shuttleclone.driver.Util.vibratePhone
import com.shuttleclone.driver.ViewModel.MainViewModel
import java.lang.Exception


class MainActivity : BaseActivity(), View.OnClickListener {

    var rootLayout: View? = null
    val EXTRA_CIRCULAR_REVEAL_X = "EXTRA_CIRCULAR_REVEAL_X"
    val EXTRA_CIRCULAR_REVEAL_Y = "EXTRA_CIRCULAR_REVEAL_Y"
    val TAG = "MainActivity"
    private var revealX = 0
    private var revealY = 0
    private var PERMISSION_REQUEST_LOCATION = 102
    private var REQUEST_ID_MULTIPLE_PERMISSIONS = 1
    private val REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE = 56
    private val mainViewModel: MainViewModel by viewModels()


    var rvTrips: RecyclerView? = null
    var tripsAdapter: TripsAdapter? = null
    var ivNotification: ImageView? = null
    var ivMenu: ImageView? = null
    var tvProfile: TextView? = null
    var navDrawer: DrawerLayout? = null

    var mTvTitle: TextView? = null
    private var mIvNotification: ImageView? = null
    private var mIvHome: ImageView? = null
    private var mIvPackages: ImageView? = null
    private var mIvPassengerList: ImageView? = null
    private var mIvOther: ImageView? = null
    private var mDriverOnOffSwitch: LabeledSwitch? = null
    private var mHomeFragment: HomeFragment? = null
    private var mPassengerListFragment: PassengerListFragment? = null
    private var mMenuFragment: MenuFragment? = null
    private var mLlHome: LinearLayout? = null
    private var mLllPackages: LinearLayout? = null
    private var mLlPassengerList: LinearLayout? = null
    private var mLlMore: LinearLayout? = null
    private var mDriverOfflineDialog: Dialog? = null

    private lateinit var btnAllowLPermission: Button
    private lateinit var btnAllowBGLPermission: Button
    private lateinit var tvLocationAlert: TextView
    private lateinit var locationAllowView: View
    private lateinit var locationAllowDialog: BottomSheetDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocaleManager().setLocale(this)
        setContentView(R.layout.activity_main)

        initLayouts()
        doOperationOnLayouts()
        // explode animation on activity start.
        explodeAnim(savedInstanceState, intent)

        setSelected(mIvHome!!)
        loadFragment(mHomeFragment)

        LiveUpdate.updateLocation.observe(this, Observer {
            myLog("updateLocation", it.longitude.toString())
        })

        handelViewModel()
    }


    private fun handelViewModel() {
        try {
            if (isInternetConnection(this)) {
                LoadingDialog.showLoadingDialog(this, getString(R.string.pls_wait_loading))
                mainViewModel!!.getConfigSettings(getPreference(this, AppConstants.TOKEN)!!)
                    .observe(this,
                        Observer {
                            LoadingDialog.cancelLoading()
                            if (it == null) {
                                sessionExpireDialog(this)
                                return@Observer
                            }

                            if (null!=it.errorResponse) {
                                alertDialog(this, it.errorResponse.message.toString())
                                return@Observer
                            }

                            if (it.status!!) {
                                savePreference(
                                    this,
                                    AppConstants.COMMON_DATA,
                                    Gson().toJson(it.data)
                                )
                            } else alertDialog(this, it.message.toString())
                        })
            } else toast(this)
        } catch (e: Exception) {
            Log.i(TAG, "getConfigSettings: Error=${e.localizedMessage}")
        }
    }

    override fun onResume() {
        super.onResume()
        LocaleManager().setLocale(this)
        checkPermission()
    }

    /* init layout */
    private fun initLayouts() {
        rootLayout = findViewById(R.id.rlMain)

        ivNotification = findViewById(R.id.ivNotification)
        ivMenu = findViewById(R.id.ivMenu)
//        navDrawer = findViewById(R.id.drawer_layout) as DrawerLayout
        tvProfile = findViewById(R.id.tvProfile)

        mTvTitle = findViewById(R.id.tvTitle)
        mIvNotification = findViewById(R.id.ivNotification)
        mLlHome = findViewById(R.id.llHome)
        mLllPackages = findViewById(R.id.llPackage)
        mLlPassengerList = findViewById(R.id.llPList)
        mLlMore = findViewById(R.id.llMore)
        mIvHome = findViewById(R.id.ivHome)
        mIvPackages = findViewById(R.id.ivPackages)
        mIvPassengerList = findViewById(R.id.ivPList)
        mIvOther = findViewById(R.id.ivMore)
        mDriverOnOffSwitch = findViewById(R.id.driverOnOffSwitch)
        mTvTitle!!.text = (getString(R.string.home))

        mHomeFragment = HomeFragment()
        mPassengerListFragment = PassengerListFragment()
        mMenuFragment = MenuFragment()

        inflateLocationDialog()

    }

    private fun inflateLocationDialog() {
        locationAllowView = layoutInflater.inflate(R.layout.location_permission_alert_layout, null)
        locationAllowDialog = BottomSheetDialog(this, R.style.CustomBottomSheetDialogTheme)
        btnAllowLPermission = locationAllowView.findViewById(R.id.btnAllowLPermission)
        btnAllowBGLPermission = locationAllowView.findViewById(R.id.btnAllowBGLPermission)
        tvLocationAlert = locationAllowView.findViewById(R.id.tvLocationAlert)
    }

    /* add functionality to layout */
    private fun doOperationOnLayouts() {
        mIvNotification!!.setOnClickListener(this)
        mLlHome!!.setOnClickListener(this)
        mLllPackages!!.setOnClickListener(this)
        mLlPassengerList!!.setOnClickListener(this)
        mLlMore!!.setOnClickListener(this)
        SetNotificationImage(mIvNotification)

        ivNotification!!.setOnClickListener { startActivity(NotificationActivity::class.java) }

        setDriverDutyStatus()
    }

    private fun setDriverDutyStatus() {
        mDriverOnOffSwitch!!.visibility = View.VISIBLE

        if (getPreference(this, AppConstants.DRIVER_STATUS).equals(AppConstants.Driver_Offline)) {
            vibratePhone(applicationContext)
            mDriverOnOffSwitch!!.isOn = false
            stopDriverLocationService(this)
            stopTripTrackingLocationService(this)

        } else if (getPreference(
                this,
                AppConstants.DRIVER_STATUS
            ).equals(AppConstants.Driver_Online)
            || getPreference(this, AppConstants.DRIVER_STATUS).equals(AppConstants.Driver_Tracking)
        ) {
            mDriverOnOffSwitch!!.isOn = true
            vibratePhone(applicationContext)
            if (isPreference(this, AppConstants.IS_TRIP_STARTED))
                startTripTrackingLocationService(this)
            else startDriverLocationService(this)
        }


        mDriverOnOffSwitch!!.setOnToggledListener(OnToggledListener { _, isOn ->
            if ((getPreference(this, AppConstants.DRIVER_STATUS).equals(AppConstants.Driver_Online)
                        || getPreference(
                    this,
                    AppConstants.DRIVER_STATUS
                ).equals(AppConstants.Driver_Tracking))
                && isPreference(this, AppConstants.IS_TRIP_STARTED)
            ) {
                driverOfflineAlertDialog()
                return@OnToggledListener
            }

            if (isOn) {
                vibratePhone(applicationContext)
                if (isPreference(this, AppConstants.IS_TRIP_STARTED)) {
                    startTripTrackingLocationService(this)
                    savePreference(this, AppConstants.DRIVER_STATUS, AppConstants.Driver_Tracking)
                } else {
                    savePreference(this, AppConstants.DRIVER_STATUS, AppConstants.Driver_Online)
                    startDriverLocationService(this)
                }
            } else {
                savePreference(this, AppConstants.DRIVER_STATUS, AppConstants.Driver_Offline)
                stopDriverLocationService(this)
                stopTripTrackingLocationService(this)
            }
        })

    }

    private fun driverOfflineAlertDialog() {
        try {
            if (mDriverOfflineDialog == null)
                mDriverOfflineDialog = Dialog(this)

            mDriverOfflineDialog!!.setContentView(R.layout.alert_dailog_layout)
            mDriverOfflineDialog!!.setCancelable(true)
            mDriverOfflineDialog!!.window?.setBackgroundDrawable(ColorDrawable(0))

            mDriverOfflineDialog!!.findViewById<View>(R.id.btnOkay).setOnClickListener {
                mDriverOnOffSwitch!!.isOn = true
                if (mDriverOfflineDialog!!.isShowing) mDriverOfflineDialog!!.dismiss()
            }
            mDriverOfflineDialog!!.setCancelable(false)
            mDriverOfflineDialog!!.setCanceledOnTouchOutside(false)

            if (!mDriverOfflineDialog!!.isShowing)
                mDriverOfflineDialog!!.show()
        } catch (e: Exception) {
            myLog(TAG, "driverOfflineAlertDialog: Error=${e.localizedMessage}")
        }
    }


    private fun checkPermission() {
        // Check if the Camera permission has been granted
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is missing and must be requested.
            requestLocationPermission()
        } else {
            updateBackgroundButtonState()
        }
    }

    private fun showBackgroundButton(): Boolean {
        return !applicationContext.hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }

    private fun updateBackgroundButtonState() {
        if (showBackgroundButton()) {
            btnAllowLPermission.visibility = View.GONE
            btnAllowBGLPermission.visibility = View.VISIBLE
            tvLocationAlert.text = getString(R.string.background_location_permission_rationale)
            showLocationPermissionDialog()
        } else if (locationAllowDialog.isShowing) locationAllowDialog.dismiss()
    }


    private fun requestLocationPermission() {
        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            showLocationPermissionDialog()
        } else {
            showLocationPermissionDialog()
        }
    }

    private fun showLocationPermissionDialog() {

        if (locationAllowDialog != null && !locationAllowDialog.isShowing) {

            locationAllowDialog.setOnShowListener(object : DialogInterface.OnShowListener {
                override fun onShow(dialog: DialogInterface) {
                    val d = dialog as BottomSheetDialog
                    val bottomSheet =
                        d.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?
                    val behavior = BottomSheetBehavior.from<View?>(bottomSheet!!)
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED

                    behavior.addBottomSheetCallback(object :
                        BottomSheetBehavior.BottomSheetCallback() {
                        override fun onStateChanged(bottomSheet: View, newState: Int) {
                            if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                            }
                        }

                        override fun onSlide(bottomSheet: View, slideOffset: Float) {}
                    })
                }
            })

            btnAllowLPermission.setOnClickListener {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ),
                    PERMISSION_REQUEST_LOCATION
                )

            }

            btnAllowBGLPermission.setOnClickListener {

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE
                )
            }

            locationAllowDialog.setContentView(locationAllowView)
            locationAllowDialog.setCancelable(false)
            locationAllowDialog.setCanceledOnTouchOutside(false)
            if (!locationAllowDialog.isShowing)
                locationAllowDialog.show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE -> when {
                grantResults.isEmpty() ->
                    // If user interaction was interrupted, the permission request
                    // is cancelled and you receive an empty array.
                    myLog(TAG, "User interaction was cancelled.")

                grantResults[0] == PackageManager.PERMISSION_GRANTED ->
                    if (locationAllowDialog != null)
                        locationAllowDialog.dismiss()


                else -> {
                    Snackbar.make(
                        locationAllowDialog.getWindow()!!.getDecorView(),
                        R.string.background_permission_denied_explanation,
                        Snackbar.LENGTH_LONG
                    )
                        .setAction(R.string.settings) {
                            // Build intent that displays the App settings screen.
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts(
                                "package",
                                packageName,
                                null
                            )
                            intent.data = uri
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                        .show()
                }
            }

            PERMISSION_REQUEST_LOCATION -> {

                // Request for camera permission.
                myLog(TAG, "onRequestPermissionsResult: grantResults.size=" + grantResults.size)
                if (grantResults.size == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission has been granted. Start camera preview Activity.
                    /* locationAllowDialog.getWindow()!!.getDecorView().showSnackbar(
                        R.string.location_permission_granted,
                        Snackbar.LENGTH_SHORT
                    )*/

                    permissionGrantedProceedNow()
                    updateBackgroundButtonState()

                } else {
                    // Permission request was denied.

                    Snackbar.make(
                        locationAllowDialog.getWindow()!!.getDecorView(),
                        R.string.fine_permission_denied_explanation,
                        Snackbar.LENGTH_LONG
                    )
                        .setAction(R.string.settings) {
                            // Build intent that displays the App settings screen.
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts(
                                "package",
                                packageName,
                                null
                            )
                            intent.data = uri
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                        .show()
                }
            }

        }
    }

    private fun permissionGrantedProceedNow() {
//        val serviceIntent = Intent(this, LocationUpdateService::class.java)
        val serviceIntent = Intent(this, BGLocationUpdateService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopService(serviceIntent)
            startForegroundService(serviceIntent)
        } else {
            stopService(serviceIntent)
            startService(serviceIntent)
        }

        if (locationAllowDialog != null)
            locationAllowDialog.dismiss()
    }

    // explode animation on activity start.
    private fun explodeAnim(savedInstanceState: Bundle?, intent: Intent) {
        if (savedInstanceState == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
            intent.hasExtra(EXTRA_CIRCULAR_REVEAL_X) &&
            intent.hasExtra(EXTRA_CIRCULAR_REVEAL_Y)
        ) {
            rootLayout?.setVisibility(View.INVISIBLE)
            revealX = intent.getIntExtra(EXTRA_CIRCULAR_REVEAL_X, 0)
            revealY = intent.getIntExtra(EXTRA_CIRCULAR_REVEAL_Y, 0)
            val viewTreeObserver: ViewTreeObserver = rootLayout!!.getViewTreeObserver()
            if (viewTreeObserver.isAlive) {
                viewTreeObserver.addOnGlobalLayoutListener(object :
                    ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        revealActivity(revealX, revealY)
                        rootLayout!!.getViewTreeObserver().removeOnGlobalLayoutListener(this)
                    }
                })
            }
        } else {
            rootLayout!!.setVisibility(View.VISIBLE)
        }
    }

    // explode animation.
    protected fun revealActivity(x: Int, y: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val finalRadius = (Math.max(
                rootLayout!!.getWidth(),
                rootLayout!!.getHeight()
            ) * 1.1f)

            // create the animator for this view (the start radius is zero)
            val circularReveal =
                ViewAnimationUtils.createCircularReveal(rootLayout, x, y, 0f, finalRadius)
            circularReveal.duration = 800
            circularReveal.interpolator = AccelerateInterpolator()

            // make the view visible and start the animation
            rootLayout!!.setVisibility(View.VISIBLE)
            circularReveal.start()
        } else {
            finish()
        }
    }

    /* set selected item in bottom navigation */
    private fun setSelected(mBarImg: ImageView) {
        mBarImg.background = resources.getDrawable(R.drawable.bg_tint_icon)
    }

    /* Update UI */
    private fun updateUi() {
        mIvHome!!.setImageResource(R.drawable.ic_home)
        mIvHome!!.background = null
        mIvPackages!!.setImageResource(R.drawable.ic_package)
        mIvPackages!!.background = null
        mIvPassengerList!!.setImageResource(R.drawable.ic_booking)
        mIvPassengerList!!.background = null
        mIvOther!!.setImageResource(R.drawable.ic_fill)
        mIvOther!!.background = null
    }

    /* onBack press */
    override fun onBackPressed() {
        super.onBackPressed()
    }

    /* onClick listener */
    override fun onClick(v: View) {
        try {
            if (v === mIvNotification) {
                startActivity(NotificationActivity::class.java)
                return
            }
            updateUi()
            when (v.id) {
                R.id.llHome -> {
                    if (!mHomeFragment!!.isVisible) {
                        mTvTitle!!.text = getString(R.string.home)
                        loadFragment(mHomeFragment)
                    }
                    setSelected(mIvHome!!)
                    mIvHome!!.setImageResource(R.drawable.ic_home_fill)
                }

                R.id.llPList -> {
                    if (!mPassengerListFragment!!.isVisible) {
                        mTvTitle!!.setText(PassengerListFragment.mTitle)
                        loadFragment(mPassengerListFragment)
                    }
                    setSelected(mIvPassengerList!!)
                    mIvPassengerList!!.setImageResource(R.drawable.ic_booking_fill)
                }

                R.id.llMore -> {
                    if (!mMenuFragment!!.isVisible()) {
                        mTvTitle!!.setText(getString(R.string.title_more))
                        loadFragment(mMenuFragment)
                    }
                    setSelected(mIvOther!!)
                    mIvOther!!.setImageResource(R.drawable.ic_more_fill2)
                }
            }
        } catch (e: Exception) {
            myLog(TAG, "onClick: Error=${e.localizedMessage}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mDriverOfflineDialog != null && mDriverOfflineDialog!!.isShowing)
            mDriverOfflineDialog!!.dismiss()
    }
}