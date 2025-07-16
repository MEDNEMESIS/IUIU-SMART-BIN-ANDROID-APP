package com.example.iuiutrash.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.iuiutrash.R;
import com.example.iuiutrash.adapters.FeedbackAdapter;
import com.example.iuiutrash.model.FeedbackModel;
import com.example.iuiutrash.utils.ServerApi;
import com.example.iuiutrash.utils.UserManager;
import com.example.iuiutrash.utils.libs.HttpResult;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;
import org.json.JSONException;
import org.json.JSONArray;

public class AdminFeedbackFragment extends Fragment implements FeedbackAdapter.OnFeedbackClickListener {
    private static final String TAG = "AdminFeedbackFragment";
    private RecyclerView feedbackRecyclerView;
    private FeedbackAdapter feedbackAdapter;
    private List<FeedbackModel> feedbackList;
    private ServerApi api;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_feedback, container, false);
        
        // Initialize views
        feedbackRecyclerView = view.findViewById(R.id.feedbackRecyclerView);
        feedbackRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Initialize data
        feedbackList = new ArrayList<>();
        feedbackAdapter = new FeedbackAdapter(feedbackList, this);
        feedbackRecyclerView.setAdapter(feedbackAdapter);
        
        // Initialize API
        api = new ServerApi(requireContext());
        api.setUsername(UserManager.getInstance(requireContext()).getUser().getEmail());
        
        // Load feedback data
        loadFeedback();
        
        return view;
    }

    private void loadFeedback() {
        Map<String, String> params = new HashMap<>();
        params.put("action", "select");
        params.put("username", UserManager.getInstance(requireContext()).getUser().getEmail());

        Log.d(TAG, "Loading feedback with params: " + params);

        api.feedback(params, new ServerApi.Callback<FeedbackModel>() {
            @Override
            public void onResults(HttpResult<FeedbackModel> result) {
                Log.d(TAG, "Received feedback response - Status: " + result.status + 
                          ", Message: " + result.message + 
                          ", Values size: " + (result.values != null ? result.values.size() : 0) + 
                          ", Has Data: " + result.hasData());
                
                if (result.status) {
                    if (result.hasData()) {
                        List<FeedbackModel> newFeedbackList = new ArrayList<>(result.values);
                        
                        requireActivity().runOnUiThread(() -> {
                            feedbackAdapter.updateData(newFeedbackList);
                            if (newFeedbackList.isEmpty()) {
                                Toast.makeText(requireContext(), 
                                    "No feedback data available", 
                                    Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(requireContext(), 
                                    "Loaded " + newFeedbackList.size() + " feedback entries", 
                                    Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Log.w(TAG, "No feedback data available");
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), 
                                "No feedback data available", 
                                Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    Log.w(TAG, "Feedback response indicates error");
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), 
                            "Failed to load feedback: " + result.message, 
                            Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    @Override
    public void onDeleteClick(FeedbackModel feedback) {
        // Show confirmation dialog before deleting
        new AlertDialog.Builder(requireContext())
            .setTitle("Delete Feedback")
            .setMessage("Are you sure you want to delete this feedback?")
            .setPositiveButton("Delete", (dialog, which) -> deleteFeedback(feedback))
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void deleteFeedback(FeedbackModel feedback) {
        api.deleteFeedback(UserManager.getInstance(requireContext()).getUser().getEmail(), feedback.getId(), new ServerApi.Callback<FeedbackModel>() {
            @Override
            public void onResults(HttpResult<FeedbackModel> result) {
                requireActivity().runOnUiThread(() -> {
                    if (result.status) {
                        Toast.makeText(requireContext(), "Feedback deleted successfully", Toast.LENGTH_SHORT).show();
                        loadFeedback(); // Reload the list
                    } else {
                        Toast.makeText(requireContext(), "Error deleting feedback: " + result.message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
} 