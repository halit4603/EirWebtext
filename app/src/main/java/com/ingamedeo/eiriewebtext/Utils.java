package com.ingamedeo.eiriewebtext;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by ingamedeo on 23/04/2017.
 */

public class Utils {

    private static SharedPreferences sharedPreferences = null;

    /* Shared preferences getter */
    public static SharedPreferences getPreferences(Context context) {
        if (sharedPreferences==null) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        }
        return sharedPreferences;
    }

    //This is UTC time: Returns the number of milliseconds since January 1, 1970, 00:00:00 GMT represented by this Date object.
    public static long genWebTextTimestamp() {
        return new Date().getTime() / 1000L;
    }

    public static String fromUTCTimestampToString(long timestamp) {
        return DateFormat.format("HH:mm dd-MM-yyyy", timestamp*1000L).toString();
    }



    public static String fromBytesToUTF8String(byte[] bytes) {
        return new String(bytes, Constants.UTF8_CHARSET);
    }

    public static byte[] fromStringToBytesUTF8(String string) {
        byte[] b = string.getBytes(Constants.UTF8_CHARSET);
        return b;
    }

    //Called from NetworkUtils
    public static String getTimestampAsString() {
        Long tsLong = System.currentTimeMillis();
        return tsLong.toString();
    }

    public static String ieNumberToMSISDN(String number) {

        if (number!=null && number.trim().length()>0) {

            //Trim initial 0
            if (number.charAt(0)=='0') {
                number = number.substring(1);
            }

            number = Constants.IE_PREFIX+number;
            return number;
        }

        return null;
    }

    //Called mainly from NetworkUtils
    public static JSONObject stringToJSON(String jsonString) {

        if (jsonString==null) {
            return null;
        }

        try {
            return new JSONObject(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }





}
