package com.example.iuiutrash.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iuiutrash.R;
import com.example.iuiutrash.model.BinDataModel;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BinDataAdapter extends RecyclerView.Adapter<BinDataAdapter.ViewHolder> {
    private List<BinDataModel> binDataList;
    private SimpleDateFormat inputFormat;
    private SimpleDateFormat outputFormat;

    public BinDataAdapter() {
        this.binDataList = new ArrayList<>();
        this.inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        this.outputFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bin_data, parent, false);
        return new ViewHolder(view);
    }

    private static int distanceToPercent(float distanceCm) {
        float max = 32f, min = 8f;
        int percent = Math.round((distanceCm - max) / (min - max) * 100f);
        if (percent < 0) percent = 0;
        if (percent > 100) percent = 100;
        return percent;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BinDataModel binData = binDataList.get(position);
        holder.levelText.setText(String.format(Locale.getDefault(), "Level: %d%%", binData.getLevels()));
        
        // Format the date
        try {
            String formattedDate = outputFormat.format(inputFormat.parse(binData.getCreated_at()));
            holder.dateText.setText(formattedDate);
        } catch (Exception e) {
            holder.dateText.setText(binData.getCreated_at());
        }

        // Update indicator color based on level
        int level = binData.getLevels();
        android.util.Log.d("BinDataAdapter", "onBindViewHolder: level=" + level);
        holder.levelIndicator.setProgress(level);
        int colorRes = level >= 90 ? R.color.red :
                      level >= 75 ? android.R.color.holo_orange_light :
                      level >= 50 ? android.R.color.holo_orange_light :
                      R.color.primary_green;
        holder.levelIndicator.setIndicatorColor(
            ContextCompat.getColor(holder.itemView.getContext(), colorRes)
        );
    }

    @Override
    public int getItemCount() {
        return binDataList.size();
    }

    public void updateData(List<BinDataModel> newData) {
        this.binDataList = newData;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView levelText;
        TextView dateText;
        CircularProgressIndicator levelIndicator;

        ViewHolder(View view) {
            super(view);
            levelText = view.findViewById(R.id.levelText);
            dateText = view.findViewById(R.id.dateText);
            levelIndicator = view.findViewById(R.id.levelIndicator);
        }
    }
}
