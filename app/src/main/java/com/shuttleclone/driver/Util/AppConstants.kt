package com.shuttleclone.driver.Util

import java.text.SimpleDateFormat
import java.util.Locale

object AppConstants {

    /*variable declaration*/
    const val NA = "N/A"

    // 🔹 Local PC server ka URL (Emulator ke liye)
    const val BASE_URL =
        "http://51.21.185.70:5000/api/" //YOU BASE URL// chal raha hai to emulator me 10.0.2.2 use hoga
   // const val BASE_URL = "http://13.48.236.133/"

    // Agar future me mobile device (real phone) pe test karna ho to IP wapas change kar lena
    // Example: const val BASE_URL = "http://192.168.137.1:8001/"

    const val COUNTRY_CODE = 91

    //SharedPreferences
    var APP_NAME = "Shuttle Clone"
    var CHANNEL_NAME = "Bus Shuttle Clone"
    var TOKEN = "TOKEN"
    var ASSIGNED_ID = "ASSIGNED_ID"
    var csrfTOKEN = "csrfTOKEN"
    var BASEURL = "BASEURL"
    var onModel = "Driver"
    var PHONE_NO = "PHONE_NO"
    var IsDriverLogIn = "IsDriverLogIn"
    var DEVICE_TOKEN = "DEVICE_TOKEN"
    var IsUserUpdatingFirstTime = "IsUserUpdatingFirstTime"
    var FirstTimeUser = "FirstTimeUser"
    var USER_REG_COMPLETE = "NO"
    var Unauthorized = "HTTP 401 Unauthorized"
    var Forbidden = "HTTP 403 Forbidden"
    var FRG_SERVICE_NF_ID = 21031
    var MIN_DIST_FOR_LOCATION_UPDATE = 0.0
    var DRIVER_LATITUDE = "DRIVER_LATITUDE"
    var DRIVER_LONGITUDE = "DRIVER_LONGITUDE"
    var DRIVER_ANGLE = "DRIVER_ANGLE"
    var IS_BOOKING_ASSIGNED = "IS_BOOKING_ASSIGNED"
    var IS_TRIP_STARTED = "IS_TRIP_STARTED"
    // Flag to indicate that at least one passenger has been onboarded for current trip
    var IS_PASSENGER_ONBOARDED = "IS_PASSENGER_ONBOARDED"
    var DEVICE_TYPE = 1 //1 for Android and 2 for iOS
    var COMMON_DATA = "COMMON_DATA"
    var DRIVER_STATUS = "DRIVER_STATUS"
    var Driver_Offline = "OFFLINE"
    var Driver_Online = "ONLINE"
    var Driver_Tracking = "TRACK"
    var TRIP_STATUS = "TRIP_STATUS"
    var LANGUAGE = "LANGUAGE"

    /*Date format*/
    object DateFormat {
        var dd_MM = "dd-MMM"
        var dd_MM_yyyy = "dd - MMM - yyyy"
        val DAY_MONTH_FORMATTER = SimpleDateFormat(dd_MM, Locale.getDefault())
        val DAY_MONTH_YEAR_FORMATTER = SimpleDateFormat(dd_MM_yyyy, Locale.getDefault())
    }

    /*intent data*/
    object intentdata {
        var CARDDETAIL = "carddetail"
        var TRAVELLERNAME = "TravellerName"
        var TYPECOACH = "typecoach"
        var PRICE = "price"
        var HOLD = "hold"
        var PACKAGE = "package"
        var OFFER = "offer"
        var TRIP_KEY = "trip_key"
        var SEARCH_BUS = "search_bus"
        var PACKAGE_NAME = "package_name"
        var CARDFLAG = "cardflag"
    }
}
