package com.shuttleclone.driver.ui.Activity

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.viewModels
import com.shuttleclone.driver.R
import com.shuttleclone.driver.Util.AppConstants
import com.shuttleclone.driver.Util.LoadingDialog
import com.shuttleclone.driver.Util.LocaleManager
import com.shuttleclone.driver.Util.alertDialog
import com.shuttleclone.driver.Util.getPreference
import com.shuttleclone.driver.Util.isNetworkAvailable
import com.shuttleclone.driver.Util.myLog
import com.shuttleclone.driver.Util.sessionExpireDialog
import com.shuttleclone.driver.Util.toast
import com.shuttleclone.driver.ViewModel.MainViewModel
import java.util.regex.Pattern

class HelpActivity : BaseActivity(), View.OnClickListener {
    /*variable declaration*/
    private var mIvBack: ImageView? = null
    private var mIvNotification: ImageView? = null
    private var mEdContact: EditText? = null
    private var mEdEmail: EditText? = null
    private var mEdMessage: EditText? = null
    private var mBtnSubmit: Button? = null
    private val TAG = "HelpActivity"
    private val mainViewModel: MainViewModel by viewModels()

    override fun onResume() {
        super.onResume()
        LocaleManager().setLocale(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocaleManager().setLocale(this)
        setContentView(R.layout.activity_help)
        initLayouts()
        initialzeListeners()
    }

    /* initialize listener */
    private fun initialzeListeners() {
        mIvBack!!.setOnClickListener(this)
        mBtnSubmit!!.setOnClickListener(this)
        mIvNotification!!.setOnClickListener(this)
//        setTypeFace(mEdContact!!)
    }

    /* init layout */
    private fun initLayouts() {
        mIvBack = findViewById(R.id.ivBack)
        mEdContact = findViewById(R.id.edContact)
        mEdEmail = findViewById(R.id.edEmail)
        mEdMessage = findViewById(R.id.edMessage)
        mIvNotification = findViewById(R.id.ivNotification)
        mBtnSubmit = findViewById(R.id.btnSubmit)
        SetNotificationImage(mIvNotification)
    }

    /* onClick listener */
    override fun onClick(v: View) {
        if (v === mIvBack) onBackPressed() else if (v === mBtnSubmit) {
            if (validate()) {
                help
            }
        } else if (v === mIvNotification) startActivity(NotificationActivity::class.java)
    }

    private val help: Unit
        private get() {
            try {
                if (isNetworkAvailable(this)) {
                    LoadingDialog.showLoadingDialog(this, getString(R.string.pls_wait_loading))
                    mainViewModel!!.helpSupport(
                        getPreference(this, AppConstants.TOKEN),
                        mEdContact!!.text.toString(),
                        mEdEmail!!.text.toString(),
                        mEdMessage!!.text.toString()
                    )
                        .observe(this, androidx.lifecycle.Observer {
                            LoadingDialog.cancelLoading()
                            if (it == null) {
                                sessionExpireDialog(this)
                                return@Observer
                            }

                            try {
                                if (it.isStatus) {
                                    toast(this, it.message)
                                    mEdContact!!.setText("")
                                    mEdEmail!!.setText("")
                                    mEdMessage!!.setText("")
                                } else alertDialog(this, it.message.toString())
                            } catch (e: Exception) {
                                alertDialog(this, e.localizedMessage.toString())
                            }
                        })

                } else toast(this)
            } catch (e: Exception) {
                myLog(TAG, "getProfileDetails: Error=" + e.localizedMessage)
                alertDialog(this@HelpActivity, e.localizedMessage.toString())
                LoadingDialog.cancelLoading()
            }
        }

    /* validations */
    private fun validate(): Boolean {
        var flag = true
        if (mEdContact!!.text.toString() == "" && mEdContact!!.text.toString().length != 10) {
            flag = false
            showToast(getString(R.string.msg_valid_mobile_number))
        } else if (mEdEmail!!.text.toString() == "" && !isEmailValid(mEdEmail!!.text.toString())) {
            flag = false
            showToast(getString(R.string.msg_email_valid))
        } else if (mEdMessage!!.text.toString() == "") {
            flag = false
            showToast(getString(R.string.msg_description))
        }
        return flag
    }

    companion object {
        fun isEmailValid(email: String?): Boolean {
            val expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$"
            val pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
            val matcher = pattern.matcher(email)
            return matcher.matches()
        }
    }
}