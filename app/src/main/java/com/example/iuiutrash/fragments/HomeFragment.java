package com.example.iuiutrash.fragments;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
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
import com.example.iuiutrash.utils.UserManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class HomeFragment extends Fragment {
    private static final String CHANNEL_ID = "bin_notifications";
    private static final int NOTIFICATION_ID = 1;
    
    private RecyclerView recyclerView;
    private BinAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ServerApi api;
    private User currentUser;
    private final AtomicBoolean isLoading = new AtomicBoolean(false);
    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            loadBins();
            loadBinStats();
            // Refresh every 30 seconds
            refreshHandler.postDelayed(this, 30000);
        }
    };
    private TextView totalBinsText;
    private TextView criticalBinsText;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Create notification channel
        createNotificationChannel();

        // Initialize components
        recyclerView = view.findViewById(R.id.binsRecyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        totalBinsText = view.findViewById(R.id.totalBinsText);
        criticalBinsText = view.findViewById(R.id.criticalBinsText);
        
        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Initialize API and user
        api = new ServerApi(requireContext());
        currentUser = UserManager.getInstance(requireContext()).getUser();
        api.setUsername(currentUser.getEmail());
        
        // Initialize adapter
        adapter = new BinAdapter(new ArrayList<>(), api, currentUser, this::loadBins);
        recyclerView.setAdapter(adapter);

        // Setup swipe refresh
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadBins();
            loadBinStats();
        });

        // Initial data load
        loadBins();
        loadBinStats();

        // Start periodic refresh
        refreshHandler.postDelayed(refreshRunnable, 30000);

        return view;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Bin Status";
            String description = "Notifications for bin status updates";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            
            NotificationManager notificationManager = requireContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void checkAndNotifyBinStatus(List<Bin> bins) {
        for (Bin bin : bins) {
            if (bin.getCurrentLevel() >= 90) {
                showBinFullNotification(bin);
            }
        }
    }

    private void showBinFullNotification(Bin bin) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Bin Alert")
                .setContentText("Bin at " + bin.getLocation() + " is nearly full (" + bin.getCurrentLevel() + "%)")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireContext());
            notificationManager.notify(NOTIFICATION_ID + bin.getBinId(), builder.build());
        } catch (SecurityException e) {
            // Handle the case where notification permission is not granted
            Toast.makeText(getContext(), "Please enable notifications in settings", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Start periodic refresh
        refreshHandler.post(refreshRunnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop periodic refresh
        refreshHandler.removeCallbacks(refreshRunnable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remove callbacks to prevent memory leaks
        refreshHandler.removeCallbacks(refreshRunnable);
    }

    private static int distanceToPercent(float distanceCm) {
        float max = 32f, min = 8f;
        // Inverted formula: when distance is min (8cm), level is 100%, when distance is max (32cm), level is 0%
        int percent = Math.round((distanceCm - max) / (min - max) * 100f);
        if (percent < 0) percent = 0;
        if (percent > 100) percent = 100;
        return percent;
    }

    private void loadBins() {
        // Prevent multiple simultaneous loads
        if (!isLoading.compareAndSet(false, true)) {
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("action", "select");
        params.put("username", currentUser.getEmail());

        api.bins(params, result -> {
            if (isAdded()) {
                swipeRefreshLayout.setRefreshing(false);
                if (result.status) {
                    if (result.hasData()) {
                        List<Bin> bins = new ArrayList<>();
                        // Convert BinModel objects to Bin objects
                        for (Object obj : result.getValues()) {
                            if (obj instanceof BinModel) {
                                BinModel model = (BinModel) obj;
                                Bin bin = new Bin();
                                bin.setBinId(model.getBinid());
                                bin.setBinCode(model.getBincode());
                                // Assume model.getCurrentLevel() returns a percentage. If it returns cm, convert it.
                                int level = model.getCurrentLevel();
                                bin.setCurrentLevel(level);
                                bin.setBinStatus(model.getBinStatus());
                                bin.setLocation(model.getLocation());
                                bins.add(bin);
                            }
                        }
                        adapter.updateData(bins);
                        // Check bin status and show notifications if needed
                        checkAndNotifyBinStatus(bins);
                    } else {
                        Toast.makeText(getContext(), "No bins found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Error: " + result.message, Toast.LENGTH_SHORT).show();
                }
                // Reset loading flag
                isLoading.set(false);
            }
        });
    }

    private void loadBinStats() {
        Map<String, String> params = new HashMap<>();
        params.put("action", "select");
        params.put("username", currentUser.getEmail());

        api.bins(params, result -> {
            if (isAdded()) {
                if (result.status && result.hasData()) {
                    int totalBins = 0;
                    int criticalBins = 0;
                    
                    // Count total bins and critical bins
                    for (Object obj : result.getValues()) {
                        if (obj instanceof BinModel) {
                            BinModel bin = (BinModel) obj;
                            totalBins++;
                            if (bin.getCurrentLevel() >= 90) {
                                criticalBins++;
                            }
                        }
                    }
                    
                    // Update UI with counts
                    totalBinsText.setText(String.valueOf(totalBins));
                    criticalBinsText.setText(String.valueOf(criticalBins));
                } else {
                    Log.e("HomeFragment", "Error loading bin stats: " + result.message);
                }
            }
        });
    }
} 