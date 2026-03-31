package com.shuttleclone.driver.RetrofitRepository

import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.shuttleclone.driver.Model.*
import com.shuttleclone.driver.Util.*
import com.google.gson.Gson
import com.shuttleclone.driver.Util.AppConstants.DEVICE_TYPE
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers

class MainRepo(val application: Application) {

    val TAG = "MainRepo"

    val loginData : MutableLiveData<DriverLoginResponseModel?> by lazy { MutableLiveData() }
    val driverDetails : MutableLiveData<DriverDetailsResponseModel?> by lazy { MutableLiveData() }
    val otpVerify : MutableLiveData<DriverVerifyResponseModel?> by lazy { MutableLiveData() }
    val reSendOtp : MutableLiveData<DefaultResponse?> by lazy { MutableLiveData() }
    val logout : MutableLiveData<DefaultResponse?> by lazy { MutableLiveData() }
    val refreshToken : MutableLiveData<RefreshTokenModel?> by lazy { MutableLiveData() }
    val tripList : MutableLiveData<MyTripListResponseModel?> by lazy { MutableLiveData() }
    val stopDetails : MutableLiveData<StopsDetailResponseModel?> by lazy { MutableLiveData() }
    val passengerDetails : MutableLiveData<PassengerDetailsResponseModel?> by lazy { MutableLiveData() }
    val scannedTicketStatus : MutableLiveData<DefaultResponse?> by lazy { MutableLiveData() }
    val driverLocationUpdateStatus : MutableLiveData<DefaultResponse?> by lazy { MutableLiveData() }
    val notificationListData : MutableLiveData<NotificationResponseModel?> by lazy { MutableLiveData() }
    val updateNotificationStatus : MutableLiveData<DefaultResponse?> by lazy { MutableLiveData() }
    val updateTrackingData : MutableLiveData<TrackingStatusResponse?> by lazy { MutableLiveData() }
    val configSettings : MutableLiveData<CommonDataResponse?> by lazy { MutableLiveData() }
    val helpAndSupport : MutableLiveData<DefaultResponse?> by lazy { MutableLiveData() }
    val updateLanguage : MutableLiveData<DefaultResponse?> by lazy { MutableLiveData() }

    private val apiClient :ApiCalls by lazy { RetrofitClient.getClient() }
    
    /**
     * Helper function to get user-friendly error message
     */
    private fun getErrorMessage(error: Throwable): String {
        return NetworkErrorHandler.getErrorMessage(application, error)
    }

