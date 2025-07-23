package com.shuttleclone.driver.Util

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import com.shuttleclone.driver.ui.Activity.SplashActivity
import org.jsoup.Jsoup


class AppUpdate(val mContext: SplashActivity) : AsyncTask<Void, String, String>() {


    override fun doInBackground(vararg p0: Void?): String {
        var playStoreVersion: String? = null
        return try {
            playStoreVersion = Jsoup.connect(
                "https://play.google.com/store/apps/details?id=" + mContext.getPackageName()
                    .toString() + "&hl=it"
            )
                .timeout(30000)
                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                .referrer("http://www.google.com")
                .get()
                .select(".hAyfc .htlgb")[7]
                .ownText()
            playStoreVersion
        } catch (e: Exception) {
            myLog("AppUpdate", "doInBackground: " + e.localizedMessage).toString()
           return "1.0"
        }
    }


    override fun onPostExecute(playStoreVersion: String?) {
        super.onPostExecute(playStoreVersion)

        try {

            val currentVersion = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                mContext.packageManager.getPackageInfo(mContext.packageName, 0).versionName
            } else {
                mContext.packageManager.getPackageInfo(mContext.packageName, 0).versionName
            }
            myLog("AppUpdate", "Current version =" + currentVersion + " playstore version=" + playStoreVersion);

            if (playStoreVersion != null && !playStoreVersion.isEmpty()) {

                if (currentVersion != null) {
                    mContext.appStatus(currentVersion == playStoreVersion)

                    myLog("AppUpdate", "Update =" + (currentVersion < playStoreVersion));

                    if (currentVersion != playStoreVersion) {
                        myLog("update", "onPostExecute: ")

                        val dialog = AlertDialog.Builder(mContext).setTitle("Update available")
                        dialog.setMessage("New version with updated features available on the play store. Please update the application to continue.")
                        dialog.setCancelable(false)
                        dialog.setPositiveButton(
                            "Update"
                        ) { dialog, which ->
                            val intent = Intent(Intent.ACTION_MAIN)
                            intent.addCategory(Intent.CATEGORY_HOME)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            mContext.startActivity(intent)

                            val appPackageName =
                                mContext.packageName // getPackageName() from Context or Activity object
                            try {
                                mContext.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("market://details?id=$appPackageName")
                                    )
                                )
                            } catch (anfe: ActivityNotFoundException) {
                                mContext.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                                    )
                                )
                            }
                        }
                        dialog.show()
                    }
                }
            }
        } catch (e: Exception) {
            myLog("AppUpdate", "onPostExecute: " + e.localizedMessage).toString()
        }
    }


}

internal interface AppUpdateResponse {
    fun appStatus(versionStatus: Boolean)
}
