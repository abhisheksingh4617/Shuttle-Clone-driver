package com.shuttleclone.driver.Model

import com.google.gson.annotations.SerializedName

data class PassengerDetailsResponseModel(

    @field:SerializedName("data")
    val data: List<PassengerDataItem>? = null,

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("status")
    val status: Boolean? = null,

    @field:SerializedName("error_response")
    val errorResponse: ErrorResponse? = null,
)

data class PassengerDataItem(

    @field:SerializedName("is_drop")
    val isDrop: Boolean? = null,

    @field:SerializedName("user_phone")
    val userPhone: String? = null,

    @field:SerializedName("travel_status")
    val travelStatus: String? = null,

    @field:SerializedName("user_fullname")
    val userFullName: String? = null,

    @field:SerializedName("passengers")
    val subPassengers: List<SubPassengersItem>? = null,

    @field:SerializedName("is_pickup")
    val isPickup: Boolean? = null
)

data class SubPassengersItem(

    @field:SerializedName("seat")
    val seat: String? = null,

    @field:SerializedName("gender")
    val gender: String? = null,

    @field:SerializedName("fullname")
    val fullname: String? = null,

    @field:SerializedName("age")
    val age: String? = null
)
