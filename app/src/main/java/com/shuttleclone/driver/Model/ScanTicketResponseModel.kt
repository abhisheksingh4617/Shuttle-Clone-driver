package com.shuttleclone.driver.Model

import com.google.gson.annotations.SerializedName

data class ScanTicketResponseModel(

	@field:SerializedName("passengers")
	val passengers: String? = null,

	@field:SerializedName("firstname")
	val firstname: String? = null,

	@field:SerializedName("seat_nos")
	val seatNos: List<String>? = null,

	@field:SerializedName("profile_picture")
	val profilePicture: String? = null,

	@field:SerializedName("pnr_no")
	val pnrNo: String? = null,

	@field:SerializedName("travel_status")
	val travelStatus: String? = null,

	@field:SerializedName("lastname")
	val lastname: String? = null,

	@field:SerializedName("passengerdetails")
	val passengerDetails: List<PassengerDetails>? = null,

	@field:SerializedName("bus_model_no")
	val busModelNo: String? = null,

	@field:SerializedName("phone")
	val phone: String? = null,

	@field:SerializedName("has_return")
	val hasReturn: String? = null,

	@field:SerializedName("bus_name")
	val busName: String? = null,

	@field:SerializedName("payment_method")
	val paymentMethod: String? = null,

	@field:SerializedName("bus_depature_date")
	val busDepatureDate: String? = null,

	@field:SerializedName("time")
	val time: String? = null,

	@field:SerializedName("final_total_fare")
	val finalTotalFare: String? = null
)

data class PassengerDetails(

	@field:SerializedName("seat")
	val seat: String? = null,

	@field:SerializedName("gender")
	val gender: String? = null,

	@field:SerializedName("fullname")
	val fullname: String? = null,

	@field:SerializedName("age")
	val age: String? = null
)
