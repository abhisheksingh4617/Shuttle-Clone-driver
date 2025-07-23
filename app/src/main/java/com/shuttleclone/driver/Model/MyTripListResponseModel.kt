package com.shuttleclone.driver.Model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class MyTripListResponseModel(

	@field:SerializedName("data")
	val data: List<TripsDataItem>? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Boolean? = null,

	@field:SerializedName("error_response")
	val errorResponse: ErrorResponse? = null,
): Serializable

data class Assistants(

	@field:SerializedName("country_code")
	val countryCode: String? = null,

	@field:SerializedName("firstname")
	val firstname: String? = null,

	@field:SerializedName("phone")
	val phone: String? = null,

	@field:SerializedName("email")
	val email: String? = null,

	@field:SerializedName("picture")
	val picture: String? = null,

	@field:SerializedName("lastname")
	val lastname: String? = null
): Serializable

data class StopsItem(

	@field:SerializedName("pickup_count")
	val pickupCount: Int? = null,

	@field:SerializedName("arrival_time")
	val arrivalTime: String? = null,

	@field:SerializedName("lng")
	val lng: Double? = null,

	@field:SerializedName("drop_count")
	val dropCount: Int? = null,

	@field:SerializedName("stop_name")
	val name: String? = null,

	@field:SerializedName("stop_id")
	val id: String? = null,

	@field:SerializedName("pickup_bookings")
	val pickupBookings: ArrayList<String>? = null,

	@field:SerializedName("drop_bookings")
	val dropBookings: ArrayList<String>? = null,

	@field:SerializedName("departure_time")
	val departureTime: String? = null,

	@field:SerializedName("lat")
	val lat: Double? = null,

	@field:SerializedName("order")
	val order: Int? = null
): Serializable


data class TripsDataItem(

	@field:SerializedName("assistants")
	val assistants: Assistants? = null,

	@field:SerializedName("bus_schedules")
	val routes: List<RoutesItem>? = null,

	@field:SerializedName("trip_status")
	val tripStatus: String? = null,

	@field:SerializedName("route_name")
	val routeName: String? = null,

	@field:SerializedName("assignId")
	val assignId: String? = null,

	@field:SerializedName("status")
	val status: Boolean? = null
): Serializable

data class RoutesItem(

	@field:SerializedName("booking_date")
	val bookingDate: String? = null,

	@field:SerializedName("bus_schedule_id")
	val busScheduleId: String? = null,

	@field:SerializedName("bookings_info")
	val bookingsInfo: BookingsInfo? = null,

	@field:SerializedName("stops")
	val stops: List<StopsItem>? = null,

	@field:SerializedName("time")
	val time: String? = null
): Serializable

data class Bus(

	@field:SerializedName("code")
	val code: String? = null,

	@field:SerializedName("reg_no")
	val regNo: String? = null,

	@field:SerializedName("chassis_no")
	val chassisNo: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("brand")
	val brand: String? = null,

	@field:SerializedName("model_no")
	val modelNo: String? = null
): Serializable

data class BookingsInfo(

	@field:SerializedName("bus")
	val bus: Bus? = null,

	@field:SerializedName("routeId")
	val routeId: String? = null,

	@field:SerializedName("total_seat_left")
	val totalSeatLeft: Int? = null,

	@field:SerializedName("route_name")
	val routeName: String? = null,

	@field:SerializedName("total_bookings")
	val totalBookings: Int? = null,

	@field:SerializedName("total_seats")
	val totalSeats: Int? = null,

	@field:SerializedName("total_passengers")
	val totalPassengers: Int? = null
): Serializable

