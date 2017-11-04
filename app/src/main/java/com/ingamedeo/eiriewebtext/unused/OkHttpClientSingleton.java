package com.ingamedeo.eiriewebtext.unused;

import okhttp3.OkHttpClient;

/**
 * Created by ingamedeo on 02/11/2017.
 */

public class OkHttpClientSingleton {

    private static OkHttpClientSingleton mInstance = null;
    private OkHttpClient mClient = null;

    private OkHttpClientSingleton() {
        mClient = getClient();
    }

    public static synchronized OkHttpClientSingleton getInstance() {

        if (mInstance==null) {
            mInstance = new OkHttpClientSingleton();
        }

        return mInstance;
    }

    public OkHttpClient getClient() {

        if (mClient == null) {
            mClient = new OkHttpClient();
        }

        return mClient;
    }
}