    @Suppress("NullSafeMutableLiveData")
    fun driverLogin(phoneNo: String, countryCode: String, countryDetails: String) {
        apiClient
            .loginDriver(phoneNo,countryCode,countryDetails, getPreference(application,AppConstants.LANGUAGE).toString())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : DisposableSingleObserver<DriverLoginResponseModel?>() {
                override fun onSuccess(response: DriverLoginResponseModel) {
                    myLog(TAG, "driverLogin->${Gson().toJson(response)}")
                    loginData?.value = response
                }

                override fun onError(e: Throwable) {
                    myLog(TAG, "driverLogin->${NetworkErrorHandler.getSanitizedLogMessage(e)}")
                    loginData?.value = DriverLoginResponseModel(errorResponse = ErrorResponse(getErrorMessage(e), true))
                }
            })
    }
    @Suppress("NullSafeMutableLiveData")
    fun verifyOTP(token: String, deviceToken: String, otp: Int, isMobileVerified: Boolean) {
        apiClient.verifyOtp(token,deviceToken, DEVICE_TYPE,otp,
            isMobileVerified,
            getDeviceDetails()).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : DisposableSingleObserver<DriverVerifyResponseModel>() {
                override fun onSuccess(response: DriverVerifyResponseModel) {
                    myLog(TAG, "verifyOTP->${Gson().toJson(response)}")
                    otpVerify?.value = response
                }

                override fun onError(e: Throwable) {
                    myLog(TAG, "verifyOTP->${NetworkErrorHandler.getSanitizedLogMessage(e)}")

                    if (e.localizedMessage.equals(AppConstants.Unauthorized)||e.localizedMessage.equals(AppConstants.Forbidden))
                        checkToken(application, ApiCallBack { success ->
                            if (success) verifyOTP(getPreference(application, AppConstants.TOKEN)!!, deviceToken,otp,isMobileVerified)
                            else otpVerify?.value = null
                        })
                    else otpVerify?.value = DriverVerifyResponseModel(errorResponse = ErrorResponse(getErrorMessage(e), true))

                }

            })
    }

    @Suppress("NullSafeMutableLiveData")
    fun resendOtp(token: String, phone: String, countryCode: String, countryDetails: String) {
        apiClient.resendOtp(token, phone, countryCode,countryDetails).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : DisposableSingleObserver<DefaultResponse>() {
                override fun onSuccess(response: DefaultResponse) {
                    myLog(TAG, "resendOtp->${Gson().toJson(response)}")
                    reSendOtp?.value = response
                }

                override fun onError(e: Throwable) {
                    myLog(TAG, "resendOtp->${NetworkErrorHandler.getSanitizedLogMessage(e)}")
                    if (e.localizedMessage.equals(AppConstants.Unauthorized)||e.localizedMessage.equals(AppConstants.Forbidden))
                        checkToken(application, ApiCallBack { success ->
                            if (success) resendOtp(getPreference(application, AppConstants.TOKEN)!!, phone,countryCode,countryDetails)
                            else reSendOtp?.value = null
                        })
                    else reSendOtp?.value = DefaultResponse(getErrorMessage(e),0,false)

                }

            })
    }

    @Suppress("NullSafeMutableLiveData")
    fun getDriverDetails(token: String) {
        apiClient.getProfileDetails(token).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : DisposableSingleObserver<DriverDetailsResponseModel>() {
                override fun onSuccess(response: DriverDetailsResponseModel) {
                    myLog(TAG, "getDriverDetails->${Gson().toJson(response)}")
                    driverDetails?.value = response
                }

                override fun onError(e: Throwable) {
                    myLog(TAG, "getDriverDetails->${e.localizedMessage}")

                    if (e.localizedMessage.equals(AppConstants.Unauthorized)||e.localizedMessage.equals(AppConstants.Forbidden))
                        checkToken(application, ApiCallBack { success ->
                            if (success) getDriverDetails(getPreference(application, AppConstants.TOKEN)!!)
                            else driverDetails?.value = null
                        })
                    else driverDetails?.value = DriverDetailsResponseModel(errorResponse =  ErrorResponse(e.localizedMessage,true))

                }

            })
    }

    @Suppress("NullSafeMutableLiveData")
    fun logOut(token: String, csrftoken: String) {
        apiClient.logOut(token, csrftoken).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : DisposableSingleObserver<DefaultResponse>() {
                override fun onSuccess(response: DefaultResponse) {
                    myLog(TAG, "logOut->${Gson().toJson(response)}")
                    logout?.value = response
                }

                override fun onError(e: Throwable) {
                    myLog(TAG, "logOut->${e.localizedMessage}")
                    toast(application,"logOut->${e.localizedMessage}")

                    if (e.localizedMessage.equals(AppConstants.Unauthorized)||e.localizedMessage.equals(AppConstants.Forbidden))
                        checkToken(application, ApiCallBack { success ->
                            if (success) logOut(getPreference(application, AppConstants.TOKEN)!!, getPreference(application, AppConstants.csrfTOKEN)!!)
                            else logout?.value = null
                        })
                    else logout?.value = DefaultResponse(e.localizedMessage,0,false)
                }

            })
    }
    @Suppress("NullSafeMutableLiveData")
    fun myTrips(token: String) {
        apiClient.myTripList(token, getCurrentDate()).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : DisposableSingleObserver<MyTripListResponseModel>() {
                override fun onSuccess(response: MyTripListResponseModel) {
                    myLog(TAG, "myTrips->${Gson().toJson(response)}")
                    tripList?.value = response
                }

                override fun onError(e: Throwable) {
                    myLog(TAG, "myTrips->${e.localizedMessage}")

                    if (e.localizedMessage.equals(AppConstants.Unauthorized)||e.localizedMessage.equals(AppConstants.Forbidden))
                        checkToken(application, ApiCallBack { success ->
                            if (success) myTrips(getPreference(application, AppConstants.TOKEN)!!)
                            else tripList?.value = null
                        })
                    else tripList?.value = MyTripListResponseModel(errorResponse =  ErrorResponse(e.localizedMessage,true))
                }

            })
    }
    @Suppress("NullSafeMutableLiveData")
    fun stopsDetails(token: String,stopId: String,routeId: String,bookingDate: String) {
        apiClient.getStopsDetails(token,stopId, routeId, bookingDate).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : DisposableSingleObserver<StopsDetailResponseModel>() {
                override fun onSuccess(response: StopsDetailResponseModel) {
                    myLog(TAG, "stopsDetails->${Gson().toJson(response)}")
                    stopDetails?.value = response
                }

                override fun onError(e: Throwable) {
                    myLog(TAG, "stopsDetails->${e.localizedMessage}")

                    if (e.localizedMessage.equals(AppConstants.Unauthorized)||e.localizedMessage.equals(AppConstants.Forbidden))
                        checkToken(application, ApiCallBack { success ->
                            if (success) stopsDetails(getPreference(application, AppConstants.TOKEN)!!,stopId, routeId, bookingDate)
                            else stopDetails?.value = null
                        })
                    else stopDetails?.value = StopsDetailResponseModel(errorResponse =  ErrorResponse(e.localizedMessage,true))

                }

            })
    }
    @Suppress("NullSafeMutableLiveData")
    fun getPassengersDetails(
        token: String,
        stopId: String,
        routeId: String,
        bookingDate: String,
        bookings: ArrayList<String>
    ) {
        apiClient.getPassengersDetails(token,stopId, routeId, bookingDate,bookings).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : DisposableSingleObserver<PassengerDetailsResponseModel>() {
                override fun onSuccess(response: PassengerDetailsResponseModel) {
                    myLog(TAG, "getPassengersDetails->${Gson().toJson(response)}")
                    passengerDetails?.value = response
                }

                override fun onError(e: Throwable) {
                    myLog(TAG, "getPassengersDetails->${e.localizedMessage}")

                    if (e.localizedMessage.equals(AppConstants.Unauthorized)||e.localizedMessage.equals(AppConstants.Forbidden))
                        checkToken(application, ApiCallBack { success ->
                            if (success) getPassengersDetails(
                                getPreference(application, AppConstants.TOKEN)!!,
                                stopId,
                                routeId,
                                bookingDate,
                                bookings
                            )
                            else passengerDetails?.value = null
                        })
                    else passengerDetails?.value = PassengerDetailsResponseModel(errorResponse =  ErrorResponse(e.localizedMessage,true))

                }

            })
    }
    @Suppress("NullSafeMutableLiveData")
    fun tokenRefresh(phone: String, csrfToken: String, onModel: String) {
        apiClient.refreshToken(phone, csrfToken, onModel)!!.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : DisposableSingleObserver<RefreshTokenModel>() {
                override fun onSuccess(response: RefreshTokenModel) {
                    myLog(TAG, "tokenRefresh->${Gson().toJson(response)}")
                    refreshToken?.value = response
                }

                override fun onError(e: Throwable) {
                    myLog(TAG, "tokenRefresh->${e.localizedMessage}")
                    if (e.localizedMessage.equals(AppConstants.Unauthorized)||e.localizedMessage.equals(AppConstants.Forbidden))
                        checkToken(application, ApiCallBack { success ->
                            if (success) tokenRefresh(phone,getPreference(application, AppConstants.csrfTOKEN)!!,onModel)
                            else refreshToken?.value = null
                        })
                    else refreshToken?.value = RefreshTokenModel(errorResponse =  ErrorResponse(e.localizedMessage,true))

                }

            })
    }

    @Suppress("NullSafeMutableLiveData")
    fun updateTicketStatus(token: String, pnrNo: String, travelStatus: String) {
        apiClient.updatePassengerStatus(token, pnrNo, travelStatus)!!.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : DisposableSingleObserver<DefaultResponse>() {
                override fun onSuccess(response: DefaultResponse) {
                    myLog(TAG, "updateTicketStatus->${Gson().toJson(response)}")
                    scannedTicketStatus?.value = response
                }

                override fun onError(e: Throwable) {
                    myLog(TAG, "updateTicketStatus->${e.localizedMessage}")
                    if (e.localizedMessage.equals(AppConstants.Unauthorized)||e.localizedMessage.equals(AppConstants.Forbidden))
                        checkToken(application, ApiCallBack { success ->
                            if (success) updateTicketStatus(getPreference(application, AppConstants.TOKEN)!!,pnrNo, travelStatus)
                            else scannedTicketStatus?.value = null
                        })
                    else scannedTicketStatus?.value = DefaultResponse(e.localizedMessage,0,false)


                }

            })
    }
    @Suppress("NullSafeMutableLiveData")
    fun updateDriverLocationStatus(token: String, tripId: String, driverStatus: String, address: String,angle: String, lat: String, lng: String) {
        apiClient.updateDriverLocationStatus(token, tripId, driverStatus,address,angle,lat,lng)!!.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : DisposableSingleObserver<DefaultResponse>() {
                override fun onSuccess(response: DefaultResponse) {
                    myLog(TAG, "updateDriverLocationStatus->${Gson().toJson(response)}")
                    driverLocationUpdateStatus?.value = response
                }

                override fun onError(e: Throwable) {
                    myLog(TAG, "updateDriverLocationStatus->${e.localizedMessage}")
                    if (e.localizedMessage.equals(AppConstants.Unauthorized)||e.localizedMessage.equals(AppConstants.Forbidden))
                        checkToken(application, ApiCallBack { success ->
                            if (success) updateDriverLocationStatus(getPreference(application, AppConstants.TOKEN)!!,tripId, driverStatus,address,angle,lat,lng)
                            else driverLocationUpdateStatus?.value = null
                        })
                    else driverLocationUpdateStatus?.value = DefaultResponse(e.localizedMessage,0,false)
                }

            })
    }

    @Suppress("NullSafeMutableLiveData")
    fun getNotificationData(token: String,perPage: Int,page: Int) {
        apiClient.getNotifications(token,perPage,
                page)!!.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : DisposableSingleObserver<NotificationResponseModel>() {
                override fun onSuccess(response: NotificationResponseModel) {
                    myLog(TAG, "getNotificationData->${Gson().toJson(response)}")
                    notificationListData?.value = response
                }

                override fun onError(e: Throwable) {
                    myLog(TAG, "getNotificationData->${e.localizedMessage}")
                    if (e.localizedMessage.equals(AppConstants.Unauthorized)||e.localizedMessage.equals(AppConstants.Forbidden))
                        checkToken(application, ApiCallBack { success ->
                            if (success) getNotificationData(getPreference(application, AppConstants.TOKEN)!!,perPage, page)
                            else notificationListData?.value = null
                        })
                    else notificationListData?.value = NotificationResponseModel(errorResponse =  ErrorResponse(e.localizedMessage,true))

                }

            })
    }
    @Suppress("NullSafeMutableLiveData")
    fun updateNotificationStatus(token: String, id: String, readStatus: String) {
        apiClient.updateNotificationStatus(token, id, readStatus)!!.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : DisposableSingleObserver<DefaultResponse>() {
                override fun onSuccess(response: DefaultResponse) {
                    myLog(TAG, "updateNotificationStatus->${Gson().toJson(response)}")
                    updateNotificationStatus?.value = response
                }

                override fun onError(e: Throwable) {
                    myLog(TAG, "updateNotificationStatus->${e.localizedMessage}")
                    if (e.localizedMessage.equals(AppConstants.Unauthorized)||e.localizedMessage.equals(AppConstants.Forbidden))
                        checkToken(application, ApiCallBack { success ->
                            if (success) updateNotificationStatus(getPreference(application, AppConstants.TOKEN)!!,id,readStatus)
                            else updateNotificationStatus?.value = null
                        })
                    else updateNotificationStatus?.value = DefaultResponse(e.localizedMessage,0,false)

                }

            })
    }
    @Suppress("NullSafeMutableLiveData")
    fun updateTrackingStatus(token: String, id: String, trip_status: String, lat: String, lng: String,angle:String) {
        apiClient.updateTrackingStatus(token, id, trip_status, lat, lng,angle)!!.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : DisposableSingleObserver<TrackingStatusResponse>() {
                override fun onSuccess(response: TrackingStatusResponse) {
                    myLog(TAG, "updateTrackingStatus->${Gson().toJson(response)}")
                    updateTrackingData?.value = response
                }

                override fun onError(e: Throwable) {
                    myLog(TAG, "updateTrackingStatus->${e.localizedMessage}")
                    if (e.localizedMessage.equals(AppConstants.Unauthorized)||e.localizedMessage.equals(AppConstants.Forbidden))
                        checkToken(application, ApiCallBack { success ->
                            if (success) updateTrackingStatus(getPreference(application, AppConstants.TOKEN)!!,id,trip_status,lat,lng,angle)
                            else updateTrackingData?.value = null
                        })
                    else updateTrackingData?.value = TrackingStatusResponse(errorResponse =  ErrorResponse(e.localizedMessage,true))


                }

            })
    }

    @Suppress("NullSafeMutableLiveData")
    fun getConfigSettings(token: String) {
        apiClient.getConfigSettings(token)!!.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : DisposableSingleObserver<CommonDataResponse>() {
                override fun onSuccess(response: CommonDataResponse) {
                    myLog(TAG, "getConfigSettings->${Gson().toJson(response)}")
                    configSettings?.value = response
                }

                override fun onError(e: Throwable) {
                    myLog(TAG, "getConfigSettings->${e.localizedMessage}")
                    if (e.localizedMessage.equals(AppConstants.Unauthorized)||e.localizedMessage.equals(AppConstants.Forbidden))
                        checkToken(application, ApiCallBack { success ->
                            if (success) getConfigSettings(getPreference(application, AppConstants.TOKEN)!!)
                            else configSettings?.value = null
                        })
                    else configSettings?.value = CommonDataResponse(errorResponse =  ErrorResponse(e.localizedMessage,true))

                }

            })
    }
    @Suppress("NullSafeMutableLiveData")
    fun updateLanguage(token: String,language: String) {
        apiClient.updateLanguage(token,language)!!.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : DisposableSingleObserver<DefaultResponse>() {
                override fun onSuccess(response: DefaultResponse) {
                    myLog(TAG, "updateLanguage->${Gson().toJson(response)}")
                    updateLanguage?.value = response
                }

                override fun onError(e: Throwable) {
                    myLog(TAG, "updateLanguage->${e.localizedMessage}")
                    if (e.localizedMessage.equals(AppConstants.Unauthorized)||e.localizedMessage.equals(AppConstants.Forbidden))
                        checkToken(application, ApiCallBack { success ->
                            if (success) updateLanguage(getPreference(application, AppConstants.TOKEN)!!,language)
                            else updateLanguage?.value = null
                        })
                    else updateLanguage?.value = DefaultResponse(e.localizedMessage,0,false)

                }

            })
    }
    @Suppress("NullSafeMutableLiveData")
    fun helpSupport(token: String?, contact: String, helpemail: String, description: String) {
        apiClient.getHelp(token, contact, helpemail, description)!!.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : DisposableSingleObserver<DefaultResponse>() {
                override fun onSuccess(response: DefaultResponse) {
                    myLog(TAG, "helpAndSupport->${Gson().toJson(response)}")
                    helpAndSupport?.value = response
                }

                override fun onError(e: Throwable) {
                    myLog(TAG, "helpAndSupport->${e.localizedMessage}")
                    if (e.localizedMessage.equals(AppConstants.Unauthorized)||e.localizedMessage.equals(AppConstants.Forbidden))
                        checkToken(application, ApiCallBack { success ->
                            if (success) helpSupport(getPreference(application, AppConstants.TOKEN)!!,contact,helpemail,description)
                            else helpAndSupport?.value = null
                        })
                    else helpAndSupport?.value = DefaultResponse(e.localizedMessage,0,false)

                }

            })
    }

    fun checkToken(mContext: Context, apiCallBack: ApiCallBack) {

        apiClient.refreshToken(
            getPreference(mContext, AppConstants.PHONE_NO),
            getPreference(mContext, AppConstants.csrfTOKEN),
            AppConstants.onModel
        )!!
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : DisposableSingleObserver<RefreshTokenModel?>() {
                override fun onSuccess(response: RefreshTokenModel) {
                    myLog("CHECK_TOKEN", "onSuccess: response=" + Gson().toJson(response))
                    if (response.status!!) {
                        savePreference(mContext, AppConstants.csrfTOKEN, response.data?.csrfToken)
                        savePreference(mContext, AppConstants.TOKEN, "Bearer " + response.data?.token)

                        apiCallBack.onResponse(true)

                    } else apiCallBack.onResponse(false)
                }

                override fun onError(e: Throwable) {
                    myLog("CHECK_TOKEN", "onError: checkToken Error=" + e.localizedMessage)
                    apiCallBack.onResponse(false)
                }
            })

    }


}