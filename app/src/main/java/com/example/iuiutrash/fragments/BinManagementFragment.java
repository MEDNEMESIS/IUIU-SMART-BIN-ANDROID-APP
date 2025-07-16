package com.example.iuiutrash.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.iuiutrash.R;
import com.example.iuiutrash.adapters.BinAdapter;
import com.example.iuiutrash.model.Bin;
import com.example.iuiutrash.model.BinModel;
import com.example.iuiutrash.model.User;
import com.example.iuiutrash.utils.ServerApi;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BinManagementFragment extends Fragment {
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View emptyView;
    private FloatingActionButton fabAddBin;
    private ServerApi api;
    private User userData;
    private List<Bin> binList;
    private BinAdapter adapter;

    public static BinManagementFragment newInstance(User userData) {
        BinManagementFragment fragment = new BinManagementFragment();
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
        api.setUsername(userData.getEmail());
        binList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bin_management, container, false);

        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        emptyView = view.findViewById(R.id.emptyView);
        fabAddBin = view.findViewById(R.id.fabAddBin);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new BinAdapter(binList, api, userData, this::loadBins);
        recyclerView.setAdapter(adapter);

        // Set up SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(this::loadBins);

        // Set up FAB - only show for admin users
        fabAddBin.setVisibility(userData.canManageBins() ? View.VISIBLE : View.GONE);
        fabAddBin.setOnClickListener(v -> showAddBinDialog());

        // Initial data load
        loadBins();

        return view;
    }

    private void loadBins() {
        Map<String, String> params = new HashMap<>();
        params.put("action", "select");
        params.put("username", userData.getEmail());

        api.bins(params, result -> {
            if (isAdded()) {
                swipeRefreshLayout.setRefreshing(false);
                if (result.status) {
                    if (result.hasData()) {
                        binList.clear();
                        // Convert BinModel objects to Bin objects
                        for (Object obj : result.getValues()) {
                            if (obj instanceof BinModel) {
                                BinModel model = (BinModel) obj;
                                Bin bin = new Bin();
                                bin.setBinId(model.getBinid());
                                bin.setBinCode(model.getBincode());
                                bin.setCurrentLevel(model.getCurrentLevel());
                                bin.setBinStatus(model.getBinStatus());
                                bin.setLocation(model.getLocation());
                                binList.add(bin);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        updateEmptyView(false);
                    } else {
                        updateEmptyView(true);
                    }
                } else {
                    Toast.makeText(requireContext(), "Error: " + result.message, Toast.LENGTH_SHORT).show();
                    updateEmptyView(true);
                }
            }
        });
    }

    private void showAddBinDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_bin, null);

        TextInputEditText locationInput = dialogView.findViewById(R.id.locationEditText);
        TextInputEditText currentLevelInput = dialogView.findViewById(R.id.currentLevelEditText);
        AutoCompleteTextView statusDropdown = dialogView.findViewById(R.id.statusDropdown);

        // Set up status dropdown
        String[] statuses = new String[]{"active", "inactive", "maintenance"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, statuses);
        statusDropdown.setAdapter(adapter);
        statusDropdown.setText(statuses[0], false); // Set default to active

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Add New Bin")
                .setView(dialogView)
                .setPositiveButton("Add", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                String location = locationInput.getText().toString().trim();
                String levelStr = currentLevelInput.getText().toString().trim();
                String status = statusDropdown.getText().toString().trim();

                if (location.isEmpty()) {
                    Toast.makeText(requireContext(), "Location is required", Toast.LENGTH_SHORT).show();
                    return;
                }

                int level;
                try {
                    level = Integer.parseInt(levelStr);
                    if (level < 0 || level > 100) {
                        Toast.makeText(requireContext(), "Level must be between 0 and 100", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(requireContext(), "Invalid level value", Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, String> params = new HashMap<>();
                params.put("action", "new");
                params.put("username", userData.getEmail());
                params.put("location", location);
                params.put("levels", String.valueOf(level));
                params.put("binstatus", status);

                api.bins(params, result -> {
                    if (result.status) {
                        Toast.makeText(requireContext(), "Bin added successfully", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadBins();
                    } else {
                        Toast.makeText(requireContext(), "Error: " + result.message, Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

        dialog.show();
    }

    private void updateEmptyView(boolean isEmpty) {
        if (isEmpty) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }
} 