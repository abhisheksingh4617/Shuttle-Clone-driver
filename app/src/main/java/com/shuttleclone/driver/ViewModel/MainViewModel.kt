package com.shuttleclone.driver.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.shuttleclone.driver.Model.*
import com.shuttleclone.driver.RetrofitRepository.MainRepo

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val repo = MainRepo(application)

    fun driverLogin(phoneNo: String,countryCode: String,countryDetails: String): MutableLiveData<DriverLoginResponseModel?> {
        repo.driverLogin(phoneNo,countryCode,countryDetails)
        return repo.loginData
    }

    fun verifyOTP(token: String,deviceToken:String, otp: Int, isMobileVerified: Boolean): MutableLiveData<DriverVerifyResponseModel?> {
        repo.verifyOTP(token,deviceToken,otp,isMobileVerified)
        return repo.otpVerify
    }

    fun resendOtp(token: String, phoneNo: String, countryCode: String, countryDetails: String): MutableLiveData<DefaultResponse?> {
        repo.resendOtp(token,phoneNo,countryCode,countryDetails)
        return repo.reSendOtp
    }

    fun myTrips(token: String): MutableLiveData<MyTripListResponseModel?> {
        repo.myTrips(token)
        return repo.tripList
    }

    fun stopsDetails(token: String, stopId: String, routeId: String, bookingDate: String): MutableLiveData<StopsDetailResponseModel?> {
        repo.stopsDetails(token,stopId, routeId, bookingDate)
        return repo.stopDetails
    }

    fun getPassengersDetails(
        token: String,
        stopId: String,
        routeId: String,
        bookingDate: String,
        bookings: ArrayList<String>
    ): MutableLiveData<PassengerDetailsResponseModel?> {
        repo.getPassengersDetails(token,stopId, routeId, bookingDate,bookings)
        return repo.passengerDetails
    }

    fun getDriverDetails(token: String): MutableLiveData<DriverDetailsResponseModel?> {
        repo.getDriverDetails(token)
        return repo.driverDetails
    }

    fun tokenRefresh(phone: String, csrfToken: String, onModel: String): MutableLiveData<RefreshTokenModel?> {
        repo.tokenRefresh(phone, csrfToken, onModel)
        return repo.refreshToken
    }

    fun updateTicketStatus(token: String, pnrno: String,travelStatus:String): MutableLiveData<DefaultResponse?> {
        repo.updateTicketStatus(token,pnrno,travelStatus)
        return repo.scannedTicketStatus
    }

    fun getNotificationData(token: String,perPage: Int,page: Int): MutableLiveData<NotificationResponseModel?> {
        repo.getNotificationData(token,perPage, page)
        return repo.notificationListData
    }

    fun updateNotificationStatus(token: String, id: String,readStatus:String): MutableLiveData<DefaultResponse?> {
        repo.updateNotificationStatus(token,id,readStatus)
        return repo.updateNotificationStatus
    }

    fun updateTrackingStatus(token: String, id: String,trip_status:String, lat: String,lng:String,angle:String): MutableLiveData<TrackingStatusResponse?> {
        repo.updateTrackingStatus(token,id,trip_status,lat,lng,angle)
        return repo.updateTrackingData
    }


    fun logOut(token: String, csrfToken: String): MutableLiveData<DefaultResponse?> {
        repo.logOut(token, csrfToken)
        return repo.logout
    }

    fun getConfigSettings(token: String): MutableLiveData<CommonDataResponse?> {
        repo.getConfigSettings(token)
        return repo.configSettings
    }

    fun helpSupport(token: String?, contact: String, helpemail: String, description: String): MutableLiveData<DefaultResponse?> {
        repo.helpSupport(token,contact,helpemail,description)
        return repo.helpAndSupport
    }

    fun updateLanguage(token: String, language: String): MutableLiveData<DefaultResponse?> {
        repo.updateLanguage(token,language)
        return repo.updateLanguage
    }

}