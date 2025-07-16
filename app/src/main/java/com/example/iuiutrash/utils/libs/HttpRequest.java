package com.example.iuiutrash.utils.libs;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpRequest {
    public interface ApiCallback<T> {
        void onResults(HttpResult<T> result);
    }

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final HttpClient httpClient;
    private final Context context;

    public HttpRequest(Context context, String baseUrl) {
        this.context = context;
        this.httpClient = new HttpClient(context, baseUrl);
    }

    public <T> void postData(String endPoint, Map<String, String> formData, Class<T> clazz, ApiCallback<T> callback) {
        executor.execute(() -> {
            try {
                HttpResult response = httpClient.postData(endPoint, formData);

                handler.post(() -> {
                    Log.e(clazz.getName(), response.message);
                    if(response.status){
                        HttpResult<T> res = parseResults(response.message, clazz);
                        callback.onResults(res);
                    }else{
                        callback.onResults(response);
                    }
                });

            } catch (Exception e) {
                handler.post(() -> {
                    callback.onResults(new HttpResult(false, e.getMessage()));
                });
            }
        });
    }

    private <T> HttpResult<T> parseResults(String json, Class<T> clazz) {
        try {
            JSONObject jObj = new JSONObject(json);
            HttpResult<T> result = new HttpResult<>();
            result.status = jObj.has("status") ? jObj.optBoolean("status") : false;
            result.message = jObj.has("message") ? jObj.optString("message") : "";

            List<T> list = new ArrayList<>();

            if (jObj.has("values") && jObj.optString("values") != null) {
                Log.d(clazz.getName(), jObj.optString("values"));
                JSONArray valuesArray = jObj.optJSONArray("values");

                for (int i = 0; i < valuesArray.length(); i++) {
                    JSONObject itemJson = valuesArray.getJSONObject(i);
                    T instance = clazz.getDeclaredConstructor().newInstance();

                    for (Field field : clazz.getDeclaredFields()) {
                        field.setAccessible(true);
                        String fieldName = field.getName();
                        
                        // Skip static fields
                        if (fieldName.equals("TAG") || fieldName.equals("CREATOR")) {
                            continue;
                        }

                        // Convert camelCase to snake_case for matching
                        String snakeCase = fieldName.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
                        
                        Log.d(clazz.getName(), "Looking for field: " + fieldName + " (snake_case: " + snakeCase + ")");
                        
                        for (Iterator<String> it = itemJson.keys(); it.hasNext(); ) {
                            String key = it.next();
                            
                            if (fieldName.equalsIgnoreCase(key) || snakeCase.equalsIgnoreCase(key)) {
                                Object value = itemJson.get(key);
                                if (value != JSONObject.NULL) {
                                    Class<?> type = field.getType();
                                    try {
                                        if (type == int.class || type == Integer.class)
                                            field.set(instance, itemJson.getInt(key));
                                        else if (type == boolean.class || type == Boolean.class)
                                            field.set(instance, itemJson.getBoolean(key));
                                        else if (type == double.class || type == Double.class)
                                            field.set(instance, itemJson.getDouble(key));
                                        else if (type == float.class || type == Float.class)
                                            field.set(instance, (float) itemJson.getDouble(key));
                                        else
                                            field.set(instance, itemJson.getString(key));
                                        
                                        Log.d(clazz.getName(), "Set " + fieldName + " to: " + value);
                                    } catch (Exception e) {
                                        Log.e(clazz.getName(), "Error setting field " + fieldName + ": " + e.getMessage());
                                    }
                                }
                                break;
                            }
                        }
                    }
                    list.add(instance);
                }
            }

            result.values = list;
            return result;
        } catch (Exception e) {
            Log.e("HttpRequest", "Error parsing results: " + e.getMessage());
            e.printStackTrace();
            return new HttpResult(false, e.getMessage());
        }
    }
}
