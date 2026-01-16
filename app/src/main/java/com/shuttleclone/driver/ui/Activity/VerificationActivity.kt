package com.shuttleclone.driver.ui.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.shuttleclone.driver.Model.UserDetail
import com.shuttleclone.driver.R
import com.shuttleclone.driver.Util.*
import com.shuttleclone.driver.Util.Receiver.SmsBroadcastReceiver
import com.shuttleclone.driver.ViewModel.MainViewModel
import java.util.concurrent.TimeUnit
import java.util.regex.Matcher
import java.util.regex.Pattern

class VerificationActivity : BaseActivity(), View.OnClickListener {
    /*variable declaration*/
    private var mEdDigit1: EditText? = null
    private var mEdDigit2: EditText? = null
    private var mEdDigit3: EditText? = null
    private var mEdDigit4: EditText? = null
    private var mEdDigit5: EditText? = null
    private var mEdDigit6: EditText? = null
    private var mLlVerify: LinearLayout? = null
    private var mTvResend: TextView? = null
    private var mTvTimer: TextView? = null
    private var tvOTP: TextView? = null
    private var mIvBack: ImageView? = null
    private lateinit var mEds: Array<EditText?>
    private var otp = ""
    private var phone = ""
    private var countryCode = "91"
    private var countryDetails = ""
    private var userDetail: UserDetail? = null
    private val TAG = "VerificationActivity"
    private val mainViewModel: MainViewModel by viewModels()
    var smsBroadcastReceiver: SmsBroadcastReceiver? = null

    override fun onResume() {
        super.onResume()
        LocaleManager().setLocale(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocaleManager().setLocale(this)
        setContentView(R.layout.activity_verification)
        initLayouts()
        initializeListeners()
        startSmsUserConsent()
    }

    private fun startSmsUserConsent() {
        val client = SmsRetriever.getClient(this)
        client.startSmsUserConsent(null).addOnSuccessListener { }.addOnFailureListener { }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_USER_CONSENT) {
            if (resultCode == RESULT_OK && data != null) {
                val message = data.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
                getOtpFromMessage(message.toString())
            }
        }
    }

    private fun getOtpFromMessage(message: String) {
        mEdDigit1!!.setText("")
        mEdDigit2!!.setText("")
        mEdDigit3!!.setText("")
        mEdDigit4!!.setText("")
        mEdDigit5!!.setText("")
        mEdDigit6!!.setText("")

        // This will match any 4 digit number in the message
        val pattern: Pattern = Pattern.compile("(|^)\\d{6}")
        val matcher: Matcher = pattern.matcher(message)
        if (matcher.find()) {

            try {
                mEdDigit1!!.setText(matcher.group(0)[0] + "")
                mEdDigit2!!.setText(matcher.group(0)[1] + "")
                mEdDigit3!!.setText(matcher.group(0)[2] + "")
                mEdDigit4!!.setText(matcher.group(0)[3] + "")
                mEdDigit5!!.setText(matcher.group(0)[4] + "")
                mEdDigit6!!.setText(matcher.group(0)[5] + "")

            } catch (e: Exception) {
                myLog(TAG, "getOtpFromMessage: Error=" + e.localizedMessage)
            }


        }
    }

    /* init layout */
    private fun initLayouts() {
        mEdDigit1 = findViewById(R.id.edDigit1)
        mEdDigit2 = findViewById(R.id.edDigit2)
        mEdDigit3 = findViewById(R.id.edDigit3)
        mEdDigit4 = findViewById(R.id.edDigit4)
        mEdDigit5 = findViewById(R.id.edDigit5)
        mEdDigit6 = findViewById(R.id.edDigit6)
        mLlVerify = findViewById(R.id.llVerify)
        mTvResend = findViewById(R.id.tvResend)
        mTvTimer = findViewById(R.id.tvTimer)
        mEds = arrayOf(mEdDigit1, mEdDigit2, mEdDigit3, mEdDigit4 ,mEdDigit5, mEdDigit6)
        mIvBack = findViewById(R.id.ivBack)
        tvOTP = findViewById(R.id.tvOTP)

        if (intent != null) {
            otp = intent.getIntExtra("otp", 0).toString()
            phone = intent.getStringExtra("phone").toString()
            countryCode = intent.getStringExtra("country_code").toString()
            countryDetails = intent.getStringExtra("country_details").toString()
        }
    }

