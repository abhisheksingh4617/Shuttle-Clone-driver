package com.shuttleclone.driver.Util

import android.content.Context
import android.content.res.Resources
import android.os.Build
import java.util.Locale


class LocaleManager {

    /**
     * SharedPreferences Key
     */
    /**
     * set current pref locale
     */
    fun setLocale(mContext: Context): Context {
        if (getPreference(mContext, AppConstants.LANGUAGE).equals(""))
            savePreference(mContext,AppConstants.LANGUAGE,ENGLISH)

        return updateResources(mContext, getPreference(mContext, AppConstants.LANGUAGE).toString())
    }

    /**
     * Set new Locale with context
     */
    fun setNewLocale(mContext: Context, language: String): Context {
        savePreference(mContext, AppConstants.LANGUAGE,language)
        return updateResources(mContext, language)
    }
    /**
     * Get saved Locale from SharedPreferences
     *
     * @param mContext current context
     * @return current locale key by default return english locale
     */
    /**
     * set pref key
     */
    /**
     * update resource
     */
    private fun updateResources(context: Context, language: String): Context {

        //setting new configuration
        val myLocale = Locale(language)
        val res = context.resources
        val dm = res.displayMetrics
        val conf = res.configuration
        conf.locale = myLocale
        conf.setLayoutDirection(myLocale)
        res.updateConfiguration(conf, dm)

        /*val myLocale = Locale(language)
        val res = context.resources
        val configuration = res.configuration
        configuration.locale = myLocale
        res.updateConfiguration(configuration, res.displayMetrics)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(myLocale)
            val localeList = LocaleList(myLocale)
            configuration.setLocales(localeList)
            configuration.setLayoutDirection(myLocale)
            return context.createConfigurationContext(configuration)
        } else {
            configuration.locale = myLocale
            configuration.setLayoutDirection(myLocale)
            res.updateConfiguration(configuration, res.displayMetrics)
        }*/

        return context
    }



    /**
     * get current locale
     */
    fun getLocale(res: Resources): Locale {
        val config = res.configuration
        return if (Build.VERSION.SDK_INT >= 24) config.locales[0] else config.locale
    }

    companion object{
        const val ENGLISH = "en"
        const val ARABIC = "ar"
    }
}