package com.shuttleclone.driver.RetrofitRepository

import com.shuttleclone.driver.Model.*
import com.google.gson.JsonObject
import io.reactivex.Single
import retrofit2.http.*

interface ApiCalls {

    // 🔹 TEST LOGIN (Postman wala simple /login endpoint)
    @POST("login")
    fun testLogin(
        @Body requestBody: JsonObject
    ): Single<JsonObject>

    // Login driver (original method)
    @FormUrlEncoded
    @POST("drivers/login")
    fun loginDriver(
        @Field("phone") phone: String,
        @Field("country_code") countryCode: String,
        @Field("country_details") countryDetails: String,
        @Field("language") language: String
    ): Single<DriverLoginResponseModel>

    //verify OTP
    @FormUrlEncoded
    @POST("drivers/verify")
    fun verifyOtp(
        @Header("Authorization") token: String?,
        @Field("device_token") device_token: String,
        @Field("device_type") deviceType: Int,
        @Field("otp") otp: Int,
        @Field("is_mobile_verified") is_mobile_verified: Boolean,
        @Field("device_info") deviceInfoDataModel: String
    ): Single<DriverVerifyResponseModel>

    //Refresh token
    @FormUrlEncoded
    @POST("drivers/refresh-token")
    fun refreshToken(
        @Field("phone") phone: String?,
        @Field("csrfToken") csrfToken: String?,
        @Field("onModel") onModel: String?
    ): Single<RefreshTokenModel?>?

    @GET("drivers/getdriver")
    fun getProfileDetails(
        @Header("Authorization") token: String
    ): Single<DriverDetailsResponseModel>

    @FormUrlEncoded
    @POST("drivers/help")
    fun getHelp(
        @Header("Authorization") token: String?,
        @Field("contact") contact: String?,
        @Field("helpemail") helpemail: String?,
        @Field("description") description: String?
    ): Single<DefaultResponse>

    @FormUrlEncoded
    @PUT("drivers/re-send")
    fun resendOtp(
        @Header("Authorization") token: String,
        @Field("phone") phone: String,
        @Field("country_code") countryCode: String,
        @Field("country_details") countryDetails: String
    ): Single<DefaultResponse>

    @GET("drivers/my-trips")
    fun myTripList(
        @Header("Authorization") token: String,
        @Query("current_date") current_date: String
    ): Single<MyTripListResponseModel>

    @FormUrlEncoded
    @POST("drivers/get-stop-details")
    fun getStopsDetails(
        @Header("Authorization") token: String,
        @Field("stopId") stopId: String,
        @Field("routeId") routeId: String,
        @Field("booking_date") bookingDate: String
    ): Single<StopsDetailResponseModel>

    @FormUrlEncoded
    @POST("drivers/passenger-details")
    fun getPassengersDetails(
        @Header("Authorization") token: String,
        @Field("stop_id") stopId: String,
        @Field("route_id") routeId: String,
        @Field("booking_date") bookingDate: String,
        @Field("booking_ids[]") booking_ids: ArrayList<String>,
    ): Single<PassengerDetailsResponseModel>

    @GET("drivers/notifications")
    fun getNotifications(
        @Header("Authorization") token: String?,
        @Query("perPage") perPage: Int?,
        @Query("page") page: Int?
    ): Single<NotificationResponseModel>

    @POST("drivers/notifications/{id}/{readStatus}")
    fun updateNotificationStatus(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Path("readStatus") readStatus: String
    ): Single<DefaultResponse>

    @FormUrlEncoded
    @POST("drivers/update-assign-status/{id}")
    fun updateTrackingStatus(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Field("trip_status") trip_status: String,
        @Field("lat") lat: String,
        @Field("lng") lng: String,
        @Field("angle") angle: String
    ): Single<TrackingStatusResponse>

    @FormUrlEncoded
    @POST("drivers/update-booking-status")
    fun updatePassengerStatus(
        @Header("Authorization") token: String,
        @Field("pnr_no") pnr_no: String,
        @Field("travel_status") travel_status: String
    ): Single<DefaultResponse>

    @FormUrlEncoded
    @POST("drivers/update-location")
    fun updateDriverLocationStatus(
        @Header("Authorization") token: String,
        @Field("tripId") tripId: String,
        @Field("driver_status") driver_status: String,
        @Field("address") address: String,
        @Field("angle") angle: String,
        @Field("lat") lat: String,
        @Field("lng") lng: String
    ): Single<DefaultResponse>

    @PUT("drivers/logout")
    fun logOut(
        @Header("Authorization") token: String,
        @Header("csrf-token") ctoken: String
    ): Single<DefaultResponse>

    @GET("settings/commondata")
    fun getConfigSettings(
        @Header("Authorization") token: String
    ): Single<CommonDataResponse>

    @FormUrlEncoded
    @POST("drivers/update-language")
    fun updateLanguage(
        @Header("Authorization") token: String,
        @Field("language") language: String
    ): Single<DefaultResponse>
}
