package com.shuttleclone.driver.Model

import com.google.gson.annotations.SerializedName

data class StopsDetailResponseModel(

	@field:SerializedName("data")
	val data: List<DataItem>? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Boolean? = null,

	@field:SerializedName("error_response")
	val errorResponse: ErrorResponse? = null,
)

data class PassengerDetailsItem(

	@field:SerializedName("seat")
	val seat: String? = null,

	@field:SerializedName("gender")
	val gender: String? = null,

	@field:SerializedName("travel_status")
	val travelStatus: String? = null,

	@field:SerializedName("customer_phone")
	val customerPhone: String? = null,

	@field:SerializedName("fullname")
	val fullname: String? = null,

	@field:SerializedName("userId")
	val userId: String? = null,

	@field:SerializedName("age")
	val age: String? = null
)

data class DataItem(

	@field:SerializedName("passengerdetails")
	val passengerdetails: List<PassengerDetailsItem>? = null,

	@field:SerializedName("payment_status")
	val paymentStatus: String? = null,

	@field:SerializedName("id")
	val id: String? = null,

	@field:SerializedName("pnr_no")
	val pnrNo: String? = null,

	@field:SerializedName("travel_status")
	val travelStatus: String? = null,

	@field:SerializedName("final_total_fare")
	val finalTotalFare: String? = null
)
