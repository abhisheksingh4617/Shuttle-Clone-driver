package com.shuttleclone.driver.Model

import com.google.gson.annotations.SerializedName

data class DeviceInfoDataModel(

	@field:SerializedName("os")
	val os: String? = null,

	@field:SerializedName("brand")
	val brand: String? = null,

	@field:SerializedName("model_no")
	val modelNo: String? = null,

	@field:SerializedName("version")
	val version: String? = null
)
