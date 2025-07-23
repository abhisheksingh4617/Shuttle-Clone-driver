package com.shuttleclone.driver.Model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class TripListResponseModel(

	@field:SerializedName("data")
	val data: TripsData? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Boolean? = null,

	@field:SerializedName("error_response")
	val errorResponse: ErrorResponse? = null,
): Serializable

data class StopsList(

	@field:SerializedName("arrival_time")
	val arrivalTime: String? = null,

	@field:SerializedName("lng")
	val lng: Double? = null,

	@field:SerializedName("passenger_count")
	val passengerCount: String? = null,

//	@field:SerializedName("files")
//	val files: List<String>? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: String? = null,

	@field:SerializedName("departure_time")
	val departureTime: String? = null,

	@field:SerializedName("lat")
	val lat: Double? = null
): Serializable

data class TripsData(

	@field:SerializedName("trip_status")
	val tripStatus: String? = null,

	@field:SerializedName("assignId")
	val assignId: String? = null,

	@field:SerializedName("assistants")
	val assistants: List<AssistantsList>? = null,

	@field:SerializedName("ticket_end_at")
	val ticketEndAt: String? = null,

	@field:SerializedName("passenger_total")
	val passengerTotal: String? = null,

	@field:SerializedName("bus_model_no")
	val busModelNo: String? = null,

	@field:SerializedName("bus_reg_no")
	val busRegNo: String? = null,

	@field:SerializedName("date")
	val createdDate: String? = null,

	@field:SerializedName("time")
	val createdTime: String? = null,

	@field:SerializedName("ticket_start_at")
	val ticketStartAt: String? = null,

	@field:SerializedName("ticket_total_seat_count")
	val ticketTotalSeatCount: String? = null,

	@field:SerializedName("ticket_total_seat_remain")
	val ticketTotalSeatRemain: String? = null,

	@field:SerializedName("routeId")
	val routeId: String? = null,

	@field:SerializedName("route_name")
	val routeName: String? = null,

	@field:SerializedName("ticket_name")
	val ticketName: String? = null,

	@field:SerializedName("stops")
	val stops: List<StopsList>? = null,

	@field:SerializedName("ticketId")
	val ticketId: String? = null,

	@field:SerializedName("status")
	val status: Boolean? = null,

	@field:SerializedName("ticket_total_seat_booked")
	val ticketTotalSeatBooked: String? = null
) : Serializable

data class AssistantsList(

	@field:SerializedName("firstname")
	val firstname: String? = null,

	@field:SerializedName("phone")
	val phone: String? = null,

	@field:SerializedName("_id")
	val id: String? = null,

	@field:SerializedName("lastname")
	val lastname: String? = null
): Serializable
