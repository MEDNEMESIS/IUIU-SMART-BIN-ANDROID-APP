package com.example.iuiutrash.adapters;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iuiutrash.R;
import com.example.iuiutrash.model.Bin;
import com.example.iuiutrash.model.BinDataModel;
import com.example.iuiutrash.model.User;
import com.example.iuiutrash.utils.ServerApi;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BinAdapter extends RecyclerView.Adapter<BinAdapter.BinViewHolder> {
    private final List<Bin> binList;
    private final ServerApi api;
    private final User currentUser;
    private final Runnable onDataChangeListener;

    public BinAdapter(List<Bin> binList, ServerApi api, User currentUser, Runnable onDataChangeListener) {
        this.binList = binList;
        this.api = api;
        this.currentUser = currentUser;
        this.onDataChangeListener = onDataChangeListener;
    }

    @NonNull
    @Override
    public BinViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bin, parent, false);
        return new BinViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BinViewHolder holder, int position) {
        Bin bin = binList.get(position);
        holder.bind(bin);
    }

    @Override
    public int getItemCount() {
        return binList.size();
    }

    public void updateData(List<Bin> newBins) {
        binList.clear();
        binList.addAll(newBins);
        notifyDataSetChanged();
    }

    private static int distanceToPercent(float distanceCm) {
        float max = 32f, min = 8f;
        int percent = Math.round((distanceCm - max) / (min - max) * 100f);
        if (percent < 0) percent = 0;
        if (percent > 100) percent = 100;
        return percent;
    }

    class BinViewHolder extends RecyclerView.ViewHolder {
        private final TextView binCodeText;
        private final TextView locationText;
        private final TextView levelText;
        private final TextView statusText;
        private final TextView alertText;
        private final LinearProgressIndicator levelIndicator;
        private final ImageButton editButton;
        private final ImageButton deleteButton;
        private final Button showSummaryButton;

        BinViewHolder(@NonNull View itemView) {
            super(itemView);
            binCodeText = itemView.findViewById(R.id.binCodeText);
            locationText = itemView.findViewById(R.id.locationText);
            levelText = itemView.findViewById(R.id.levelText);
            statusText = itemView.findViewById(R.id.statusText);
            alertText = itemView.findViewById(R.id.alertText);
            levelIndicator = itemView.findViewById(R.id.levelIndicator);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            showSummaryButton = itemView.findViewById(R.id.showSummaryButton);
        }

        void bind(Bin bin) {
            binCodeText.setText(bin.getBinCode());
            locationText.setText(bin.getLocation());
            int level = bin.getCurrentLevel();
            android.util.Log.d("BinAdapter", "bind: bin=" + bin.getBinCode() + ", level=" + level);
            levelText.setText(level + "%");

            // Set status with appropriate color
            statusText.setText(bin.getBinStatus().toUpperCase());
            int statusColor;
            switch (bin.getBinStatus().toLowerCase()) {
                case "active":
                    statusColor = Color.parseColor("#4CAF50"); // Green
                    break;
                case "maintenance":
                    statusColor = Color.parseColor("#FF9800"); // Orange
                    break;
                case "inactive":
                    statusColor = Color.parseColor("#F44336"); // Red
                    break;
                default:
                    statusColor = Color.GRAY;
            }
            statusText.getBackground().setTint(statusColor);

            // Set up level indicator
            levelIndicator.setProgress(level);

            // Update colors and alerts based on level
            int indicatorColor;
            if (level >= 90) {
                indicatorColor = ContextCompat.getColor(itemView.getContext(), android.R.color.holo_red_light);
                alertText.setVisibility(View.VISIBLE);
                alertText.setText("Critical: Bin is almost full!");
            } else if (level >= 75) {
                indicatorColor = ContextCompat.getColor(itemView.getContext(), android.R.color.holo_orange_light);
                alertText.setVisibility(View.VISIBLE);
                alertText.setText("Warning: Bin is filling up");
            } else {
                indicatorColor = ContextCompat.getColor(itemView.getContext(), android.R.color.holo_green_light);
                alertText.setVisibility(View.GONE);
            }
            levelIndicator.setIndicatorColor(indicatorColor);
            levelIndicator.setTrackColor(adjustAlpha(indicatorColor, 0.3f));

            // Only show edit/delete buttons for admin users
            boolean showControls = currentUser.canManageBins();
            editButton.setVisibility(showControls ? View.VISIBLE : View.GONE);
            deleteButton.setVisibility(showControls ? View.VISIBLE : View.GONE);

            editButton.setOnClickListener(v -> showEditDialog(bin));
            deleteButton.setOnClickListener(v -> showDeleteDialog(bin));
            showSummaryButton.setOnClickListener(v -> showBinSummaryDialog(bin));
        }

        private int adjustAlpha(int color, float factor) {
            int alpha = Math.round(Color.alpha(color) * factor);
            int red = Color.red(color);
            int green = Color.green(color);
            int blue = Color.blue(color);
            return Color.argb(alpha, red, green, blue);
        }

        private void showEditDialog(Bin bin) {
            View dialogView = LayoutInflater.from(itemView.getContext())
                    .inflate(R.layout.dialog_add_bin, null);

            TextInputEditText locationInput = dialogView.findViewById(R.id.locationEditText);
            TextInputEditText currentLevelInput = dialogView.findViewById(R.id.currentLevelEditText);
            AutoCompleteTextView statusDropdown = dialogView.findViewById(R.id.statusDropdown);

            // Pre-fill existing data
            locationInput.setText(bin.getLocation());
            currentLevelInput.setText(String.valueOf(bin.getCurrentLevel()));

            // Set up status dropdown
            String[] statuses = new String[]{"active", "inactive", "maintenance"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(itemView.getContext(),
                    android.R.layout.simple_dropdown_item_1line, statuses);
            statusDropdown.setAdapter(adapter);
            statusDropdown.setText(bin.getBinStatus(), false);

            AlertDialog dialog = new AlertDialog.Builder(itemView.getContext())
                    .setTitle("Edit Bin")
                    .setView(dialogView)
                    .setPositiveButton("Save", null)
                    .setNegativeButton("Cancel", null)
                    .create();

            dialog.setOnShowListener(dialogInterface -> {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    String location = locationInput.getText().toString().trim();
                    String levelStr = currentLevelInput.getText().toString().trim();
                    String status = statusDropdown.getText().toString().trim();

                    if (location.isEmpty()) {
                        locationInput.setError("Location is required");
                        return;
                    }

                    int level;
                    try {
                        level = Integer.parseInt(levelStr);
                        if (level < 0 || level > 32) {
                            currentLevelInput.setError("Level must be between 0 and 32");
                            return;
                        }
                    } catch (NumberFormatException e) {
                        currentLevelInput.setError("Invalid level value");
                        return;
                    }

                    Map<String, String> params = new HashMap<>();
                    params.put("action", "edit");
                    params.put("username", currentUser.getEmail());
                    params.put("binid", String.valueOf(bin.getBinId()));
                    params.put("location", location);
                    params.put("currentlevel", String.valueOf(level));
                    params.put("binstatus", status);

                    // First update the bin's basic info
                    api.bins(params, result -> {
                        if (result.status) {
                            // Then create a new bin data record
                            Map<String, String> binDataParams = new HashMap<>();
                            binDataParams.put("action", "new");
                            binDataParams.put("username", currentUser.getEmail());
                            binDataParams.put("binid", String.valueOf(bin.getBinId()));
                            binDataParams.put("levels", String.valueOf(level));

                            api.binData(binDataParams, binDataResult -> {
                                if (binDataResult.status) {
                                    Toast.makeText(itemView.getContext(), "Bin updated successfully", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                    onDataChangeListener.run();
                                } else {
                                    Toast.makeText(itemView.getContext(), "Error updating bin data: " + binDataResult.message, Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(itemView.getContext(), "Error: " + result.message, Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });

            dialog.show();
        }

        private void showDeleteDialog(Bin bin) {
            new AlertDialog.Builder(itemView.getContext())
                    .setTitle("Delete Bin")
                    .setMessage("Are you sure you want to delete this bin?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        Map<String, String> params = new HashMap<>();
                        params.put("action", "delt");
                        params.put("username", currentUser.getEmail());
                        params.put("binid", String.valueOf(bin.getBinId()));

                        api.bins(params, result -> {
                            if (result.status) {
                                Toast.makeText(itemView.getContext(), "Bin deleted successfully", Toast.LENGTH_SHORT).show();
                                onDataChangeListener.run();
                            } else {
                                Toast.makeText(itemView.getContext(), "Error: " + result.message, Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }

        private void showBinSummaryDialog(Bin bin) {
            View dialogView = LayoutInflater.from(itemView.getContext())
                    .inflate(R.layout.dialog_bin_summary, null);

            RecyclerView recyclerView = dialogView.findViewById(R.id.summaryRecyclerView);
            Button returnButton = dialogView.findViewById(R.id.returnButton);
            BinDataAdapter adapter = new BinDataAdapter();
            recyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            recyclerView.setAdapter(adapter);

            AlertDialog dialog = new AlertDialog.Builder(itemView.getContext())
                    .setView(dialogView)
                    .create();

            // Load bin data
            Map<String, String> params = new HashMap<>();
            params.put("action", "select");
            params.put("username", currentUser.getEmail());
            params.put("binid", String.valueOf(bin.getBinId()));

            api.binData(params, result -> {
                if (result.status && result.hasData()) {
                    List<BinDataModel> binDataList = new ArrayList<>();
                    for (Object obj : result.getValues()) {
                        if (obj instanceof BinDataModel) {
                            binDataList.add((BinDataModel) obj);
                        }
                    }
                    adapter.updateData(binDataList);
                } else {
                    Toast.makeText(itemView.getContext(), 
                        "Error loading bin data: " + (result.message != null ? result.message : "Unknown error"), 
                        Toast.LENGTH_SHORT).show();
                }
            });

            returnButton.setOnClickListener(v -> dialog.dismiss());
            dialog.show();
        }
    }
}
