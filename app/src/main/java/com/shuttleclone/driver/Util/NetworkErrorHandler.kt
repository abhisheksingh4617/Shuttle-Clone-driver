package com.shuttleclone.driver.Util

import android.content.Context
import com.shuttleclone.driver.R
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.io.IOException

/**
 * Network Error Handler
 * Converts technical error messages to user-friendly messages
 * Hides IP addresses and technical details from users
 */
object NetworkErrorHandler {

    /**
     * Get user-friendly error message from exception
     * @param context Context for getting string resources
     * @param error The exception that occurred
     * @return User-friendly error message
     */
    fun getErrorMessage(context: Context, error: Throwable?): String {
        return when (error) {
            is UnknownHostException -> {
                // No internet or DNS resolution failed
                context.getString(R.string.error_no_internet)
            }
            is SocketTimeoutException -> {
                // Request timeout
                context.getString(R.string.error_timeout)
            }
            is IOException -> {
                // Network error
                if (error.message?.contains("Unable to resolve host") == true ||
                    error.message?.contains("Failed to connect") == true ||
                    error.message?.contains("No internet connection") == true ||
                    error.message?.contains("Connection failed") == true) {
                    context.getString(R.string.error_no_internet)
                } else if (error.message?.contains("timeout") == true) {
                    context.getString(R.string.error_timeout)
                } else {
                    context.getString(R.string.error_network)
                }
            }
            else -> {
                // Generic error - don't expose technical details
                val message = error?.message ?: ""
                
                // Hide IP addresses and technical URLs
                if (message.contains("51.21.185.70") || 
                    message.contains("Failed to connect to") ||
                    message.contains("Unable to resolve host") ||
                    message.contains("No internet connection") ||
                    message.contains("Connection failed")) {
                    context.getString(R.string.error_no_internet)
                } else {
                    context.getString(R.string.error_something_wrong)
                }
            }
        }
    }

    /**
     * Check if error is network related
     */
    fun isNetworkError(error: Throwable?): Boolean {
        return error is UnknownHostException ||
               error is SocketTimeoutException ||
               error is IOException ||
               error?.message?.contains("Failed to connect") == true ||
               error?.message?.contains("Unable to resolve host") == true ||
               error?.message?.contains("No internet connection") == true
    }

    /**
     * Get sanitized error message for logging (removes sensitive info)
     */
    fun getSanitizedLogMessage(error: Throwable?): String {
        val message = error?.message ?: "Unknown error"
        // Replace IP addresses with placeholder
        return message
            .replace(Regex("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"), "SERVER_IP")
            .replace(Regex("http://[^\\s]+"), "SERVER_URL")
            .replace(Regex("https://[^\\s]+"), "SERVER_URL")
    }
}
