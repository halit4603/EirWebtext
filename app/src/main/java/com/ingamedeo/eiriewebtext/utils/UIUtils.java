package com.ingamedeo.eiriewebtext.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.text.Html;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.ingamedeo.eiriewebtext.Constants;
import com.ingamedeo.eiriewebtext.R;

/**
 * Created by ingamedeo on 04/11/2017.
 */

public class UIUtils {

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static boolean runInputCheck(EditText editText) {

        if (editText!=null && editText.getText()!=null && editText.getText().toString().trim().length()>0) {
            return true;
        }

        return false;
    }

    private static void showInAppPurchaseDialog(final Activity activity) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(activity, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(activity);
        }
        builder.setTitle(R.string.hide_ads_title)
                .setMessage(Html.fromHtml("I'm an independent app developer, "))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ActivityCompat.requestPermissions(activity,
                                Constants.myPermisssions,
                                Constants.MY_PERMISSIONS_REQUEST);
                    }
                })
                .show();
    }

    public static void showGenericYesDialog(final Activity activity, String title, String htmlBody, boolean cancelable) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(activity, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(activity);
        }
        builder.setTitle(title)
                .setCancelable(cancelable)
                .setMessage(Html.fromHtml(htmlBody))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    public static void showDebugDialog(final Activity activity) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(activity, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(activity);
        }
        builder.setTitle(R.string.app_name)
                .setCancelable(false)
                .setMessage(Html.fromHtml("<b>This is a DEBUG VERSION of Eir.ie Webtexts.</b><br><br>Redistribution of this APK file is prohibited. Download the app through the Play Store unless instructed otherwise.<br><br>Google Play <a href=\"https://www.amedeobaragiola.me/blog/eir-ie-webtext-privacy-policy/\">privacy policy</a> applies, additional information may be collected for debug purposes."))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }
}
