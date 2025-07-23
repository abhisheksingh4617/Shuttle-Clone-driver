package com.shuttleclone.driver.Model

import com.google.gson.annotations.SerializedName

data class NotificationResponseModel(

	@field:SerializedName("data")
	val data: List<NotificationsDataItem>? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Boolean? = null,

	@field:SerializedName("error_response")
	val errorResponse: ErrorResponse? = null,
)

data class NotificationsDataItem(

	@field:SerializedName("created_at")
	val createdAt: String? = null,

	@field:SerializedName("title")
	val title: String? = null,

	@field:SerializedName("type")
	val type: String? = null,

	@field:SerializedName("content")
	val content: String? = null
)
