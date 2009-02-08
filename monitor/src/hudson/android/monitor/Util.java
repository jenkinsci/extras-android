/*
 * The MIT License
 * 
 * Copyright (c) 2009, Xavier Le Vourch
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.android.monitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import android.util.Log;

/**
 *
 * @author Xavier Le Vourch
 *
 */

public final class Util {

    public static final String LOG_TAG = "HudsonMonitor";

    public static final String UPDATE_URL = "http://xlv-labs.com/android/hudsonmonitor/latest?version=";

    private Util() {
    }

    public static final String MARKET_URL = "market://search?q=pname:hudson.android.monitor";

    public static final long SECOND_IN_MILLISECONDS = 1000;

    public static final long MINUTE_IN_MILLISECONDS = SECOND_IN_MILLISECONDS * 60;

    public static final long HOUR_IN_MILLISECONDS = MINUTE_IN_MILLISECONDS * 60;

    public static final long DAY_IN_MILLISECONDS = HOUR_IN_MILLISECONDS * 24;

    public static final String PACKAGE_NAME = "hudson.android.monitor";

    private static final String HUDSON_DATE_FORMAT = "yyy-MM-dd'T'HH:mm:ssZ";

    public static Date parseHudsonDate(final String date) {
        try {
            DateFormat dateFormat = new SimpleDateFormat(HUDSON_DATE_FORMAT, Locale.getDefault());
            Date d = dateFormat.parse(date.replaceAll("Z$", "-0000"));

            return d;
        } catch (ParseException e) {
            Log.e(LOG_TAG, "Parse exception for " + date, e);

            return new Date(0);
        }
    }

    public static String[] getLatestVersion(int installedVersion) {
        String[] a = new String[2];
        try {
            URL url;
            URLConnection urlConn = null;

            url = new URL(UPDATE_URL + installedVersion);
            urlConn = url.openConnection();

            BufferedReader r = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));

            a[0] = r.readLine();
            a[1] = r.readLine();

            r.close();
        } catch (IOException e) {
            a[0] = "0";
        }
        return a;
    }

    static {
        // Make sure self signed certificates are accepted
        try {
            javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            TrustManager[] dummyTrustManager = new TrustManager[] { new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[] {};
                }

                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                }
            } };

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, dummyTrustManager, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
        } catch (NoSuchAlgorithmException e) {
            Log.e(LOG_TAG, "self signed certificate", e);
        } catch (KeyManagementException e) {
            Log.e(LOG_TAG, "self signed certificate", e);
        }
    }

    public static URLConnection getURLConnection(String urlString, String userName, String password) throws MonitorException {
        try {
            URL url = new URL(urlString);
            URLConnection urlConn = url.openConnection();

            if (userName.length() != 0) {
                String auth = Base64.encodeBytes((userName + ':' + password).getBytes());
                urlConn.setRequestProperty("Authorization", "Basic " + auth);
            }

            return urlConn;
        } catch (IOException e) {
            throw new MonitorException("Connection problem", e);
        }
    }

}
