package com.shuttleclone.driver.Model

import com.google.gson.annotations.SerializedName
data class ErrorResponse (
    @SerializedName("error_message")
    val message: String? = null,
    @SerializedName("is_error")
    val isError:Boolean = false
)

