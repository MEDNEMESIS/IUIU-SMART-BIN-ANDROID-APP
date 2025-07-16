package com.example.iuiutrash.utils.libs;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.util.Log;
import android.util.Patterns;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HttpClient {
    private static final String TAG = "HttpClient";
    private static final int CONNECT_TIMEOUT = 5000; // 5 seconds
    private static final int READ_TIMEOUT = 5000; // 5 seconds
    private static final int MAX_RETRIES = 3;

    private Context context;
    private String baseUrl;

    public HttpClient(Context context, String baseUrl) {
        this.context = context;
        this.baseUrl = baseUrl;
    }

    private HttpResult isValidUrl(String urlString) {
        try {
            if (urlString == null || urlString.isEmpty()) {
                throw new Exception("Missing base url");
            } else if (!(urlString.startsWith("http://") || urlString.startsWith("https://"))) {
                throw new Exception("Missing http or https in your base url");
            } else if (!Patterns.WEB_URL.matcher(urlString).matches()){
                throw new Exception("invalid base url");
            } else {
                String newStr = !urlString.endsWith("/") ? urlString : urlString.substring(0, urlString.length() - 1);
                return new HttpResult(true, newStr);
            }
        } catch (Exception e) {
            return new HttpResult(false, e.getMessage());
        }
    }

    private boolean isValidJson(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        value = value.trim();

        try {
            if (value.startsWith("{")) {
                new JSONObject(value);
            } else if (value.startsWith("[")) {
                new JSONArray(value);
            } else {
                return false;
            }
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

    private boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm != null) {
            Network network = cm.getActiveNetwork();
            if (network == null) return false;

            NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
            return capabilities != null &&
                    (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        }

        return false;
    }

    public HttpResult postData(String endpoint, Map<String, String> params) {
        if (!isInternetAvailable()) {
            return new HttpResult(false, "No internet connection available");
        }

        HttpResult urlResult = isValidUrl(baseUrl);
        if (!urlResult.status) {
            return urlResult;
        }

        String urlString = urlResult.message + endpoint;
        Log.d(TAG, "Making POST request to: " + urlString);

        int retryCount = 0;
        while (retryCount < MAX_RETRIES) {
            try {
                StringBuilder postData = new StringBuilder();
                for (Map.Entry<String, String> param : params.entrySet()) {
                    if (postData.length() != 0) postData.append('&');
                    postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                    postData.append('=');
                    postData.append(URLEncoder.encode(param.getValue(), "UTF-8"));
                }

                byte[] postDataBytes = postData.toString().getBytes("UTF-8");

                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setConnectTimeout(CONNECT_TIMEOUT);
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(postDataBytes);
                }

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "Response code: " + responseCode);

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                                responseCode < HttpURLConnection.HTTP_BAD_REQUEST
                                        ? conn.getInputStream()
                                        : conn.getErrorStream()
                        )
                );

                StringBuilder result = new StringBuilder();
                String line;

                while ((line = in.readLine()) != null) {
                    result.append(line);
                }

                in.close();
                conn.disconnect();

                String strResults = result.toString();
                if (isValidJson(strResults)) {
                    return new HttpResult(responseCode == 200, responseCode, strResults);
                } else {
                    throw new Exception(strResults);
                }

            } catch (Exception e) {
                Log.e(TAG, "Error in POST request (attempt " + (retryCount + 1) + "): " + e.getMessage());
                retryCount++;
                
                if (retryCount == MAX_RETRIES) {
                    return new HttpResult(false, "Failed to connect after " + MAX_RETRIES + " attempts: " + e.getMessage());
                }
                
                try {
                    Thread.sleep(1000); // Wait 1 second before retrying
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return new HttpResult(false, "Request interrupted: " + ie.getMessage());
                }
            }
        }

        return new HttpResult(false, "Failed to connect after " + MAX_RETRIES + " attempts");
    }
}

