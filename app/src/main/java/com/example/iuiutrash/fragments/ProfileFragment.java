package com.example.iuiutrash.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.iuiutrash.MainActivity;
import com.example.iuiutrash.R;
import com.example.iuiutrash.model.User;
import com.example.iuiutrash.utils.ServerApi;
import com.example.iuiutrash.utils.UserManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private User userData;
    private ServerApi api;

    private static final int PICK_IMAGE_REQUEST = 1;
    private TextInputLayout fullNameLayout, emailLayout, passwordLayout;
    private TextInputEditText fullNameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private ImageView profileImageView;
    private byte[] profilePhoto;

    public static ProfileFragment newInstance(User user) {
        Bundle arg = new Bundle();
        arg.putParcelable("user_data", user);
        ProfileFragment fragment = new ProfileFragment();
        fragment.setArguments(arg);
        return  fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null && getArguments().containsKey("user_data")){
            userData = getArguments().getParcelable("user_data");
        }else{
            userData = new User();
        }
        api = new ServerApi(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize views
        fullNameLayout = view.findViewById(R.id.fullNameLayout);
        emailLayout = view.findViewById(R.id.emailLayout);
        passwordLayout = view.findViewById(R.id.passwordLayout);
        fullNameEditText = view.findViewById(R.id.fullNameEditText);
        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        confirmPasswordEditText = view.findViewById(R.id.confirmPasswordEditText);
        profileImageView = view.findViewById(R.id.profileImageView);

        // Set up photo upload button
        view.findViewById(R.id.uploadPhotoButton).setOnClickListener(v -> openImageChooser());

        // Load user data
        loadUserData();

        // Set up update button
        view.findViewById(R.id.updateButton).setOnClickListener(v -> updateProfile());

        return view;
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                // Get the image dimensions
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(requireActivity().getContentResolver().openInputStream(imageUri), null, options);
                
                // Calculate inSampleSize
                options.inSampleSize = calculateInSampleSize(options, 200, 200);
                
                // Decode bitmap with inSampleSize set
                options.inJustDecodeBounds = false;
                Bitmap bitmap = BitmapFactory.decodeStream(requireActivity().getContentResolver().openInputStream(imageUri), null, options);
                
                // Make the bitmap circular
                Bitmap circularBitmap = getCircularBitmap(bitmap);
                
                // Set the circular bitmap to ImageView
                profileImageView.setImageBitmap(circularBitmap);
                
                // Convert bitmap to byte array for storage
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                circularBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                profilePhoto = stream.toByteArray();
                
                Toast.makeText(requireContext(), "Profile photo updated", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(requireContext(), "Error loading image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private Bitmap getCircularBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        android.graphics.Canvas canvas = new android.graphics.Canvas(output);

        final int color = 0xff424242;
        final android.graphics.Paint paint = new android.graphics.Paint();
        final android.graphics.Rect rect = new android.graphics.Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    private void loadUserData() {
        fullNameEditText.setText(userData.getFullname());
        emailEditText.setText(userData.getEmail());

        //                if (photo != null) {
//                    Bitmap bitmap = BitmapFactory.decodeByteArray(photo, 0, photo.length);
//                    profileImageView.setImageBitmap(bitmap);
//                    profilePhoto = photo;
//                }
    }

    private void updateProfile() {
        String fullName = fullNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (validateInputs(fullName, email, password, confirmPassword)) {
            // Prepare parameters for API call
            Map<String, String> params = new HashMap<>();
            params.put("action", "edit");
            params.put("username", userData.getEmail());
            params.put("userid", String.valueOf(userData.getUsid()));
            params.put("email", email);
            params.put("fullname", fullName);
            if (!password.isEmpty()) {
                params.put("password", password);
            }
            params.put("userrole", userData.getUserrole());

            // Show loading state
            if (getActivity() != null) {
                getActivity().findViewById(R.id.updateButton).setEnabled(false);
            }

            // Call API to update profile
            api.users(params, result -> {
                if (getActivity() != null) {
                    getActivity().findViewById(R.id.updateButton).setEnabled(true);
                }

                if (result.status) {
                    // Update local user data
                    User updatedUser = (User) result.getValueAt(0);
                    userData = updatedUser;
                    
                    // Clear password field
                    passwordEditText.setText("");
                    confirmPasswordEditText.setText("");
                    
                    // Show success message
                    Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    
                    // Update navigation header if in MainActivity
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).updateNavigationHeader(userData);
                    }

                    // Update stored user data
                    UserManager.getInstance(requireContext()).saveUser(userData);
                } else {
                    Toast.makeText(requireContext(), "Error: " + result.message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private boolean validateInputs(String fullName, String email, String password, String confirmPassword) {
        boolean isValid = true;

        if (fullName.isEmpty()) {
            fullNameLayout.setError("Full name is required");
            isValid = false;
        } else {
            fullNameLayout.setError(null);
        }

        if (email.isEmpty()) {
            emailLayout.setError("Email is required");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Invalid email format");
            isValid = false;
        } else {
            emailLayout.setError(null);
        }

        if (!password.isEmpty() && !password.equals(confirmPassword)) {
            passwordLayout.setError("Passwords do not match");
            isValid = false;
        } else {
            passwordLayout.setError(null);
        }

        return isValid;
    }
} 