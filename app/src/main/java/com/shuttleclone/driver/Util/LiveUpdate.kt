package com.shuttleclone.driver.Util

import android.location.Location
import androidx.lifecycle.MutableLiveData
import com.shuttleclone.driver.Model.DefaultResponse

object LiveUpdate {

    val updateLocation : MutableLiveData<Location> by lazy {
        MutableLiveData<Location>()
    }

    val updateTrackStatus : MutableLiveData<DefaultResponse> by lazy {
        MutableLiveData<DefaultResponse>()
    }
}