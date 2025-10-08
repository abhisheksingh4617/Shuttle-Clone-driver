package com.shuttleclone.driver.ui.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewTreeObserver
import android.view.Window
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import com.shuttleclone.driver.R
import com.shuttleclone.driver.Util.AppConstants
import com.shuttleclone.driver.Util.LocaleManager
import com.shuttleclone.driver.Util.savePreference

class SelectionActivity : BaseActivity(), View.OnClickListener {
    /*variable declaration*/
    private var mBtnContinue: Button? = null
    private var mSpLanguage: Spinner? = null
    var rootLayout: View? = null
    val EXTRA_CIRCULAR_REVEAL_X = "EXTRA_CIRCULAR_REVEAL_X"
    val EXTRA_CIRCULAR_REVEAL_Y = "EXTRA_CIRCULAR_REVEAL_Y"
    private var revealX = 0
    private var revealY = 0
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
        setContentView(R.layout.activity_selection)
        initLayouts()
        initializeListeners()
        // explode animation on activity start.
        explodeAnim(savedInstanceState, intent)
    }

    /* init layout */
    private fun initLayouts() {
        mBtnContinue = findViewById(R.id.btnContinue)
        mSpLanguage = findViewById(R.id.spChangeLanguage)
        rootLayout = findViewById<View>(R.id.root_layout)
    }

    /* initialize listener */
    @SuppressLint("ClickableViewAccessibility")
    private fun initializeListeners() {
        mBtnContinue!!.setOnClickListener(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBtnContinue!!.stateListAnimator = null
        }

        setLocationSpinner()
    }

    /* onClick listener */
    override fun onClick(v: View) {
        if (v === mBtnContinue) {
            startActivity(SignInActivity::class.java)
            savePreference(this@SelectionActivity, AppConstants.FirstTimeUser, "NO")
            finish()
        }

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
                            this@SelectionActivity,
                            AppConstants.LANGUAGE,
                            LocaleManager.ENGLISH
                        )
                        startActivity(SplashActivity::class.java)
                        finishAffinity()
                    }

                    "हिन्दी" -> {
                        savePreference(
                            this@SelectionActivity,
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