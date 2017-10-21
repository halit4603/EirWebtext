package com.ingamedeo.eiriewebtext;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.ingamedeo.eiriewebtext.db.AccountsTable;
import com.ingamedeo.eiriewebtext.db.ContentProviderDb;
import com.ingamedeo.eiriewebtext.db.DbAdapter;
import com.ingamedeo.eiriewebtext.db.WebTextsTable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by ingamedeo on 23/04/2017.
 */

public class Utils {

    private static SharedPreferences sharedPreferences = null;
    private static DbAdapter dbAdapter = null;

    /* Shared preferences getter */
    public static SharedPreferences getPreferences(Context context) {
        if (sharedPreferences==null) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        }
        return sharedPreferences;
    }


    public static long genWebTextTimestamp() {
        return new Date().getTime() / 1000;
    }

    public static Uri generateContentUri(Constants.TableSelect tableSelect) {
        switch (tableSelect) {
            case WEBTEXT:
                return Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, WebTextsTable.TABLE_NAME);
            case ACCOUNTS:
                return Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, AccountsTable.TABLE_NAME);
            default:
                return null;
        }
    }

    public static String fromBytesToUTF8String(byte[] bytes) {
        return new String(bytes, Constants.UTF8_CHARSET);
    }

    public static byte[] fromStringToBytesUTF8(String string) {
        byte[] b = string.getBytes(Constants.UTF8_CHARSET);
        return b;
    }

    public static DbAdapter getDbAdapter(Context context) {
        if (dbAdapter==null) {
            dbAdapter = new DbAdapter(context.getApplicationContext());
        }
        return dbAdapter;
    }

    public static int getCursorDataCount(Cursor cursor) {
        int count = 0;

        if(cursor!=null && cursor.moveToFirst() && cursor.getCount()>0){
            do{
                count++;

            }while(cursor.moveToNext());
        }

        //cursor.close();
        return count;
    }

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

    public static boolean sendWebText(String emailAddress, String password, String from, String to, String content) {

        CookieManager msCookieManager = new CookieManager();

        Constants.EirLoginResult login = loginToEir(emailAddress, password, msCookieManager);
        Log.i(Constants.TAG, "EirLoginResult(): " + login.toString());

        if (msCookieManager.getCookieStore().getCookies().size() > 0) {
            Log.i(Constants.TAG, "Cookies: " +msCookieManager.getCookieStore().getCookies());
        }

        Pair<Boolean, String> webtext = sendWebTextViaEir(from, to, content, msCookieManager);
        Log.i(Constants.TAG, "sendWebTextViaEir(): " + webtext.toString());

        if (login== Constants.EirLoginResult.SUCCESS && webtext!=null && webtext.first) {
            return true;
        }

        return false;
    }

    private static String getTimestampAsString() {
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

    public static boolean runInputCheck(EditText editText) {

        if (editText!=null && editText.getText()!=null && editText.getText().toString().trim().length()>0) {
            return true;
        }

        return false;
    }

    private static JSONObject stringToJSON(String jsonString) {

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

    public static Constants.EirLoginResult loginToEir(String emailAddress, String password, CookieManager cookieManager) {

        Pair<Integer, String> login = postRequest(Constants.URL_AUTH, cookieManager, "{\"emailAddress\":\""+emailAddress+"\",\"password\":\""+password+"\"}");

        if (login==null || login.first==null) {
            return Constants.EirLoginResult.GENERIC_ERROR;
        }

        if (login.first==Constants.REST_401_KO || login.first==Constants.REST_400_KO) {
            return Constants.EirLoginResult.WRONG_USER_PASS;
        }

        Log.i(Constants.TAG, "Login responseCode: " + login.first);
        Log.i(Constants.TAG, "Login: " + login.second);

        JSONObject loginJson = stringToJSON(login.second);
        if (loginJson!=null) {
            JSONObject data = loginJson.optJSONObject("data");
            if (data!=null) {
                String status = data.optString("status");
                if (status!=null) {
                    if (status.equals("Success")) {
                        return Constants.EirLoginResult.SUCCESS;
                    } else if (status.equals("TemporarilyLocked")) {
                        return Constants.EirLoginResult.LOCKED;
                    }
                }
            }
        }

        return Constants.EirLoginResult.GENERIC_ERROR;
    }



    private static Pair<Boolean, String> sendWebTextViaEir(String from, String to, String content, CookieManager cookieManager) {

        Pair<Integer, String> webtext = postRequest(Constants.URL_WEBTEXT+from+Constants.URL_WEBTEXT2+"ts="+getTimestampAsString(), cookieManager, "{\"content\":\""+content+"\",\"recipients\":[\""+to+"\"]}");

        if (webtext==null || webtext.first==null) {
            return new Pair<>(false, null);
        }

        if (webtext.first==Constants.REST_401_KO || webtext.first==Constants.REST_400_KO) {
            return new Pair<>(false, null);
        }

        Log.i(Constants.TAG, "Webtext responseCode: " + webtext.first);
        Log.i(Constants.TAG, "Webtext: " + webtext.second);

        //{"location":{"href":"messages/XXXXXX"}}
        JSONObject webtextJson = stringToJSON(webtext.second);
        if (webtextJson!=null) {
            JSONObject location = webtextJson.optJSONObject("location");
            if (location!=null) {
                String href = location.optString("href");
                String textId = href.replace("messages/", "");
                return new Pair<>(true, textId);
            }
        }

        return new Pair<>(false, null);
    }

    public static String[] getCustomerLines(CookieManager cookieManager) {
        return getCustomerInfo(cookieManager, Constants.CustomerInfoType.LINES);
    }

    public static String[] getCustomerFullName(CookieManager cookieManager) {
        return getCustomerInfo(cookieManager, Constants.CustomerInfoType.FULLNAME);
    }

    public static String[] getCustomerInfo(CookieManager cookieManager, Constants.CustomerInfoType customerInfoType) {

        //Can't process without valid cookies
        if (cookieManager == null) {
            return null;
        }

        ArrayList<String> resArrayList = new ArrayList<>();

        String url = Constants.URL_LINES + getTimestampAsString();
        Pair<Integer, String> lines = getRequest(url, cookieManager, null);
        Log.i(Constants.TAG, "getCustomerLines(): " + url);


        if (lines == null || lines.first == null) {
            return null;
        }

        JSONObject linesJson = stringToJSON(lines.second);
        if (linesJson != null) {
            JSONObject data = linesJson.optJSONObject("data");
            if (data != null) {
                JSONArray pairingsList = data.optJSONArray("pairingsList");
                if (pairingsList != null && pairingsList.length() > 0) {

                    //Get all lines registered with this account
                    for (int i = 0; i < pairingsList.length(); i++) {

                        JSONObject pairing = pairingsList.optJSONObject(i);

                        if (pairing != null) {

                            String val = null;

                            switch (customerInfoType) {
                                case LINES:
                                    val = pairing.optString("number");
                                    break;
                                case FULLNAME:
                                    val = pairing.optString("customerName");
                                    break;
                            }

                            if (val != null) {
                                resArrayList.add(val);
                            }

                        }
                    }

                    return resArrayList.toArray(new String[resArrayList.size()]);
                }
            }
        }

        return null;
    }

    private static Pair<Integer, String> getRequest(String stringURL, CookieManager cookieManager, String body) {
        return restRequest(stringURL, Constants.RequestType.GET, cookieManager, body);
    }

    private static Pair<Integer, String> postRequest(String stringURL, CookieManager cookieManager, String body) {
        return restRequest(stringURL, Constants.RequestType.POST, cookieManager, body);
    }

    private static Pair<Integer, String> restRequest(String stringURL, Constants.RequestType requestType, CookieManager cookieManager, String body) {
        try {
            URL url = new URL(stringURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            if (requestType== Constants.RequestType.POST) {
                conn.setDoOutput(true); //Send request body
            }

            conn.setRequestMethod(requestType.name());
            conn.setRequestProperty("Content-Type", Constants.JSON_CONTENT_TYPE);

            //Import cookies, if any
            if (cookieManager!=null && cookieManager.getCookieStore().getCookies().size() > 0) {
                // While joining the Cookies, use ',' or ';' as needed. Most of the servers are using ';'
                conn.setRequestProperty("Cookie",
                        TextUtils.join(";",  cookieManager.getCookieStore().getCookies()));
            }
            //end cookies import

            conn.setUseCaches(false);

            if (requestType== Constants.RequestType.POST) {
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(conn.getOutputStream());
                outputStreamWriter.write(body);
                outputStreamWriter.flush();
            }

            int responseCode = conn.getResponseCode();

            //Check we are interested in getting cookies
            if (cookieManager!=null) {
                Map<String, List<String>> headerFields = conn.getHeaderFields();
                List<String> cookiesHeader = headerFields.get(Constants.COOKIES_HEADER);

                //Couldn't get cookies? Return now
                if (cookiesHeader == null) {
                    return new Pair<>(responseCode, null);
                }

                //Add cookies to cookieManager
                for (String cookie : cookiesHeader) {
                    cookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
                }
            }

            //Request failed? Return response code now
            if (responseCode!=Constants.REST_200_OK && responseCode!=Constants.REST_201_OK) {
                return new Pair<>(responseCode, null);
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return new Pair<>(responseCode, response.toString());

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

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
