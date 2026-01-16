package com.shuttleclone.driver.ui.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.identity.GetPhoneNumberHintIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.shuttleclone.driver.Util.*
import com.shuttleclone.driver.ViewModel.MainViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.hbb20.CountryCodePicker
import java.util.*
import com.shuttleclone.driver.Util.AppConstants.COUNTRY_CODE
import org.json.JSONObject
import com.shuttleclone.driver.R

class SignInActivity : BaseActivity(), View.OnClickListener {
    /*variable declaration*/
    private var mBtnContinue: Button? = null
    private var mEdMobileNumber: EditText? = null
    private var mIvFacebook: ImageView? = null
    private var mIvGoogle: ImageView? = null
    private var mCcp: CountryCodePicker? = null
    private var mTvCountyCode: TextView? = null
    private val TAG = "SignInActivity"
    private var countryDetails = ""
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var callbackManager: CallbackManager? = null
    private var mSpLanguage: Spinner? = null
    private val mainViewModel: MainViewModel by viewModels()

    private var mTvTermPolicy: TextView? = null



    var rootLayout: View? = null
    val EXTRA_CIRCULAR_REVEAL_X = "EXTRA_CIRCULAR_REVEAL_X"
    val EXTRA_CIRCULAR_REVEAL_Y = "EXTRA_CIRCULAR_REVEAL_Y"
    private var revealX = 0
    private var revealY = 0

    private var verificationIntent: Intent? = null

