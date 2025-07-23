package com.shuttleclone.driver.ui.Activity

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.shuttleclone.driver.ui.Adapters.NotificationListAdapter
import com.shuttleclone.driver.R
import com.shuttleclone.driver.Util.*
import com.shuttleclone.driver.ViewModel.MainViewModel
import com.shuttleclone.driver.databinding.ActivityNotificationBinding

class NotificationActivity : BaseActivity() {
    var ivBack: ImageView? = null
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var binding:ActivityNotificationBinding
    private val TAG="NotificationActivity"

    override fun onResume() {
        super.onResume()
        LocaleManager().setLocale(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocaleManager().setLocale(this)
        binding=ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)


        doOperationOnLayouts()
        loadData()

    }

    private fun loadData() {

        if (isInternetConnection(this)) {
            LoadingDialog.showLoadingDialog(this, getString(R.string.pls_wait_loading))
            mainViewModel.getNotificationData(getPreference(this, AppConstants.TOKEN).toString(),50,1)
                .observe(this,
                    Observer {
                        LoadingDialog.cancelLoading()
                        try {
                            if (it == null) {
                                sessionExpireDialog(this)
                                return@Observer
                            }

                            if (null!=it.errorResponse){
                                binding.rvNotifications.visibility = View.GONE
                                binding.layNoNotificationAvailable.visibility = View.VISIBLE
                                alertDialog(this,it.errorResponse.message.toString())
                                return@Observer
                            }

                            if (it.status!! && it.data!!.size != 0) {
                                binding.rvNotifications.visibility = View.VISIBLE
                                binding.layNoNotificationAvailable.visibility = View.GONE
                                binding.rvNotifications.apply {
                                    setHasFixedSize(true)
                                    layoutManager =
                                        LinearLayoutManager(this@NotificationActivity)
                                    adapter = NotificationListAdapter(
                                        applicationContext,
                                        this@NotificationActivity,
                                        it.data!!
                                    )
                                }
                                RunLayoutAnimation(this, binding.rvNotifications!!)
                            } else {
                                binding.rvNotifications.visibility = View.GONE
                                binding.layNoNotificationAvailable.visibility = View.VISIBLE
                                alertDialog(this,it.message.toString())
                            }
                        } catch (e: Exception) {
                            binding.rvNotifications.visibility = View.GONE
                            binding.layNoNotificationAvailable.visibility = View.VISIBLE
                            myLog(TAG, "loadData: Error=${e.localizedMessage}")
                            alertDialog(this,e.localizedMessage.toString())
                        }


                    })
        }else toast(this)
    }


    /* add functionality to layout */
    private fun doOperationOnLayouts() {
        binding.ivBack!!.setOnClickListener { finish() }
    }

    fun updateNotificationStatus(id: String?) {
        if (isInternetConnection(this)) {
            LoadingDialog.showLoadingDialog(this, getString(R.string.pls_wait_loading))
            mainViewModel.updateNotificationStatus(getPreference(this,AppConstants.TOKEN).toString(),id.toString(),"1")
                .observe(this,
                    Observer {
                        LoadingDialog.cancelLoading()

                        if (it == null) {
                            sessionExpireDialog(this)
                            return@Observer
                        }

                        try {
                            if (it.isStatus) {
                                toast(this,it.message)
                            }else alertDialog(this, it.message.toString())
                        } catch (e: Exception) {
                            alertDialog(this, e.localizedMessage.toString())
                        }


                    })
        }else toast(this)

    }
}