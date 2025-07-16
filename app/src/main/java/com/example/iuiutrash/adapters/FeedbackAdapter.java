package com.example.iuiutrash.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.iuiutrash.R;
import com.example.iuiutrash.model.FeedbackModel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder> {
    private List<FeedbackModel> feedbackList;
    private final OnFeedbackClickListener listener;
    private final SimpleDateFormat inputFormat;
    private final SimpleDateFormat outputFormat;

    public interface OnFeedbackClickListener {
        void onDeleteClick(FeedbackModel feedback);
    }

    public FeedbackAdapter(List<FeedbackModel> feedbackList, OnFeedbackClickListener listener) {
        this.feedbackList = feedbackList;
        this.listener = listener;
        this.inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        this.outputFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
    }

    public void updateData(List<FeedbackModel> newFeedbackList) {
        this.feedbackList = newFeedbackList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FeedbackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_feedback, parent, false);
        return new FeedbackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedbackViewHolder holder, int position) {
        FeedbackModel feedback = feedbackList.get(position);
        
        // Set user information
        holder.userNameText.setText(feedback.getFullname() != null ? 
            feedback.getFullname() : feedback.getUsername());
        
        // Set feedback text
        holder.feedbackText.setText(feedback.getFeedbackText());
        
        // Set rating
        holder.ratingBar.setRating(feedback.getRating());
        
        // Set date
        if (feedback.getCreatedAt() != null) {
            try {
                java.util.Date date = inputFormat.parse(feedback.getCreatedAt());
                holder.dateText.setText(outputFormat.format(date));
            } catch (ParseException e) {
                holder.dateText.setText(feedback.getCreatedAt());
            }
        } else {
            holder.dateText.setText("Date not available");
        }

        // Set delete button visibility based on user role
        holder.deleteButton.setVisibility(View.VISIBLE);
        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(feedback);
            }
        });
    }

    @Override
    public int getItemCount() {
        return feedbackList != null ? feedbackList.size() : 0;
    }

    static class FeedbackViewHolder extends RecyclerView.ViewHolder {
        TextView userNameText;
        TextView feedbackText;
        RatingBar ratingBar;
        TextView dateText;
        ImageButton deleteButton;

        FeedbackViewHolder(View itemView) {
            super(itemView);
            userNameText = itemView.findViewById(R.id.userNameText);
            feedbackText = itemView.findViewById(R.id.feedbackText);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            dateText = itemView.findViewById(R.id.dateText);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
} 