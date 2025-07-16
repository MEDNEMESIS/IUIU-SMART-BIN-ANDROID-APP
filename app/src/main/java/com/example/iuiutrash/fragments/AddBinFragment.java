package com.example.iuiutrash.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.iuiutrash.R;
import com.example.iuiutrash.model.User;
import com.example.iuiutrash.utils.ServerApi;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.button.MaterialButton;
import java.util.HashMap;
import java.util.Map;

public class AddBinFragment extends Fragment {
    private static final String TAG = "AddBinFragment";
    private TextInputLayout locationLayout;
    private TextInputEditText locationEditText;
    private MaterialButton addBinButton;
    private ServerApi api;
    private User userData;

    public static AddBinFragment newInstance(User userData) {
        AddBinFragment fragment = new AddBinFragment();
        Bundle args = new Bundle();
        args.putParcelable("userData", userData);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userData = getArguments().getParcelable("userData");
        }
        api = new ServerApi(requireContext());
        if (userData != null) {
            api.setUsername(userData.getEmail());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_bin, container, false);

        // Initialize views
        locationLayout = view.findViewById(R.id.locationLayout);
        locationEditText = view.findViewById(R.id.locationEditText);
        addBinButton = view.findViewById(R.id.addBinButton);

        // Set up add button click listener
        addBinButton.setOnClickListener(v -> addBin());

        return view;
    }

    private void addBin() {
        String location = locationEditText.getText().toString().trim();

        // Validate location
        if (location.isEmpty()) {
            locationLayout.setError("Location is required");
            return;
        }

        // Clear any previous errors
        locationLayout.setError(null);

        // Prepare parameters for API call
        Map<String, String> params = new HashMap<>();
        params.put("action", "new");
        params.put("location", location);
        params.put("username", userData.getEmail());

        // Disable button while processing
        addBinButton.setEnabled(false);

        // Call API to add bin
        api.bins(params, result -> {
            // Re-enable button
            addBinButton.setEnabled(true);

            if (result.status) {
                Toast.makeText(requireContext(), "Bin added successfully", Toast.LENGTH_SHORT).show();
                // Clear the form
                locationEditText.setText("");
            } else {
                Toast.makeText(requireContext(), "Error: " + result.message, Toast.LENGTH_SHORT).show();
            }
        });
    }
} 