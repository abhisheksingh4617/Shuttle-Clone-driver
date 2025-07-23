package com.shuttleclone.driver.ui.Activity

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.shuttleclone.driver.R
import com.shuttleclone.driver.Util.*
import com.shuttleclone.driver.ViewModel.MainViewModel
import de.hdodenhof.circleimageview.CircleImageView

class ProfileActivity : BaseActivity() {

    private val TAG = "ProfileActivity"
    var ivBack: ImageView? = null
    var ivNotification: ImageView? = null
    private var mTvFirstName: TextView? = null
    private var mTvLastName: TextView? = null
    private var mTvEmail: TextView? = null
    private var mTvContact: TextView? = null
    private var mIvProfileImage: CircleImageView? = null
    private val mainViewModel: MainViewModel by viewModels()

    override fun onResume() {
        super.onResume()
        LocaleManager().setLocale(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocaleManager().setLocale(this)
        setContentView(R.layout.activity_profile)

        initLayouts()
        doOperationOnLayouts()
    }

    /* init layout */
    private fun initLayouts() {
        ivBack = findViewById(R.id.ivBack)
        ivNotification = findViewById(R.id.ivNotification)
        mTvFirstName = findViewById(R.id.tvFirstName)
        mTvLastName = findViewById(R.id.tvLastName)
        mTvEmail = findViewById(R.id.tvEmail)
        mTvContact = findViewById(R.id.tvContact)
        mIvProfileImage = findViewById(R.id.ivProfileImage)
    }

    /* add functionality to layout */
    private fun doOperationOnLayouts() {
        ivBack!!.setOnClickListener { finish() }
        ivNotification!!.setOnClickListener { startActivity(NotificationActivity::class.java) }

        getProfileDetails()

    }

    private fun getProfileDetails() {
        try {
            if (isInternetConnection(this)) {
                LoadingDialog.showLoadingDialog(this, getString(R.string.pls_wait_loading))
                mainViewModel!!.getDriverDetails(getPreference(this, AppConstants.TOKEN)!!)
                    .observe(this,
                        Observer {
                            LoadingDialog.cancelLoading()
                            if (it == null) {
                                sessionExpireDialog(this)
                                return@Observer
                            }

                            if (null!=it.errorResponse){
                                alertDialog(this, it.errorResponse.message.toString())
                                return@Observer
                            }

                            if (it.status!!&&null!=it.data) {
                                try {
                                    mTvFirstName!!.setText(it.data!!.firstname)
                                    mTvLastName!!.setText(it.data!!.lastname)
                                    mTvEmail!!.setText(it.data!!.email)
                                    mTvContact!!.setText(it.data!!.phone)

                                    if (it.data.picture != null) {
                                        val url: String = getPreference(
                                            this,
                                            AppConstants.BASEURL
                                        ) + it.data.picture
                                        myLog(TAG, "onSuccess:getProfileDetails url=" + url)
                                        Glide.with(applicationContext).load(url)
                                            .placeholder(R.drawable.ic_profile).into(
                                                mIvProfileImage!!
                                            )
                                    }

                                } catch (e: java.lang.Exception) {
                                    alertDialog(this, e.localizedMessage)
                                }
                            } else alertDialog(this, it.message.toString())
                        })

            } else toast(this)

        } catch (e: Exception) {
            alertDialog(this, e.localizedMessage)
        }
    }
}