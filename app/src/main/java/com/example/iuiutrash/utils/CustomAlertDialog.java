package com.example.iuiutrash.utils;


import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;

import com.example.iuiutrash.R;

public class CustomAlertDialog {


    private final Context context;

    public CustomAlertDialog(Context context) {
        this.context = context;
    }


    public enum AlertType {
        ERROR,
        WARNING,
        SUCCESS
    }

    public void showAlert(String title, String message) {
        showAlert(AlertType.ERROR, title, message);
    }
    public void showAlert(AlertType type, String title, String message) {
        // Create the parent layout
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = dpToPx(context, 24);
        layout.setPadding(padding, padding, padding, padding);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        // Create and add the icon
        ImageView icon = new ImageView(context);
        Drawable iconDrawable;
        int color;

        switch (type) {
            case ERROR:
                iconDrawable = ContextCompat.getDrawable(context, android.R.drawable.ic_delete);
                color = Color.RED;
                break;
            case WARNING:
                iconDrawable = ContextCompat.getDrawable(context, android.R.drawable.ic_dialog_alert);
                color = Color.YELLOW;
                break;
            case SUCCESS:
                iconDrawable = ContextCompat.getDrawable(context, android.R.drawable.checkbox_on_background);
                color = Color.GREEN;
                break;
            default:
                iconDrawable = ContextCompat.getDrawable(context, android.R.drawable.ic_dialog_info);
                color = Color.BLUE;
                break;
        }

        icon.setImageDrawable(iconDrawable);
        icon.setColorFilter(color);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                dpToPx(context, 48),
                dpToPx(context, 48)
        );
        iconParams.gravity = Gravity.CENTER;
        icon.setLayoutParams(iconParams);
        layout.addView(icon);

        // Create and add the title
        TextView titleView = new TextView(context);
        titleView.setText(title);
        titleView.setTextSize(20);
        titleView.setTextColor(color);
        titleView.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.setMargins(0, dpToPx(context, 16), 0, dpToPx(context, 8));
        titleView.setLayoutParams(titleParams);
        layout.addView(titleView);

        // Create and add the message
        TextView messageView = new TextView(context);
        messageView.setText(message);
        messageView.setTextSize(16);
        messageView.setTextColor(Color.DKGRAY);
        messageView.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams messageParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        messageParams.setMargins(0, 0, 0, dpToPx(context, 16));
        messageView.setLayoutParams(messageParams);

//        layout.setBackgroundColor(Color.WHITE);
        layout.addView(messageView);


        // Build and display the AlertDialog
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomAlertDialogTheme);
        builder.setView(layout);
//        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Helper method to convert dp to pixels
    private static int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
}
