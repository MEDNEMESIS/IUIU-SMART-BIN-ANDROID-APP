package com.example.iuiutrash.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import com.example.iuiutrash.R;

public class ShareFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_share, container, false);
        
        // Set up share button
        view.findViewById(R.id.shareButton).setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Smart Trash App");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this amazing Smart Trash app: [App Store Link]");
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        });
        
        return view;
    }
} 