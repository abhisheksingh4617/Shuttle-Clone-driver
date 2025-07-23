package com.shuttleclone.driver.ui.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.shuttleclone.driver.Model.ScanTicketResponseModel
import com.shuttleclone.driver.R
import com.shuttleclone.driver.Util.*
import com.shuttleclone.driver.ViewModel.MainViewModel
import com.google.gson.Gson

class ScanTicketActivity : AppCompatActivity() {

    private lateinit var codeScanner: CodeScanner
    val TAG = "ScanTicketActivity"
    private val mainViewModel: MainViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocaleManager().setLocale(this)
        setContentView(R.layout.activity_scan_ticket)
        val scannerView = findViewById<CodeScannerView>(R.id.scanner_view)

        codeScanner = CodeScanner(this, scannerView)

        // Parameters (default values)
        codeScanner.camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
        codeScanner.formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat,
        // ex. listOf(BarcodeFormat.QR_CODE)
        codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
        codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not

        // Callbacks
        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                toast(this, "Scan result: ${it.text}")
                myLog(TAG, "onCreate: Scan ticket response=${it.text}")
                val ticketData = Gson().fromJson(it.text, ScanTicketResponseModel::class.java)
                ticketScanned(ticketData)

            }
        }
        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            runOnUiThread {
                toast(this, "Camera initialization error: ${it.message}")
            }
        }

        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }

    private fun ticketScanned(ticketData: ScanTicketResponseModel) {
        try {
            if (isInternetConnection(this!!)) {
                LoadingDialog.showLoadingDialog(this!!, getString(R.string.pls_wait_loading))
                mainViewModel!!.updateTicketStatus(
                    getPreference(this!!, AppConstants.TOKEN)!!,
                    ticketData.pnrNo!!,
                    ticketData.travelStatus!!
                ).observe(this, Observer {
                    LoadingDialog.cancelLoading()

                    if (it == null) {
                        sessionExpireDialog(this)
                        return@Observer
                    }

                    if (it.isStatus) {
                        toast(this, it.message)
                    } else alertDialog(this, it.message.toString())

                })

            } else toast(this)

        } catch (e: Exception) {
            myLog(TAG, e.localizedMessage)
            alertDialog(this, e.localizedMessage.toString())
        }

    }


    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
        LocaleManager().setLocale(this)
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }
}