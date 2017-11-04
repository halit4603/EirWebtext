package com.ingamedeo.eiriewebtext.utils;

import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.ingamedeo.eiriewebtext.Constants;
import com.ingamedeo.eiriewebtext.unused.CookieJarSingleton;
import com.ingamedeo.eiriewebtext.Utils;

import org.json.JSONArray;
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
import java.util.List;
import java.util.Map;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by ingamedeo on 04/11/2017.
 */

public class NetworkUtils {

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

        JSONObject loginJson = Utils.stringToJSON(login.second);
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

        Pair<Integer, String> webtext = postRequest(Constants.URL_WEBTEXT+from+Constants.URL_WEBTEXT2+"ts="+Utils.getTimestampAsString(), cookieManager, "{\"content\":\""+content+"\",\"recipients\":[\""+to+"\"]}");

        if (webtext==null || webtext.first==null) {
            return new Pair<>(false, null);
        }

        if (webtext.first==Constants.REST_401_KO || webtext.first==Constants.REST_400_KO) {
            return new Pair<>(false, null);
        }

        Log.i(Constants.TAG, "Webtext responseCode: " + webtext.first);
        Log.i(Constants.TAG, "Webtext: " + webtext.second);

        //{"location":{"href":"messages/XXXXXX"}}
        JSONObject webtextJson = Utils.stringToJSON(webtext.second);
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

        String url = Constants.URL_LINES + Utils.getTimestampAsString();
        Pair<Integer, String> lines = getRequest(url, cookieManager, null);
        Log.i(Constants.TAG, "getCustomerLines(): " + url);

        if (lines == null || lines.first == null) {
            return null;
        }

        JSONObject linesJson = Utils.stringToJSON(lines.second);
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

    /*
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .cookieJar(CookieJarSingleton.getInstance().getCookieJar()).build();

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static void okHttpRequest(String stringURL, Constants.RequestType requestType, String body, Callback callback) {

        Request.Builder builder = new Request.Builder().url(stringURL);

        if (requestType== Constants.RequestType.POST) {
            builder.post(RequestBody.create(JSON, body));
        }

        Request request = builder.build();

        client.newCall(request).enqueue(callback);
    }
    */


}
