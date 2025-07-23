package com.shuttleclone.driver.Model

import com.google.gson.annotations.SerializedName

data class DefaultResponse(
    @SerializedName("message")
    val message: String? = null,

    @SerializedName("otp")
    val otp: Int? = null,

    @SerializedName("status")
    val isStatus: Boolean = false
)