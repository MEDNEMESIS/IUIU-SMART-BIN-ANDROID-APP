package com.example.iuiutrash.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.iuiutrash.MainActivity;
import com.example.iuiutrash.R;
import com.example.iuiutrash.utils.ServerApi;
import com.example.iuiutrash.utils.libs.HttpResult;
import com.example.iuiutrash.model.BinModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class NotificationHelper {
    private static final String CHANNEL_ID = "bin_notification_channel";
    private static final String CHANNEL_NAME = "Bin Notifications";
    private static final String CHANNEL_DESCRIPTION = "Notifications for full bins";
    private static final int COMBINED_NOTIFICATION_ID = 999;

    private Context context;
    private NotificationManagerCompat notificationManager;
    private List<BinModel> fullBins;
    private AtomicInteger pendingChecks;

    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = NotificationManagerCompat.from(context);
        this.fullBins = new ArrayList<>();
        this.pendingChecks = new AtomicInteger(0);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void checkAllBins() {
        clearFullBins();
        pendingChecks.set(2); // We're going to check 2 bins
        
        // Check first bin
        checkBin(1);
        // Check second bin
        checkBin(2);
    }

    private void checkBin(int binId) {
        ServerApi serverApi = new ServerApi(context);
        Map<String, String> params = new HashMap<>();
        params.put("action", "select");
        params.put("binid", String.valueOf(binId));

        serverApi.bins(params, new ServerApi.Callback() {
            @Override
            public void onResults(HttpResult result) {
                try {
                    if (result.status && result.hasData() && result.rows() > 0) {
                        BinModel bin = (BinModel) result.getValueAt(0);
                        if (bin.getCurrentLevel() >= 90) {
                            synchronized (fullBins) {
                                // Add to full bins list if not already present
                                boolean binExists = false;
                                for (BinModel existingBin : fullBins) {
                                    if (existingBin.getBinid() == bin.getBinid()) {
                                        binExists = true;
                                        break;
                                    }
                                }
                                if (!binExists) {
                                    fullBins.add(bin);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    // Decrease pending checks counter and show notification if all checks are done
                    if (pendingChecks.decrementAndGet() == 0) {
                        showAppropriateNotification();
                    }
                }
            }
        });
    }

    private void showAppropriateNotification() {
        synchronized (fullBins) {
            if (fullBins.isEmpty()) {
                return; // No notifications needed
            } else if (fullBins.size() == 1) {
                showSingleBinNotification(fullBins.get(0));
            } else {
                showMultipleBinsNotification();
            }
        }
    }

    private void showSingleBinNotification(BinModel bin) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Bin Alert: " + bin.getBincode())
            .setContentText(bin.getLocation() + " is " + bin.getCurrentLevel() + "% full")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setColor(context.getResources().getColor(R.color.primary_green))
            .setStyle(new NotificationCompat.BigTextStyle()
                .bigText(bin.getLocation() + "\nStatus: " + bin.getBinStatus() + "\nFill Level: " + bin.getCurrentLevel() + "%"));

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, bin.getBinid(), intent, PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntent);

        notificationManager.notify(bin.getBinid(), builder.build());
    }

    public void showSingleBinNotification(String binCode, String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setColor(context.getResources().getColor(R.color.primary_green));

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, binCode.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntent);

        notificationManager.notify(binCode.hashCode(), builder.build());
    }

    private void showMultipleBinsNotification() {
        StringBuilder contentText = new StringBuilder();
        StringBuilder bigText = new StringBuilder("Multiple bins require attention:\n\n");

        synchronized (fullBins) {
            for (BinModel bin : fullBins) {
                contentText.append(bin.getLocation()).append(", ");
                bigText.append(bin.getLocation())
                      .append("\nStatus: ").append(bin.getBinStatus())
                      .append("\nFill Level: ").append(bin.getCurrentLevel()).append("%\n\n");
            }
        }

        // Remove last comma and space from contentText
        if (contentText.length() > 2) {
            contentText.setLength(contentText.length() - 2);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Multiple Bins Alert!")
            .setContentText(contentText.toString())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setColor(context.getResources().getColor(R.color.primary_green))
            .setStyle(new NotificationCompat.BigTextStyle()
                .bigText(bigText.toString()));

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, COMBINED_NOTIFICATION_ID, intent, PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntent);

        notificationManager.notify(COMBINED_NOTIFICATION_ID, builder.build());
    }

    private void showErrorNotification(String binName) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Bin Alert Error")
            .setContentText("Could not fetch status for bin " + binName)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true);

        notificationManager.notify(binName.hashCode(), builder.build());
    }

    public void clearFullBins() {
        synchronized (fullBins) {
            fullBins.clear();
        }
    }
} 