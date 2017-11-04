package com.ingamedeo.eiriewebtext;

import android.Manifest;

import java.nio.charset.Charset;

/**
 * Created by ingamedeo on 23/04/2017.
 */

public class Constants {

    public final static String TAG = "meteor";
    public final static String PACKAGE = "com.ingamedeo.eiriewebtext";
    public final static String ADMOB_APP_ID = "ca-app-pub-8649408349532515~7668946235";

    public static final Charset UTF8_CHARSET = Charset.forName("UTF-8");
    public static final String EMPTY = "";

    public enum TableSelect {
        WEBTEXT,
        ACCOUNTS,
        ACCOUNTS_DISTINCT
    }

    public enum RequestType {
        GET,
        POST
    }

    public static final String JSON_CONTENT_TYPE = "application/json;charset=UTF-8";
    public static final String COOKIES_HEADER = "Set-Cookie";

    public static final int REST_200_OK = 200;
    public static final int REST_201_OK = 201;
    public static final int REST_401_KO = 401;
    public static final int REST_400_KO = 400;

    public enum EirLoginResult {
        SUCCESS,
        LOCKED,
        WRONG_USER_PASS,
        GENERIC_ERROR
    }

    public enum CustomerInfoType {
        LINES,
        FULLNAME
    }

    public static final String URL_AUTH = "https://my.eir.ie/rest/brand/3/portalUser/authenticate";
    public static final String URL_WEBTEXT = "https://my.eir.ie/mobile/webtext/mobileNumbers/";
    public static final String URL_WEBTEXT2 = "/messages?";
    public static final String URL_LINES = "https://my.eir.ie/rest/secure/brand/3/portalUser/lines?ts=";
    public static final String IE_PREFIX = "+353";

        /* Permissions request constants */

    public static final String[] myPermisssions = new String[]{Manifest.permission.READ_CONTACTS};
    public static final int MY_PERMISSIONS_REQUEST = 5;

    public static final int MAX_WEBTEXT_LEN = 480;
    public static final int MAX_WEBTEXT_NUM = 3;

}
