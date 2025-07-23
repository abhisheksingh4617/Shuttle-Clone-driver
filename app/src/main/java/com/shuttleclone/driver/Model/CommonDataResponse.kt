package com.shuttleclone.driver.Model

import com.google.gson.annotations.SerializedName

data class CommonDataResponse(

	@field:SerializedName("data")
	val data: CommonData? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Boolean? = null,

	@field:SerializedName("error_response")
	val errorResponse: ErrorResponse? = null,
)

data class PaymentsItem(

	@field:SerializedName("mode")
	val mode: String? = null,

	@field:SerializedName("callback_url")
	val callbackUrl: String? = null,

	@field:SerializedName("webhook_url")
	val webhookUrl: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("secret")
	val secret: String? = null,

    @field:SerializedName("currency")
	val currency: String? = null,

    @field:SerializedName("is_enabled")
	val isEnabled: String? = null,

	@field:SerializedName("key")
	val key: String? = null
)

data class CommonData(

	@field:SerializedName("privacy_policy_url")
	val privacyPolicyUrl: String? = null,

	@field:SerializedName("referral_policy")
	val referralPolicy: String? = null,

	@field:SerializedName("apple_key_id")
	val appleKeyId: String? = null,

	@field:SerializedName("timezone")
	val timezone: String? = null,

	@field:SerializedName("fee")
	val fee: String? = null,

	@field:SerializedName("payments")
	val payments: PaymentsItem? = null,

	@field:SerializedName("payment_gateway_type")
	val paymentGatewayType: String? = null,

	@field:SerializedName("apple_key")
	val appleKey: String? = null,

	@field:SerializedName("otp_validation_via")
	val otpValidationVia: Boolean? = null,

	@field:SerializedName("api_base_url")
	val apiBaseUrl: String? = null,

	@field:SerializedName("terms")
	val terms: String? = null,

	@field:SerializedName("background_location_update_interval")
	val backgroundLocationUpdateInterval: Long? = null,

	@field:SerializedName("logo")
	val logo: String? = null,

	@field:SerializedName("refund_amount")
	val refundAmount: Int? = null,

	@field:SerializedName("firebase_driver_secret_key")
	val firebaseDriverSecretKey: String? = null,

	@field:SerializedName("google_key")
	val googleKey: String? = null,

	@field:SerializedName("email")
	val email: String? = null,

	@field:SerializedName("driver_online_location_update_interval")
	val driverOnlineLocationUpdateInterval: Long? = null,

	@field:SerializedName("address")
	val address: String? = null,

	@field:SerializedName("time_format")
	val timeFormat: String? = null,

	@field:SerializedName("refund_type")
	val refundType: String? = null,

	@field:SerializedName("refund_contents")
	val refundContents: String? = null,

	@field:SerializedName("tax")
	val tax: String? = null,

	@field:SerializedName("firebase_customer_secret_key")
	val firebaseCustomerSecretKey: String? = null,

	@field:SerializedName("term_conditions_url")
	val termConditionsUrl: String? = null,

	@field:SerializedName("phone")
	val phone: String? = null,

	@field:SerializedName("apple_team_id")
	val appleTeamId: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("date_format")
	val dateFormat: String? = null,

	@field:SerializedName("default_currency")
	val defaultCurrency: String? = null,

	@field:SerializedName("default_country")
	val defaultCountry: String? = null
)
