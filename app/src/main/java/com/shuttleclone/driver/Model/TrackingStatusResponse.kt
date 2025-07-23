package com.shuttleclone.driver.Model

import com.google.gson.annotations.SerializedName

data class TrackingStatusResponse(

	@field:SerializedName("data")
	val data: Data? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Boolean? = null,

	@field:SerializedName("error_response")
	val errorResponse: ErrorResponse? = null,
)

data class NextStop(

	@field:SerializedName("lng")
	val lng: String? = null,

	@field:SerializedName("title")
	val title: String? = null,

	@field:SerializedName("lat")
	val lat: String? = null
)

data class Data(

	@field:SerializedName("next_stop")
	val nextStop: NextStop? = null,

	@field:SerializedName("trip_status")
	val tripStatus: String? = null,

	@field:SerializedName("lng")
	val lng: Double? = null,

	@field:SerializedName("angle")
	val angle: String? = null,

	@field:SerializedName("lat")
	val lat: Double? = null,

	@field:SerializedName("assignId")
	val assignId: String? = null
)
