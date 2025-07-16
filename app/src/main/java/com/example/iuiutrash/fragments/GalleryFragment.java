package com.example.iuiutrash.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.iuiutrash.R;
import com.example.iuiutrash.adapters.GalleryAdapter;
import com.example.iuiutrash.models.GalleryItem;
import java.util.ArrayList;
import java.util.List;

public class GalleryFragment extends Fragment {
    private RecyclerView galleryRecyclerView;
    private GalleryAdapter galleryAdapter;
    private List<GalleryItem> galleryItems;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);
        
        galleryRecyclerView = view.findViewById(R.id.galleryRecyclerView);
        galleryRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        // Initialize gallery items with bin images
        galleryItems = new ArrayList<>();
        galleryItems.add(new GalleryItem(R.drawable.gallery1
                , "FRONT VIEW OF BIN", "front sensor for touchless disporsal"));
        galleryItems.add(new GalleryItem(R.drawable.gallery2, "FULL VIEW OF THE BIN", "complete bin "));
        galleryItems.add(new GalleryItem(R.drawable.gallery3, "HARDWARE SETUP", "hardware assembly testing and problem solving"));
        galleryItems.add(new GalleryItem(R.drawable.gallery4, "CODE IMPLEMENTATION ", "testing and implementing code logic"));
        galleryItems.add(new GalleryItem(R.drawable.gallery5, "LCD SHOWING BIN COUNTER", "closing counter"));
        
        galleryAdapter = new GalleryAdapter(galleryItems);
        galleryRecyclerView.setAdapter(galleryAdapter);

        return view;
    }
} 