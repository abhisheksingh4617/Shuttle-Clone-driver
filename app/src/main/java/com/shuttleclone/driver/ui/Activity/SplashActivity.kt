package com.shuttleclone.driver.ui.Activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import com.shuttleclone.driver.R
import com.shuttleclone.driver.Util.*
import com.shuttleclone.driver.ui.intro.IntroActivity

class SplashActivity : BaseActivity(), AppUpdateResponse {
    /*variable declaration*/
    private var mIVLogo: ImageView? = null
    val TAG = "SplashActivity"
    var isAppUptoDate = false
    var rootView: View? = null

    private var REQUEST_ID_MULTIPLE_PERMISSIONS = 1


    override fun onResume() {
        super.onResume()
        LocaleManager().setLocale(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        LocaleManager().setLocale(this)
        setContentView(R.layout.activity_splash)
        initLayouts()
        initializeListeners()

    }

    /* init layout */
    private fun initLayouts() {
        mIVLogo = findViewById(R.id.ivLogo)
        rootView = findViewById(R.id.root_layout)
    }

    /* initialize listener */
    private fun initializeListeners() {
//        Glide.with(this).load(R.raw.ic_logo).into(mIVLogo);
        Handler().postDelayed(
            {

                if (isInternetConnection(this@SplashActivity)) {
                    val getversion = AppUpdate(this@SplashActivity)
                    getversion.execute()

                    myLog(TAG, "part4Anim  appStatus: isAppUptoDate=" + isAppUptoDate)
                } else toast(this@SplashActivity)

                /*if (getPreference(applicationContext, AppConstants.FirstTimeUser).equals("")) {
                    openIntroSlider()
                } else {
                    if (isInternetConnection(this@SplashActivity)) {
                        val getversion = AppUpdate(this@SplashActivity)
                        getversion.execute()

                        myLog(TAG, "part4Anim  appStatus: isAppUptoDate=" + isAppUptoDate)
                    } else toast(this@SplashActivity)

                }*/

            },
            1000
        )
    }

    private fun checkAndRequestPermissions(): Boolean {

        /*val camerapermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val writepermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)*/
        val locationpermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)

        val listPermissionsNeeded = ArrayList<String>()


       /* if (writepermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }*/
        if (locationpermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        /*if (camerapermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA)
        }*/

        if (!listPermissionsNeeded.isEmpty()) {

            myLog("jjjjjjjjjjjjj", "dkfsakjdlf" + listPermissionsNeeded.size)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            }
            return false
        }
        return true
    }


    private fun otherCheck() {
        if (isAppUptoDate) {

            try {

                if (!isPreference(this, AppConstants.IsDriverLogIn)) {
                    signInActivity(rootView!!)
                }else {
                    if (!checkAndRequestPermissions()) {
                        val intent = Intent(this, PermissionActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent)
                    } else {
                        explodeBaseActivity(rootView!!, this)
                    }
                }

            } catch (e: Exception) {
                myLog(TAG, "otherCheck: Error=${e.localizedMessage}")
            }

        }

    }


    private fun openIntroSlider() {

        startActivity(Intent(this, IntroActivity::class.java))
        finish()
        // val intent =  Intent(this, LoginActivity::class.java)
    }

    fun signInActivity(view: View) {

        val login = SelectionActivity()
        val options =
            ActivityOptionsCompat.makeSceneTransitionAnimation(this, view, "transition")
        val revealX = (view.x + view.width / 2).toInt()
        val revealY = (view.y + view.height / 2).toInt()
        val intent = Intent(this, SelectionActivity::class.java)
        intent.putExtra(login.EXTRA_CIRCULAR_REVEAL_X, revealX)
        intent.putExtra(login.EXTRA_CIRCULAR_REVEAL_Y, revealY)
        ActivityCompat.startActivity(this, intent, options.toBundle())
        finish()

    }

    fun explodeBaseActivity(
        view: View,
        activity: SplashActivity
    ) {

        val base = MainActivity()
        val options =
            ActivityOptionsCompat.makeSceneTransitionAnimation(activity, view, "transition")
        val revealX = (view.x + view.width / 2).toInt()
        val revealY = (view.y + view.height / 2).toInt()
        val intent = Intent(activity, MainActivity::class.java)
        intent.putExtra(base.EXTRA_CIRCULAR_REVEAL_X, revealX)
        intent.putExtra(base.EXTRA_CIRCULAR_REVEAL_Y, revealY)
        ActivityCompat.startActivity(activity, intent, options.toBundle())
        finishAffinity()
    }

    override fun appStatus(versionStatus: Boolean) {
        isAppUptoDate = versionStatus
        otherCheck()
        myLog(TAG, "appStatus: isAppUptoDate=" + isAppUptoDate)
    }
}