    /* initialize listener */
    private fun initializeListeners() {

        mIvBack!!.setOnClickListener(this)
        mEdDigit1!!.setOnKeyListener(PinOnKeyListener(0))
        mEdDigit2!!.setOnKeyListener(PinOnKeyListener(1))
        mEdDigit3!!.setOnKeyListener(PinOnKeyListener(2))
        mEdDigit4!!.setOnKeyListener(PinOnKeyListener(3))
        mEdDigit5!!.setOnKeyListener(PinOnKeyListener(4))
        mEdDigit6!!.setOnKeyListener(PinOnKeyListener(5))

        mEdDigit1!!.addTextChangedListener(CodeTextWatcher(0))
        mEdDigit2!!.addTextChangedListener(CodeTextWatcher(1))
        mEdDigit3!!.addTextChangedListener(CodeTextWatcher(2))
        mEdDigit4!!.addTextChangedListener(CodeTextWatcher(3))
        mEdDigit5!!.addTextChangedListener(CodeTextWatcher(4))
        mEdDigit6!!.addTextChangedListener(CodeTextWatcher(5))

        mLlVerify!!.setOnClickListener(this)

        mEdDigit1!!.requestFocus()


        startTimer()

       /* mEdDigit6!!.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (validate()) {
                    verifyOTP(getOtp())
                }
                true
            } else false
        }*/

        mTvResend!!.setOnClickListener {
            // Backend Resend OTP
            reSendOTP()
        }
    }



