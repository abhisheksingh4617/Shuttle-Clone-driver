package com.shuttleclone.driver.MyApp

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
class MyApplication : Application() {

    val TAG="MyApplication"
    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this)
        instance = this
        setAppContext(applicationContext)

    }


    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    private fun setAppContext(mAppContext: Context) {
        appContext = mAppContext
    }

    companion object {
        var appContext: Context? = null
        var instance: MyApplication? = null
            private set
    }
}