package com.ingamedeo.eiriewebtext.unused;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

/**
 * Created by ingamedeo on 04/11/2017.
 */

public class CookieJarSingleton {

    private static CookieJarSingleton cookieJarSingleton = null;
    private CookieJarWithClear cookieJar = null;

    private CookieJarSingleton() {
        cookieJar = getCookieJar();
    }

    public synchronized static CookieJarSingleton getInstance() {

        if (cookieJarSingleton==null) {
            cookieJarSingleton = new CookieJarSingleton();
        }

        return cookieJarSingleton;
    }

    public CookieJarWithClear getCookieJar() {

        if (cookieJar==null) {
            cookieJar = new CookieJarWithClear();
        }

        return cookieJar;
    }

    public static class CookieJarWithClear implements CookieJar {

        private final HashMap<HttpUrl, List<Cookie>> cookieStore = new HashMap<>();

        @Override
        public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
            cookieStore.put(url, cookies);
        }

        @Override
        public List<Cookie> loadForRequest(HttpUrl url) {
            List<Cookie> cookies = cookieStore.get(url);
            return cookies != null ? cookies : new ArrayList<Cookie>();
        }

        public void clearCookies() {
            cookieStore.clear();
        }
    }

}
