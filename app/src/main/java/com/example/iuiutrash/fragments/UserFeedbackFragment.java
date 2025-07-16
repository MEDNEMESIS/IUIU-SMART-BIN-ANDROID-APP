package com.example.iuiutrash.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.iuiutrash.R;
import com.example.iuiutrash.utils.ServerApi;
import com.example.iuiutrash.utils.UserManager;
import com.google.android.material.button.MaterialButton;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class UserFeedbackFragment extends Fragment {
    private EditText feedbackInput;
    private RatingBar ratingBar;
    private MaterialButton submitButton;
    private ServerApi serverApi;
    private UserManager userManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_feedback, container, false);

        feedbackInput = view.findViewById(R.id.feedbackInput);
        ratingBar = view.findViewById(R.id.ratingBar);
        submitButton = view.findViewById(R.id.submitButton);
        
        serverApi = new ServerApi(requireContext());
        userManager = UserManager.getInstance(requireContext());
        
        // Set the current username
        if (userManager.getUser() != null) {
            serverApi.setUsername(userManager.getUser().getEmail());
        }

        submitButton.setOnClickListener(v -> submitFeedback());

        return view;
    }

    private void submitFeedback() {
        String feedback = feedbackInput.getText().toString().trim();
        float rating = ratingBar.getRating();

        if (feedback.isEmpty()) {
            feedbackInput.setError("Please enter your feedback");
            return;
        }

        if (rating == 0) {
            Toast.makeText(getContext(), "Please provide a rating", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!userManager.isLoggedIn()) {
            Toast.makeText(getContext(), "Please login to submit feedback", Toast.LENGTH_SHORT).show();
            return;
        }

        String username = userManager.getUser().getEmail();

        // Show loading state
        submitButton.setEnabled(false);
        submitButton.setText("Submitting...");

        // Submit feedback using the new method
        serverApi.submitFeedback(username, feedback, rating, result -> {
            requireActivity().runOnUiThread(() -> {
                if (result.status) {
                    Toast.makeText(getContext(), "Thank you for your feedback!", Toast.LENGTH_SHORT).show();
                    // Clear the form
                    feedbackInput.setText("");
                    ratingBar.setRating(0);
                } else {
                    Toast.makeText(getContext(), "Error: " + result.message, Toast.LENGTH_SHORT).show();
                }
                submitButton.setEnabled(true);
                submitButton.setText("Submit");
            });
        });
    }
} 