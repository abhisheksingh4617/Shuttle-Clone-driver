package com.shuttleclone.driver.Model

import com.google.gson.annotations.SerializedName

data class DriverLoginResponseModel(

	@field:SerializedName("flag")
	val flag: Int? = null,

	@field:SerializedName("baseurl")
	val baseurl: String? = null,

	@field:SerializedName("csrfToken")
	val csrfToken: String? = null,

	@field:SerializedName("otp")
	val otp: Int? = null,

	@field:SerializedName("title")
	val title: String? = null,

	@field:SerializedName("status")
	val status: Boolean? = null,

	@field:SerializedName("token")
	val token: String? = null,

	@field:SerializedName("error_response")
	val errorResponse: ErrorResponse? = null,
)