    private val phoneNumberHintIntentResultLauncher: ActivityResultLauncher<IntentSenderRequest> =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            try {
                if (result.data==null)
                    result

                val phoneNumber = Identity.getSignInClient(this).getPhoneNumberFromIntent(result.data)
                val phone = phoneNumber.removePrefix(mCcp!!.selectedCountryCodeWithPlus)
                mEdMobileNumber!!.setText(phone)
            } catch (e: Exception) {
                myLog(TAG, result.data?.dataString.toString())

            }
        }

    override fun onResume() {
        super.onResume()
        LocaleManager().setLocale(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        LocaleManager().setLocale(this)
        setContentView(R.layout.activity_sign_in)
        initLayouts()
        initializeListeners()
        fCMToken
        // explode animation on activity start.
        explodeAnim(savedInstanceState, intent)

//        initializeGoogleLogin()

        savePreference(this, AppConstants.DRIVER_STATUS, AppConstants.Driver_Offline)
    }

    // Get new FCM registration token
    private val fCMToken: Unit get() {
            FirebaseMessaging.getInstance().token
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        myLog(TAG, "Fetching FCM registration token failed"+task.exception)
                        return@OnCompleteListener
                    }

                    // Get new FCM registration token
                    val token = task.result
                    savePreference(this@SignInActivity, AppConstants.DEVICE_TOKEN, token)

                    // Log and toast
                    myLog(TAG, "TOKEN=$token")
                })
        }

    /* init layout */
    private fun initLayouts() {
        mEdMobileNumber = findViewById(R.id.edMobileNumber)
        mBtnContinue = findViewById(R.id.btnContinue)
        mIvFacebook = findViewById(R.id.ivFacebook)
        mIvGoogle = findViewById(R.id.ivGoogle)
        rootLayout = findViewById<View>(R.id.root_layout)
        mSpLanguage = findViewById(R.id.spChangeLanguage)
        mCcp = findViewById(R.id.ccp)
        mTvCountyCode = findViewById(R.id.tvCounty_Code)

        mTvTermPolicy = findViewById(R.id.tvTermsPolicy)

        //Comment those lines before giving the apk for testing
        mTvCountyCode!!.visibility=View.GONE
        mCcp!!.visibility=View.VISIBLE

        //Un-Comment this line before giving the apk for testing
        mCcp!!.setCountryForPhoneCode(COUNTRY_CODE)
    }

    /* initialize listener */
    @SuppressLint("ClickableViewAccessibility")
    private fun initializeListeners() {
        mBtnContinue!!.setOnClickListener(this)
        mIvFacebook!!.setOnClickListener(this)
        mIvGoogle!!.setOnClickListener(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBtnContinue!!.stateListAnimator = null
        }
        setLocationSpinner()

        mTvTermPolicy!!.setText(Html.fromHtml(getString(R.string.txt_term_and_policy)))
        mTvTermPolicy!!.setMovementMethod(LinkMovementMethod.getInstance())

    }

    private fun setLocationSpinner() {

        val languageList = resources.getStringArray(R.array.language)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languageList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mSpLanguage!!.adapter = adapter


        mSpLanguage!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedItem = languageList[position]

                when (selectedItem) {
                    "English" -> {
                        savePreference(
                            this@SignInActivity,
                            AppConstants.LANGUAGE,
                            LocaleManager.ENGLISH
                        )

                        startActivity(SplashActivity::class.java)
                        finishAffinity()
                    }
                    "हिन्दी" -> {
                        savePreference(
                            this@SignInActivity,
                            AppConstants.LANGUAGE,
                            LocaleManager.HINDI
                        )
                        startActivity(SplashActivity::class.java)
                        finishAffinity()
                    }
                    else -> {
                    }

                }

            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
    }



    /* validation */
    private fun validate(): Boolean {
        // Allow any phone number - validation removed as per requirement
        return mEdMobileNumber!!.text.toString().isNotEmpty()
    }

    /* onClick listener */
    override fun onClick(v: View) {
        if (v === mBtnContinue) {
            if (validate()) {

                if (isInternetConnection(this)) {
                    LoadingDialog.showLoadingDialog(this, getString(R.string.pls_wait_loading));

                    val countryOjb= JSONObject()
                    countryOjb.put("country_name",mCcp!!.selectedCountryName)
                    countryOjb.put("country_with_plus",mCcp!!.selectedCountryCodeWithPlus)
                    countryOjb.put("country_name_code",mCcp!!.selectedCountryNameCode)
                    countryOjb.put("country_code",mCcp!!.selectedCountryCode)

                    countryDetails=countryOjb.toString()

                   /* RetrofitClient.getClient().loginDriver(
                        mEdMobileNumber!!.text.toString(),mCcp!!.selectedCountryCode,countryDetails
                    ).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object :
                            DisposableSingleObserver<DriverLoginResponseModel?>() {
                            override fun onSuccess(it: DriverLoginResponseModel) {
//                            LoadingDialog.cancelLoading()

                                if (it==null){
                                    LoadingDialog.cancelLoading()
                                    sessionExpireDialog(this@SignInActivity)
                                    return
                                }

                                if (it.errorResponse!=null){
                                    LoadingDialog.cancelLoading()
                                    alertDialog(this@SignInActivity, it.errorResponse.message.toString())
                                    return
                                }

                                if (it.status!!) {
                                    savePreference(this@SignInActivity, AppConstants.TOKEN, "Bearer " + it.token)
                                    savePreference(this@SignInActivity, AppConstants.csrfTOKEN, it.csrfToken)
                                    savePreference(this@SignInActivity, AppConstants.BASEURL, it.baseurl)

                                    verificationIntent = Intent(this@SignInActivity, VerificationActivity::class.java)
                                    verificationIntent!!.putExtra("otp", it.otp)
                                    verificationIntent!!.putExtra("phone", mCcp!!.selectedCountryCodeWithPlus + "${mEdMobileNumber!!.text}")
                                    verificationIntent!!.putExtra("country_code", mCcp!!.selectedCountryCode)
                                    verificationIntent!!.putExtra("country_details", countryDetails)

                                    sendVerificationCode(mCcp!!.selectedCountryCodeWithPlus+"${mEdMobileNumber!!.text}")

                                } else {
                                    LoadingDialog.cancelLoading()
                                    alertDialog(this@SignInActivity, it.title.toString())
                                }
                            }

                            override fun onError(e: Throwable) {
                                myLog(TAG, "onError: Registration Error=" + e.localizedMessage)
                                toast(applicationContext, e.localizedMessage)
                                LoadingDialog.cancelLoading()
                            }
                        })*/


                    mainViewModel!!.driverLogin(mEdMobileNumber!!.text.toString(),mCcp!!.selectedCountryCode,countryDetails)
                        .observe(this, androidx.lifecycle.Observer {

                            if (it==null){
                                LoadingDialog.cancelLoading()
                                sessionExpireDialog(this)
                                return@Observer
                            }

                            if (it.errorResponse!=null){
                                LoadingDialog.cancelLoading()
                                alertDialog(this, it.errorResponse.message.toString())
                                return@Observer
                            }

                            if (it.status!!) {
                                savePreference(this, AppConstants.TOKEN, "Bearer " + it.token)
                                savePreference(this, AppConstants.csrfTOKEN, it.csrfToken)
                                savePreference(this, AppConstants.BASEURL, it.baseurl)

                                verificationIntent = Intent(this, VerificationActivity::class.java)
                                verificationIntent!!.putExtra("otp", it.otp)
                                verificationIntent!!.putExtra("phone", mCcp!!.selectedCountryCodeWithPlus + "${mEdMobileNumber!!.text}")
                                verificationIntent!!.putExtra("country_code", mCcp!!.selectedCountryCode)
                                verificationIntent!!.putExtra("country_details", countryDetails)

                                // Start VerificationActivity - OTP is sent by backend
                                startActivity(verificationIntent)

                            } else {
                                LoadingDialog.cancelLoading()
                                alertDialog(this, it.title.toString())
                            }
                        })
                }else toast(this)
            }
        } else if (v === mIvFacebook) {
            initializeFacebookLogin()
        } else if (v === mIvGoogle) {
            val signInIntent = mGoogleSignInClient!!.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    private fun initializeFacebookLogin() {
        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance()
            .logInWithReadPermissions(this, Arrays.asList("email,public_profile,user_gender"))
        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    // App code
                    myLog(TAG, "onSuccess=" + loginResult.accessToken.applicationId)
                    myLog(TAG, "onSuccess=" + loginResult.accessToken.userId)
                    getFaceBookData(loginResult.accessToken)
                }

                override fun onCancel() {
                    // App code
                    myLog(TAG, "onCancel=Login canceled")
                }

                override fun onError(exception: FacebookException) {
                    // App code
                    myLog(TAG, "onError=" + exception.localizedMessage)
                }
            })
    }

    private fun getFaceBookData(accessToken: AccessToken) {
        try {
            /*val graphRequest: GraphRequest = GraphRequest().newMeRequest(
                accessToken,
                GraphJSONObjectCallback { `object`, response ->
                    myLog(
                        TAG,
                        "onCompleted: JSONObject=$`object`"
                    )
                })*/
        } catch (e: Exception) {
            myLog(TAG, "getFaceBookData: Errror=" + e.localizedMessage)
        }
    }

    private fun initializeGoogleLogin() {
        val request: GetPhoneNumberHintIntentRequest =
            GetPhoneNumberHintIntentRequest.builder().build()

        Identity.getSignInClient(this)
            .getPhoneNumberHintIntent(request)
            .addOnSuccessListener {
                phoneNumberHintIntentResultLauncher.launch(
                    IntentSenderRequest.Builder(it.intentSender).build()
                )
            }
            .addOnFailureListener {
                myLog(TAG, it.message.toString())
            }
    }


    companion object {
        private const val RC_SIGN_IN = 150
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
            rootLayout!!.visibility = View.VISIBLE
            circularReveal.start()
        } else {
            finish()
        }
    }
}