package com.ingamedeo.eiriewebtext.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.text.Html;

import com.ingamedeo.eiriewebtext.Constants;
import com.ingamedeo.eiriewebtext.R;

/**
 * Created by ingamedeo on 04/11/2017.
 */

public class PermissionsUtils {

    public static boolean checkAndRequestPermissions(Activity thisActivity) {
        if (PermissionChecker.checkSelfPermission(thisActivity, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(thisActivity,
                    Manifest.permission.READ_CONTACTS)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                showPermissionsDialog(thisActivity);

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(thisActivity,
                        Constants.myPermisssions,
                        Constants.MY_PERMISSIONS_REQUEST);
            }

            return false;
        }
        return true;
    }

    public static boolean checkPermissions(Activity thisActivity) {
        return PermissionChecker.checkSelfPermission(thisActivity, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
    }

    private static void showPermissionsDialog(final Activity activity) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(activity, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(activity);
        }
        builder.setTitle(R.string.permissions_dialog_title)
                .setMessage(Html.fromHtml(activity.getString(R.string.permissions_dialog_message)))
                .setPositiveButton(R.string.permissions_dialog_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ActivityCompat.requestPermissions(activity,
                                Constants.myPermisssions,
                                Constants.MY_PERMISSIONS_REQUEST);
                    }
                })
                .show();
    }

}
