package com.shuttleclone.driver.ui.Fragments

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.shuttleclone.driver.ui.Activity.HelpActivity
import com.shuttleclone.driver.ui.Activity.ProfileActivity
import com.shuttleclone.driver.ui.Activity.SplashActivity
import com.shuttleclone.driver.R
import com.shuttleclone.driver.Util.*
import com.shuttleclone.driver.ViewModel.MainViewModel

class MenuFragment : Fragment(), View.OnClickListener {

    private var TAG = "MenuFragment"
    private var mTvProfile: TextView? = null
    private var mTvSupport: TextView? = null
    private var mTvLogout: TextView? = null
    private var mTvVersion: TextView? = null
    private var mTvChangeLanguage: TextView? = null
    private var mTvTermsCondition: TextView? = null
    private var mTvPrivacyPolicy: TextView? = null
    private val mainViewModel: MainViewModel by viewModels()
    private var mContext: Context? = null
    private var selectedLanguage = ""


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_menu, container, false)

        initView(view)
        setListener()

        return view
    }


    private fun initView(view: View) {
        mTvProfile = view.findViewById(R.id.tvProfile)
        mTvSupport = view.findViewById(R.id.tvHelp)
        mTvChangeLanguage = view.findViewById(R.id.tvChangeLanguage)
        mTvLogout = view.findViewById(R.id.tvLogout)
        mTvVersion = view.findViewById(R.id.tvVersion)
        mTvVersion!!.text="${requireActivity().getString(R.string.text_version)+ requireContext().packageManager.getPackageInfo(requireContext().packageName, 0).versionName}"
        mTvTermsCondition = view.findViewById(R.id.tvTermsCondition)
        mTvPrivacyPolicy = view.findViewById(R.id.tvPrivacyPolicy)
    }


    private fun setListener() {
        mTvProfile!!.setOnClickListener(this)
        mTvSupport!!.setOnClickListener(this)
        mTvChangeLanguage!!.setOnClickListener(this)
        mTvLogout!!.setOnClickListener(this)
        mTvTermsCondition!!.setOnClickListener(this)
        mTvPrivacyPolicy!!.setOnClickListener(this)

    }

    override fun onClick(v: View) {

        when (v.id) {
            R.id.tvProfile -> {
                requireActivity().startActivity(Intent(activity, ProfileActivity::class.java))
            }
            R.id.tvHelp -> {
                requireActivity().startActivity(Intent(activity, HelpActivity::class.java))
            }
            R.id.tvChangeLanguage -> {
                updateLanguageDialog(requireContext())
            }
            R.id.tvTermsPolicy -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_terms)))
                requireActivity().startActivity(intent)
            }
            R.id.tvPrivacyPolicy -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_privacy)))
                requireActivity().startActivity(intent)
            }
            R.id.tvLogout -> {
                logOut()
            }
        }
    }

    private fun logOut() {

        try {
            if (isInternetConnection(mContext!!)) {
                LoadingDialog.showLoadingDialog(mContext!!, getString(R.string.pls_wait_loading))
                mainViewModel!!.logOut(
                    getPreference(mContext!!, AppConstants.TOKEN)!!,
                    getPreference(mContext, AppConstants.csrfTOKEN)!!
                )
                    .observe(this,
                        Observer {
                            LoadingDialog.cancelLoading()
                            if (it == null) {
                                sessionExpireDialog(requireActivity())
                                return@Observer
                            }

                            if (it.isStatus!!) {
                                clearData()
                                requireActivity().finish()
                            } else alertDialog(requireContext(), it.message.toString())

                        })

            } else toast(mContext)

        } catch (e: Exception) {
            myLog(TAG, e.localizedMessage)
        }

    }

    private var mLanguageChangeDialog: Dialog? = null
    private fun updateLanguageDialog(mContext: Context) {
        try {
            if (mLanguageChangeDialog == null)
                mLanguageChangeDialog = Dialog(mContext)

            mLanguageChangeDialog!!.setContentView(R.layout.language_update_dialog)
            mLanguageChangeDialog!!.setCancelable(true)
            mLanguageChangeDialog!!.window?.setBackgroundDrawable(ColorDrawable(0))


            val radioGroup = mLanguageChangeDialog!!.findViewById<RadioGroup>(R.id.rgLanguage)

            var selectedText = ""

            if (getPreference(requireContext(), AppConstants.LANGUAGE).equals("en")) {
                selectedText = "English"
                radioGroup!!.check(R.id.rbEnglish)
            } else if(getPreference(requireContext(), AppConstants.LANGUAGE).equals("ar")) {
                selectedText="عربي"
                radioGroup!!.check(R.id.rbArabic)
            }

            radioGroup.setOnCheckedChangeListener { group, checkedId ->
                val selectedRadioButton =
                    mLanguageChangeDialog!!.findViewById<RadioButton>(checkedId)
                selectedText = selectedRadioButton.text.toString()
            }

            mLanguageChangeDialog!!.findViewById<View>(R.id.btnConfirm).setOnClickListener {

                if (getPreference(requireContext(), AppConstants.LANGUAGE).equals("en") && selectedText.equals("English")) {
                    toast(requireContext(), getString(R.string.this_language_is_already_set))
                    return@setOnClickListener
                } else if (getPreference(requireContext(), AppConstants.LANGUAGE).equals("ar") && selectedText.equals("عربي")) {
                    toast(requireContext(), getString(R.string.this_language_is_already_set))
                    return@setOnClickListener
                }

                try {
                    if (isInternetConnection(mContext!!)) {
                        LoadingDialog.showLoadingDialog(mContext!!, getString(R.string.pls_wait_loading))
                        mainViewModel!!.updateLanguage(
                            getPreference(mContext!!, AppConstants.TOKEN)!!,
                            selectedText
                        )
                            .observe(this,
                                Observer {
                                    if (mLanguageChangeDialog!!.isShowing) mLanguageChangeDialog!!.dismiss()
                                    LoadingDialog.cancelLoading()
                                    if (it == null) {
                                        sessionExpireDialog(requireActivity())
                                        return@Observer
                                    }

                                    toast(context,it.message)

                                    if (it.isStatus!!) {
                                        when (selectedText) {
                                            "English" -> {
                                                savePreference(
                                                    requireActivity(),
                                                    AppConstants.LANGUAGE,
                                                    LocaleManager.ENGLISH
                                                )

                                                requireActivity().startActivity(
                                                    Intent(
                                                        requireActivity(),
                                                        SplashActivity::class.java
                                                    )
                                                )
                                                requireActivity().finishAffinity()
                                            }

                                            "عربي" -> {
                                                savePreference(
                                                    requireActivity(),
                                                    AppConstants.LANGUAGE,
                                                    LocaleManager.ARABIC
                                                )

                                                requireActivity().startActivity(
                                                    Intent(
                                                        requireActivity(),
                                                        SplashActivity::class.java
                                                    )
                                                )
                                                requireActivity().finishAffinity()
                                            }

                                        }

                                    } else alertDialog(requireContext(), it.message.toString())

                                })

                    } else toast(mContext)

                } catch (e: Exception) {
                    myLog(TAG, e.localizedMessage)
                }



            }

            mLanguageChangeDialog!!.setCancelable(true)
            mLanguageChangeDialog!!.setCanceledOnTouchOutside(true)

            if (!mLanguageChangeDialog!!.isShowing)
                mLanguageChangeDialog!!.show()

        } catch (e: java.lang.Exception) {
            myLog(TAG, "alertDialog: Error=${e.localizedMessage}")
        }
    }

    private fun clearData() {
        savePreference(mContext, AppConstants.TOKEN, "")
        savePreference(mContext, AppConstants.csrfTOKEN, "")
        savePreference(mContext, AppConstants.PHONE_NO, "")
        savePreference(mContext, AppConstants.BASEURL, "")
        savePreference(mContext, AppConstants.IsDriverLogIn,false )
    }


}