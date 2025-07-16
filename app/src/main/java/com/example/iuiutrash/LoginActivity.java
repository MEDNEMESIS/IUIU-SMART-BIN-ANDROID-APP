package com.example.iuiutrash;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.example.iuiutrash.model.User;
import com.example.iuiutrash.utils.CustomAlertDialog;
import com.example.iuiutrash.utils.ServerApi;
import com.example.iuiutrash.utils.UserManager;
import com.example.iuiutrash.utils.libs.HttpResult;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private TextInputLayout emailLayout, passwordLayout;
    private TextInputEditText emailEditText, passwordEditText;
    private MaterialButton loginButton;
    private MaterialTextView signupTextView;

    private ServerApi api;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        try {
            api = new ServerApi(this);
            Log.d(TAG, "DatabaseHelper initialized");

            // Initialize views
            emailLayout = findViewById(R.id.emailLayout);
            passwordLayout = findViewById(R.id.passwordLayout);
            emailEditText = findViewById(R.id.emailEditText);
            passwordEditText = findViewById(R.id.passwordEditText);
            loginButton = findViewById(R.id.loginButton);
            signupTextView = findViewById(R.id.signupTextView);

            emailEditText.setText("admin");
            passwordEditText.setText("admin123");

            if (emailLayout == null || passwordLayout == null ||
                    emailEditText == null || passwordEditText == null ||
                    loginButton == null || signupTextView == null) {
                Log.e(TAG, "Error initializing views");
                Toast.makeText(this, "Error initializing login screen", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Set up login button click listener
            loginButton.setOnClickListener(v -> handleLogin(this));

            // Set up signup text click listener
            signupTextView.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error initializing login screen", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void handleLogin(Context context) {
        try {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            // Reset errors
            emailLayout.setError(null);
            passwordLayout.setError(null);

            // Validate email
            if (TextUtils.isEmpty(email)) {
                emailLayout.setError("Email is required");
                emailEditText.requestFocus();
                return;
            }

            // Validate password
            if (TextUtils.isEmpty(password)) {
                passwordLayout.setError("Password is required");
                passwordEditText.requestFocus();
                return;
            }

            Map<String, String> params = new HashMap<>();
            params.put("username", email);
            params.put("password", password);

            Log.d(TAG, "Attempting login with email: " + email + ", password length: " + password.length());

            api.login(params, new ServerApi.Callback() {
                @Override
                public void onResults(HttpResult result) {
                    Log.d(TAG, "Login response - Status: " + result.status + 
                          ", Message: " + result.message + 
                          ", Rows: " + result.rows() + 
                          ", Has Data: " + result.hasData() +
                          ", Values: " + (result.hasData() ? result.getValueAt(0).toString() : "null"));
                    
                    if (result.status) {
                        if (result.hasData()) {
                            User u = (User) result.getValueAt(0);
                            Log.d(TAG, "User data - Email: " + u.getEmail() + 
                                  ", Role: " + u.getUserrole());
                            // Save user to UserManager
                            UserManager.getInstance(LoginActivity.this).saveUser(u);
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Log.e(TAG, "Login failed - No data");
                            runOnUiThread(() -> {
                                Toast.makeText(LoginActivity.this, 
                                    "Login failed: Invalid credentials", 
                                    Toast.LENGTH_LONG).show();
                            });
                        }
                    } else {
                        Log.e(TAG, "Login failed - Error: " + result.message);
                        runOnUiThread(() -> {
                            Toast.makeText(LoginActivity.this, 
                                "Error: " + result.message, 
                                Toast.LENGTH_LONG).show();
                        });
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error during login: " + e.getMessage(), e);
            Toast.makeText(this, "Error during login: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
} 