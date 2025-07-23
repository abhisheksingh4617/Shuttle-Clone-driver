package com.shuttleclone.driver.Util;

import static com.shuttleclone.driver.Util.UtilMethodsKt.myLog;

import android.app.ProgressDialog;
import android.content.Context;


public class LoadingDialog {

    static ProgressDialog progressDialog;
    private static String TAG ="LoadingDialog";


    public static void showLoadingDialog(Context context, String message) {

        try {
            if (!(progressDialog != null && progressDialog.isShowing())) {
                progressDialog = new ProgressDialog(context);
                progressDialog.setMessage(message);

                progressDialog.setCancelable(true);
                progressDialog.setCanceledOnTouchOutside(false);

                progressDialog.show();
            }
        } catch (Exception e) {
            myLog(TAG, "showLoadingDialog: error="+e.getLocalizedMessage());
        }
    }

    public static void cancelLoading() {
        try {
            if (null!=progressDialog&&progressDialog.isShowing())
                progressDialog.cancel();
        } catch (Exception e) {
            myLog(TAG, "cancelLoading: error="+e.getLocalizedMessage());
        }

    }



}