    private fun startTimer() {
        hideView(mTvResend!!)
        showView(mTvTimer!!)
        object : CountDownTimer(60000, 1000) {
            // adjust the milli seconds here
            @SuppressLint("DefaultLocale")
            override fun onTick(millisUntilFinished: Long) {
                mTvTimer!!.text = String.format("%d ${getString(R.string.seconds_left)}",
                    TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                            TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(
                                    millisUntilFinished
                                )
                            )
                )
            }

            override fun onFinish() {
                hideView(mTvTimer!!)
                showView(mTvResend!!)
            }
        }.start()
    }

    private fun reSendOTP() {
        try {
            if (isInternetConnection(this)) {
                LoadingDialog.showLoadingDialog(this, getString(R.string.pls_wait_loading))
                mainViewModel!!.resendOtp(getPreference(this, AppConstants.TOKEN)!!, phone.trim(),countryCode,countryDetails).observe(this,
                    Observer {
                        LoadingDialog.cancelLoading()
                        if (it == null) {
                            sessionExpireDialog(this)
                            return@Observer
                        }

                        if (it.isStatus) {
                            startTimer()
                        }else alertDialog(this, it.message.toString())
                    })

            } else toast(this)

        } catch (e: java.lang.Exception) {
            myLog(TAG, "reSendOTP: Error=${e.localizedMessage}")
        }
    }

    /* onClick listener */
    override fun onClick(v: View) {
        if (v === mIvBack) {
            onBackPressed()
        }
        if (v === mLlVerify) {
            if (validate()) {
                verifyOTP(getOtp())
            }
        }
    }

    private fun verifyOTP(OTP: Int) {
        try {
            if (isInternetConnection(this)) {
                LoadingDialog.showLoadingDialog(this, getString(R.string.pls_wait_loading))
                // isMobileVerified is set to false since we're not using Firebase anymore
                mainViewModel!!.verifyOTP(getPreference(this, AppConstants.TOKEN)!!,getPreference(this, AppConstants.DEVICE_TOKEN).toString(), OTP,false)
                    .observe(this, androidx.lifecycle.Observer {
                        LoadingDialog.cancelLoading()
                        if (it == null){
                            sessionExpireDialog(this)
                            return@Observer
                        }

                        if (it.errorResponse==null) {
                            toast(this, it.title)
                            if (it.status!!) {
                                savePreference(this, AppConstants.IsDriverLogIn, true)
                                savePreference(this, AppConstants.PHONE_NO, phone)
                                if (!checkAndRequestPermissions(this)) startActivity(
                                    PermissionActivity::class.java
                                )
                                else {
                                    startActivity(Intent(this, MainActivity::class.java))
                                    finishAffinity()
                                }

                            }else alertDialog(this,it.title.toString())
                        } else alertDialog(this,it.errorResponse!!.message.toString())
                    })

            } else toast(this)

        } catch (e: Exception) {
            myLog(TAG, "verifyOTP: Error=" + e.localizedMessage)
            LoadingDialog.cancelLoading()
        }
    }


    /* Validation */
    private fun validate(): Boolean {
        var flag = true
        if (TextUtils.isEmpty(mEdDigit1!!.text)) {
            flag = false
            showToast(getString(R.string.msg_code))
        } else if (TextUtils.isEmpty(mEdDigit2!!.text)) {
            flag = false
            showToast(getString(R.string.msg_code))
        } else if (TextUtils.isEmpty(mEdDigit3!!.text)) {
            flag = false
            showToast(getString(R.string.msg_code))
        } else if (TextUtils.isEmpty(mEdDigit4!!.text)) {
            flag = false
            showToast(getString(R.string.msg_code))
        } else if (TextUtils.isEmpty(mEdDigit5!!.text)) {
            flag = false
            showToast(getString(R.string.msg_code))
        } else if (TextUtils.isEmpty(mEdDigit6!!.text)) {
            flag = false
            showToast(getString(R.string.msg_code))
        }
        return flag
    }


    private fun getOtp(): Int {
        val otp =
            mEdDigit1!!.text.toString() + mEdDigit2!!.text.toString() + mEdDigit3!!.text.toString() + mEdDigit4!!.text.toString()+ mEdDigit5!!.text.toString() + mEdDigit6!!.text.toString()
        myLog(TAG, "getEdtOTP: otp==$otp")
        return otp.toInt()
    }

    /* back space key handler*/
    inner class PinOnKeyListener internal constructor(private val mCurrentIndex: Int) :
        View.OnKeyListener {
        override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
            if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                if (mEds[mCurrentIndex]!!.text.toString().isEmpty() && mCurrentIndex != 0) {
                    mEds[mCurrentIndex - 1]!!.requestFocus()
                }
            }
            return false
        }
    }

    /* implement TextWatcher class*/
    inner class CodeTextWatcher internal constructor(private val mCurrentIndex: Int) : TextWatcher {
        private var mIsFirst = false
        private var mIsLast = false
        private var mNewString = ""
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            mNewString = s.subSequence(start, start + count).toString().trim { it <= ' ' }
        }

        override fun afterTextChanged(s: Editable) {
            var text = mNewString
            if (text.length > 1) text = text[0].toString()
            mEds[mCurrentIndex]!!.removeTextChangedListener(this)
            mEds[mCurrentIndex]!!.setText(text)
            mEds[mCurrentIndex]!!.setSelection(text.length)
            mEds[mCurrentIndex]!!.addTextChangedListener(this)
            if (text.length == 1) moveToNext() else if (text.length == 0) moveToPrevious()
        }

        private fun moveToNext() {
            if (!mIsLast) mEds[mCurrentIndex + 1]!!.requestFocus()
            if (isAllEditTextsFilled && mIsLast) {
                mEds[mCurrentIndex]!!.clearFocus()
                hideKeyboard()
            }
        }

        private fun moveToPrevious() {
            if (!mIsFirst) mEds[mCurrentIndex - 1]!!.requestFocus()
        }

        private val isAllEditTextsFilled: Boolean
            private get() {
                for (editText in mEds) if (editText!!.text.toString().trim { it <= ' ' }.isEmpty()) return false
                return true
            }

        private fun hideKeyboard() {
            if (currentFocus != null) {
                val inputMethodManager =
                    getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
            }
        }

        init {
            if (mCurrentIndex == 0) mIsFirst =
                true else if (mCurrentIndex == mEds.size - 1) mIsLast = true
        }
    }


    private fun registerBroadcastReceiver() {
        smsBroadcastReceiver = SmsBroadcastReceiver()
        smsBroadcastReceiver!!.smsBroadcastReceiverListener =
            object : SmsBroadcastReceiver.SmsBroadcastReceiverListener {
                override fun onSuccess(intent: Intent) {
                    startActivityForResult(intent, REQ_USER_CONSENT)
                }

                override fun onFailure() {}
            }
        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(smsBroadcastReceiver, intentFilter, RECEIVER_NOT_EXPORTED)
        }else registerReceiver(smsBroadcastReceiver, intentFilter)
    }


    override fun onStart() {
        super.onStart()
        registerBroadcastReceiver()
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(smsBroadcastReceiver)
    }

    companion object {
        private const val REQ_USER_CONSENT = 200
    }
}