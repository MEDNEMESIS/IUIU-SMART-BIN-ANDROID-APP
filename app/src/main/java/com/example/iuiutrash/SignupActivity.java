package com.example.iuiutrash;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.iuiutrash.model.User;
import com.example.iuiutrash.utils.CustomAlertDialog;
import com.example.iuiutrash.utils.ServerApi;
import com.example.iuiutrash.utils.libs.HttpResult;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";
    private static final int PICK_IMAGE_REQUEST = 1;
    private TextInputLayout nameLayout, emailLayout, passwordLayout;
    private TextInputEditText nameEditText, emailEditText, passwordEditText;
    private ImageView profileImageView;
    private byte[] profilePhoto;
    private ServerApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        try {
            api = new ServerApi(this);

            // Initialize views
            nameLayout = findViewById(R.id.nameLayout);
            emailLayout = findViewById(R.id.emailLayout);
            passwordLayout = findViewById(R.id.passwordLayout);
            nameEditText = findViewById(R.id.nameEditText);
            emailEditText = findViewById(R.id.emailEditText);
            passwordEditText = findViewById(R.id.passwordEditText);
            profileImageView = findViewById(R.id.profileImageView);

            if (nameLayout == null || emailLayout == null || passwordLayout == null ||
                nameEditText == null || emailEditText == null || passwordEditText == null ||
                profileImageView == null) {
                Log.e(TAG, "Error initializing views");
                Toast.makeText(this, "Error initializing signup screen", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Set default profile image
            profileImageView.setImageResource(R.drawable.ic_launcher_foreground);

            // Set up signup button click listener
            findViewById(R.id.signupButton).setOnClickListener(v -> attemptSignup());

            // Set up login text click listener
            findViewById(R.id.loginTextView).setOnClickListener(v -> {
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                finish();
            });

            // Set up profile photo upload button
            findViewById(R.id.uploadPhotoButton).setOnClickListener(v -> openImageChooser());
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error initializing signup screen", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void attemptSignup() {
        try {
            // Reset errors
            nameLayout.setError(null);
            emailLayout.setError(null);
            passwordLayout.setError(null);

            String fullName = nameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            Log.d(TAG, "Attempting signup with email: " + email);

            boolean cancel = false;
            View focusView = null;

            // Validate full name
            if (TextUtils.isEmpty(fullName)) {
                nameLayout.setError("Full name is required");
                focusView = nameEditText;
                cancel = true;
            }

            // Validate email
            if (TextUtils.isEmpty(email)) {
                emailLayout.setError("Email is required");
                focusView = emailEditText;
                cancel = true;
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailLayout.setError("Enter a valid email address");
                focusView = emailEditText;
                cancel = true;
            }

            // Validate password
            if (TextUtils.isEmpty(password)) {
                passwordLayout.setError("Password is required");
                focusView = passwordEditText;
                cancel = true;
            } else if (password.length() < 6) {
                passwordLayout.setError("Password must be at least 6 characters");
                focusView = passwordEditText;
                cancel = true;
            }

            if (cancel) {
                if (focusView != null) {
                    focusView.requestFocus();
                }
            } else {
                // Proceed with signup
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("password", password);
                params.put("fullname", fullName);

                api.signup(params, new ServerApi.Callback() {
                    @Override
                    public void onResults(HttpResult result) {
                        if (result.status) {
                            Toast.makeText(SignupActivity.this, "Registration successful! Please login.", Toast.LENGTH_LONG).show();
                            // Navigate to login screen
                            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            api.showAlert("Error", result.message);
                        }
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during signup: " + e.getMessage(), e);
            Toast.makeText(this, "Error during signup: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {
                Uri imageUri = data.getData();
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                profileImageView.setImageBitmap(bitmap);

                // Convert bitmap to byte array
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                profilePhoto = stream.toByteArray();
            } catch (IOException e) {
                Log.e(TAG, "Error loading image: " + e.getMessage(), e);
                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
            }
        }
    }
} 