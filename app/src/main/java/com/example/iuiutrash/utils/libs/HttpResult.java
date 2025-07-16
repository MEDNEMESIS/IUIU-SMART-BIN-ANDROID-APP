package com.example.iuiutrash.utils.libs;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import android.util.Log;
import com.example.iuiutrash.model.User;
import com.example.iuiutrash.model.FeedbackModel;

public class HttpResult<T> {
    public boolean status;
    public int code;
    public String message;
    public List<T> values;
    public int rows;

    public HttpResult() {
        this.status = false;
        this.message = "";
        this.values = new ArrayList<>();
        this.rows = 0;
    }

    public HttpResult(boolean status, String message) {
        this.status = status;
        this.message = message;
        this.values = new ArrayList<>();
        this.rows = 0;
    }

    public HttpResult(boolean status, int code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.values = new ArrayList<>();
        this.rows = 0;
    }

    @SuppressWarnings("unchecked")
    public HttpResult(String response, Class<T> type) {
        try {
            JSONObject json = new JSONObject(response);
            this.status = json.getBoolean("status");
            this.message = json.getString("message");
            
            if (json.has("values") && !json.isNull("values")) {
                JSONArray valuesArray = json.getJSONArray("values");
                this.values = new ArrayList<>();
                for (int i = 0; i < valuesArray.length(); i++) {
                    JSONObject obj = valuesArray.getJSONObject(i);
                    if (type == User.class) {
                        this.values.add((T) new User(obj));
                    } else if (type == FeedbackModel.class) {
                        this.values.add((T) new FeedbackModel(obj));
                    } else {
                        // Add other type conversions as needed
                        this.values.add((T) obj);
                    }
                }
                // Set rows to match the actual number of values
                this.rows = this.values.size();
            } else {
                this.values = new ArrayList<>();
                this.rows = 0;
            }
            
            Log.d("HttpResult", "Parsed response - Status: " + status + 
                  ", Message: " + message + 
                  ", Rows: " + rows + 
                  ", Values size: " + (values != null ? values.size() : 0));
        } catch (Exception e) {
            Log.e("HttpResult", "Error parsing response: " + e.getMessage());
            this.status = false;
            this.message = "Error parsing response";
            this.rows = 0;
            this.values = new ArrayList<>();
        }
    }

    public int rows() {
        return this.rows;
    }

    public boolean hasData() {
        return this.values != null && !this.values.isEmpty();
    }

    public T getValueAt(int index) {
        if (this.values != null && index >= 0 && index < this.values.size()) {
            return this.values.get(index);
        }
        return null;
    }

    public List<T> getValues() {
        return this.values;
    }

    @Override
    public String toString() {
        return "status=" + status + ",  code=" + code + ",message='" + message + "' }";
    }
}
