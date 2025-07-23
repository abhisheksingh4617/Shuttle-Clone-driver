package com.shuttleclone.driver.Model

import com.google.gson.annotations.SerializedName

data class DriverDetailsResponseModel(

	@field:SerializedName("data")
	val data: ProfileData? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Boolean? = null,

	@field:SerializedName("error_response")
	val errorResponse: ErrorResponse? = null,
)

data class ProfileData(

	@field:SerializedName("createdAt")
	val createdAt: String? = null,

	@field:SerializedName("firstname")
	val firstname: String? = null,

	@field:SerializedName("phone")
	val phone: String? = null,

	@field:SerializedName("document_adhar_card")
	val documentAdharCard: String? = null,

	@field:SerializedName("document_police_vertification")
	val documentPoliceVertification: String? = null,

	@field:SerializedName("id")
	val id: String? = null,

	@field:SerializedName("email")
	val email: String? = null,

	@field:SerializedName("picture")
	val picture: String? = null,

	@field:SerializedName("document_licence")
	val documentLicence: String? = null,

	@field:SerializedName("lastname")
	val lastname: String? = null,

	@field:SerializedName("status")
	val status: Boolean? = null
)
